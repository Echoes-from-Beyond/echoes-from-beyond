/*
 * Echoes from Beyond: Hytale Mod
 * Copyright (C) 2025 Echoes from Beyond Team <chemky2000@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.echoesfrombeyond.util.type;

import java.lang.reflect.*;
import java.util.*;
import org.echoesfrombeyond.util.iterable.IterableUtil;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Utilities relating to reflection and types. */
@NullMarked
public final class TypeUtil {
  /**
   * Attempts to resolve the component type.
   *
   * <p>If {@code type} is a {@link Class} and {@link Class#isArray()} returns {@code true}, returns
   * the component type; else {@code null}.
   *
   * <p>If {@code type} is a {@link GenericArrayType}, returns the generic component type.
   *
   * <p>In all other cases, returns {@code null}.
   *
   * @param type the type
   * @return the component type; or {@code null} if {@code type} is not an array class or instance
   *     of {@link GenericArrayType}.
   */
  public static @Nullable Type getArrayComponentType(Type type) {
    return switch (type) {
      case Class<?> cls -> cls.isArray() ? cls.getComponentType() : null;
      case GenericArrayType generic -> generic.getGenericComponentType();
      default -> null;
    };
  }

  /**
   * Attempts to replace the raw type of {@code genericSupertype} with {@code rawSubtype}.
   *
   * <p>If {@code rawSubtype} is not assignable to the raw type of {@code genericSupertype}, {@code
   * genericSupertype} is returned as-is.
   *
   * <p>Otherwise, if {@code genericSupertype} is an instance of {@link ParameterizedType}, the
   * returned type will have the same raw type as {@code rawSubtype}, with the (applicable) type
   * arguments of the supertype preserved.
   *
   * <p>Otherwise, if {@code genericSupertype} is an instance of {@link Class}, returns {@code
   * rawSubtype}.
   *
   * <p>In all other cases, returns {@code genericSupertype}.
   *
   * @param genericSupertype the base type
   * @param rawSubtype the raw type
   * @return the new type
   */
  public static Type replaceRawType(Type genericSupertype, Class<?> rawSubtype) {
    return switch (genericSupertype) {
      case ParameterizedType parameterizedSupertype -> {
        var rawSupertype = (Class<?>) parameterizedSupertype.getRawType();
        if (rawSupertype.equals(rawSubtype) || !rawSupertype.isAssignableFrom(rawSubtype))
          yield genericSupertype;

        var parameterMap = resolveSupertypeParameters(rawSubtype, rawSupertype);
        assert parameterMap != null;

        var supertypeParameters = Arrays.asList(rawSupertype.getTypeParameters());
        var supertypeArguments = parameterizedSupertype.getActualTypeArguments();

        var subtypeParameters = rawSubtype.getTypeParameters();

        var adjustedTypeArguments = new Type[subtypeParameters.length];
        for (int i = 0; i < adjustedTypeArguments.length; i++) {
          Type argument = null;
          for (var entry : parameterMap.entrySet()) {
            if (!entry.getValue().equals(subtypeParameters[i])) continue;

            var key = supertypeParameters.indexOf(entry.getKey());
            if (key >= 0) {
              argument = supertypeArguments[key];
              break;
            }
          }

          assert argument != null;
          adjustedTypeArguments[i] =
              argument instanceof TypeVariable<?> ? subtypeParameters[i] : argument;
        }

        var nestHost = rawSubtype.getNestHost();
        yield new ParameterizedTypeImpl(
            nestHost.equals(rawSubtype) ? null : nestHost, rawSubtype, adjustedTypeArguments);
      }
      case Class<?> rawSupertype ->
          rawSupertype.isAssignableFrom(rawSubtype) ? rawSubtype : genericSupertype;
      default -> genericSupertype;
    };
  }

  /**
   * Compute the distance between a base class and superclass.
   *
   * <p>For example, if class A extends B, {@code inheritanceDistance(A.class, B.class) == 1}.
   *
   * <p>If {@code base} does not extend {@code superclass}, a negative value is returned.
   *
   * @param base the base class
   * @param superclass the superclass
   * @return the distance between the two classes in the inheritance tree, or {@code -1} if they are
   *     unrelated
   */
  public static int inheritanceDistance(Class<?> base, Class<?> superclass) {
    if (!superclass.isAssignableFrom(base)) return -1;
    if (base.equals(superclass)) return 0;

    int distance = 0;
    var queue = new ArrayDeque<Class<?>[]>();
    queue.push(new Class[] {base});

    while (!queue.isEmpty()) {
      distance++;

      var first = queue.removeFirst();

      outer:
      for (var cur : first) {
        var next = cur.getSuperclass();
        if (superclass.equals(next)) break;

        var superinterfaces = cur.getInterfaces();
        for (var superinterface : superinterfaces)
          if (superinterface.equals(superclass)) break outer;

        int offset = next == null ? 0 : 1;
        var elems = new Class[offset + superinterfaces.length];
        if (next != null) elems[0] = next;

        System.arraycopy(superinterfaces, 0, elems, offset, superinterfaces.length);
        queue.push(elems);
      }
    }

    return distance;
  }

  /**
   * Construct an {@link Iterable} that iterates the class hierarchy between {@code base} and {@code
   * stop}.
   *
   * <p>If {@code base.equals(stop)}, the iterable will only iterate a single element ({@code
   * base}).
   *
   * <p>If {@code base} does not extend {@code stop}, the iterable will be empty.
   *
   * @param base the starting class
   * @param stop the stopping class
   * @return a class hierarchy iterable
   */
  public static Iterable<Class<?>> traverseHierarchy(Class<?> base, Class<?> stop) {
    if (base.equals(stop)) return IterableUtil.onceIterable(base);
    if (!stop.isAssignableFrom(base)) return IterableUtil::emptyIterator;

    return () ->
        new Iterator<>() {
          private final Deque<Class<?>> queue;
          private boolean foundStop;

          {
            queue = new ArrayDeque<>();
            queue.push(base);
          }

          @Override
          public boolean hasNext() {
            return !queue.isEmpty() && !foundStop;
          }

          @Override
          public Class<?> next() {
            var end = foundStop;
            if (queue.isEmpty() || end) throw new NoSuchElementException();

            var next = queue.removeFirst();
            if (next.equals(stop)) {
              foundStop = true;
              return next;
            }

            var superclass = next.getSuperclass();
            if (superclass != null) queue.addLast(superclass);
            for (var superinterface : next.getInterfaces()) queue.addLast(superinterface);

            return next;
          }
        };
  }

  /**
   * If {@code type} is {@link ParameterizedType}, returns the raw type. If {@code type} is {@link
   * Class}, returns {@code type} after casting.
   *
   * <p>If {@code type} is {@link GenericArrayType}, attempts to recursively resolve the raw type of
   * the generic component type, then returns the array type of that raw type (if it exists).
   *
   * <p>If {@code type} is a {@link WildcardType}, return the result of recursively resolving the
   * upper bounds.
   *
   * @param type the type object
   * @return the raw type, or {@code null} if {@code type} is not a Class, ParameterizedType, or
   *     GenericArrayType whose component is the same
   */
  @Contract("null -> null")
  public static @Nullable Class<?> getRawType(@Nullable Type type) {
    if (type == null) return null;

    return switch (type) {
      case Class<?> cls -> cls;
      case ParameterizedType pt -> (Class<?>) pt.getRawType();
      case GenericArrayType generic -> {
        var next = getRawType(generic.getGenericComponentType());
        yield next == null ? null : next.arrayType();
      }
      case WildcardType wt -> getRawType(wt.getUpperBounds()[0]);
      default -> null;
    };
  }

  /**
   * Resolve the actual type parameters of {@code base}, relative to the superclass {@code
   * supertype}. The returned map will always be modifiable.
   *
   * <p>If {@code base} does not subclass {@code supertype}, this method will return {@code null}.
   *
   * @param base the base class
   * @param supertype the superclass
   * @return {@code null} if {@code base} does not subclass {@code supertype}, otherwise a map
   *     linking {@link TypeVariable} instances to actual types.
   */
  public static @Nullable Map<TypeVariable<?>, Type> resolveSupertypeParameters(
      Type base, Class<?> supertype) {
    var baseRaw = getRawType(base);
    if (baseRaw == null || !supertype.isAssignableFrom(baseRaw)) return null;

    var queue = new ArrayDeque<Type>();
    queue.add(base);

    var map = new HashMap<TypeVariable<?>, Type>();
    while (true) {
      var cur = queue.removeFirst();
      var raw = getRawType(cur);

      // We only add types for which getRawType returns non-null.
      assert raw != null;

      var typeVariables = raw.getTypeParameters();
      var typeArguments =
          cur instanceof ParameterizedType pt
              ? pt.getActualTypeArguments()
              : raw.getTypeParameters();

      for (int i = 0; i < typeVariables.length; i++) {
        var arg = typeArguments[i];

        //noinspection SuspiciousMethodCalls
        map.put(typeVariables[i], map.getOrDefault(arg, arg));
      }

      if (raw.equals(supertype)) return map;

      queue(raw.getGenericSuperclass(), queue);
      for (var genericInterface : raw.getGenericInterfaces()) queue(genericInterface, queue);
    }
  }

  private static void queue(@Nullable Type type, Deque<Type> queue) {
    switch (type) {
      case Class<?> _, ParameterizedType _ -> queue.addLast(type);
      case null, default -> {}
    }
  }
}
