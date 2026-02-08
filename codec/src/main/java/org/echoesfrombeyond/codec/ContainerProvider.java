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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@FunctionalInterface
public interface ContainerProvider {
  interface Immutable<C extends Collection<?>> {
    @Nullable Function<C, C> immutableCreator(Class<?> raw);

    @Nullable @Unmodifiable
    C emptyImmutable(Class<?> raw);
  }

  record Spec<C extends @Nullable Collection<?>>(
      Supplier<? extends @NonNull C> creator,
      @Nullable Function<? super @NonNull C, ? extends @NonNull C> makeImmutable,
      @Nullable C emptyImmutable) {}

  Spec<? extends Collection<?>> forType(Class<?> type, Field field);

  static ContainerProvider withAbstractMappings(
      Map<Class<?>, Class<?>> mappings, boolean tryMakeImmutable) {
    var handles = new HashMap<Class<?>, MethodHandle>(mappings.size());

    for (var entry : mappings.entrySet()) {
      var key = entry.getKey();
      var value = entry.getValue();

      if (Modifier.isAbstract(value.getModifiers()))
        throw new IllegalArgumentException(
            "Abstract type mapping value was abstract " + value.getName());

      if (!key.isAssignableFrom(value))
        throw new IllegalArgumentException(
            "Abstract type mapping key did not superclass value " + value.getName());

      try {
        handles.put(
            key, MethodHandles.lookup().findConstructor(value, MethodType.methodType(void.class)));
      } catch (NoSuchMethodException e) {
        throw new IllegalArgumentException(
            "Abstract type mapping value did not have a public parameterless constructor "
                + value.getName());
      } catch (IllegalAccessException e) {
        throw new IllegalArgumentException(value.getName(), e);
      }
    }

    return (type, _) -> {
      var handle = handles.get(type);
      if (handle == null) {
        if (Modifier.isAbstract(type.getModifiers()))
          throw new IllegalArgumentException("Missing abstract type mapping for " + type.getName());

        try {
          handle =
              MethodHandles.publicLookup().findConstructor(type, MethodType.methodType(void.class));
        } catch (NoSuchMethodException e) {
          throw new IllegalArgumentException(
              "No public parameterless constructor found for class " + type.getName());
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        }
      }

      return makeSpec(handle, type, tryMakeImmutable);
    };
  }

  @SuppressWarnings("unchecked")
  private static <T extends @Nullable Collection<?>> Spec<T> makeSpec(
      MethodHandle handle, Class<?> raw, boolean tryMakeImmutable) {

    Function<@NonNull T, @NonNull T> makeImmutable = null;
    T emptyImmutable = null;

    if (tryMakeImmutable) {
      if (raw.isAssignableFrom(List.class)) {
        makeImmutable = objects -> (T) Collections.unmodifiableList((List<?>) objects);
        emptyImmutable = (T) List.of();
      } else if (raw.isAssignableFrom(Set.class)) {
        makeImmutable = objects -> (T) Collections.unmodifiableSet((Set<?>) objects);
        emptyImmutable = (T) Set.of();
      } else if (raw.isAssignableFrom(Collection.class)) {
        makeImmutable = objects -> (T) Collections.unmodifiableCollection((Collection<?>) objects);
        emptyImmutable = (T) List.of();
      }
    }

    return new Spec<>(
        () -> {
          try {
            return (T) handle.invoke();
          } catch (Throwable e) {
            throw new RuntimeException(e);
          }
        },
        makeImmutable,
        emptyImmutable);
  }
}
