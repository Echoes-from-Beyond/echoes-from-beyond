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
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Supplier;
import org.jspecify.annotations.NullMarked;

@NullMarked
@FunctionalInterface
public interface ContainerProvider {
  Supplier<? extends Collection<?>> forType(Class<?> type);

  static ContainerProvider withAbstractMappings(Map<Class<?>, Class<?>> mappings) {
    var handles = new HashMap<Class<?>, MethodHandle>(mappings.size());

    for (var entry : mappings.entrySet()) {
      var key = entry.getKey();
      var value = entry.getValue();

      if (Modifier.isAbstract(value.getModifiers()))
        throw new IllegalArgumentException(
            "Abstract type mapping value was abstract class " + value.getName());

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

    return type -> {
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

      var finalHandle = handle;

      return () -> {
        try {
          return (Collection<?>) finalHandle.invoke();
        } catch (Throwable e) {
          throw new RuntimeException(e);
        }
      };
    };
  }
}
