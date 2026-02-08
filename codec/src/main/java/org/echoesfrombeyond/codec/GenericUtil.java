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

package org.echoesfrombeyond.codec;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import org.echoesfrombeyond.util.iterable.IterableUtil;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class GenericUtil {
  public static int hierarchyDistance(Class<?> base, Class<?> superclass) {
    if (!superclass.isAssignableFrom(base)) return -1;
    if (base.equals(superclass)) return 0;

    int distance = 0;
    var queue = new ArrayDeque<Class<?>>();
    queue.push(base);

    while (!queue.isEmpty()) {
      var cur = queue.removeFirst();

      var next = cur.getSuperclass();
      if (superclass.equals(next)) break;

      var superinterfaces = cur.getInterfaces();
      for (var superinterface : superinterfaces) if (superinterface.equals(superclass)) break;

      if (next != null) queue.push(next);
      for (var superinterface : superinterfaces) queue.push(superinterface);

      distance++;
    }

    return distance;
  }

  public static Iterable<Class<?>> traverseHierarchy(Class<?> base, Class<?> stop) {
    if (base.equals(stop)) return IterableUtil.onceIterable(base);
    if (!stop.isAssignableFrom(base)) return IterableUtil::emptyIterator;

    var queue = new ArrayDeque<Class<?>>();
    queue.push(base);

    return () ->
        new Iterator<>() {
          private boolean foundStop;

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
            if (!next.isInterface() && superclass != null) queue.addLast(superclass);
            for (var superinterface : next.getInterfaces()) queue.addLast(superinterface);

            return next;
          }
        };
  }

  public static @Nullable Class<?> getRawType(Type type) {
    return switch (type) {
      case Class<?> cls -> cls;
      case ParameterizedType pt -> (Class<?>) pt.getRawType();
      default -> null;
    };
  }

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

      // We only add ParameterizedType and Class<?>.
      assert raw != null;

      if (cur instanceof ParameterizedType pt) {
        var typeVariables = raw.getTypeParameters();
        var typeArguments = pt.getActualTypeArguments();

        for (int i = 0; i < typeVariables.length; i++) {
          var arg = typeArguments[i];

          //noinspection SuspiciousMethodCalls
          map.put(typeVariables[i], map.getOrDefault(arg, arg));
        }
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
