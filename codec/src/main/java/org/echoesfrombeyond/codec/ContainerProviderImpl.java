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
import java.util.Collection;
import java.util.Map;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class ContainerProviderImpl implements ContainerProvider {
  private final ClassHierarchyMap<MethodHandle> handles;
  private final ClassHierarchyMap<Spec.Immutable<Object>> immutables;

  ContainerProviderImpl(
      Map<Class<?>, Class<?>> abstractMappings, Iterable<Spec.Immutable<?>> immutableMappings) {
    this.handles = new HashClassHierarchyMap<>();
    this.immutables = new HashClassHierarchyMap<>();

    for (var entry : abstractMappings.entrySet()) {
      var key = entry.getKey();
      var value = entry.getValue();

      if (!Collection.class.isAssignableFrom(key))
        throw new IllegalArgumentException(
            "Abstract mapping key does not extend Collection " + value.getName());

      if (!key.isAssignableFrom(value))
        throw new IllegalArgumentException(
            "Abstract mapping key "
                + key.getName()
                + " does not superclass value "
                + value.getName());

      if (Modifier.isAbstract(value.getModifiers()))
        throw new IllegalArgumentException(
            "Abstract mapping value was abstract " + value.getName());

      this.handles.put(key, extractConstructor(value));
    }

    addImmutables(immutableMappings, this.immutables);
  }

  private static MethodHandle extractConstructor(Class<?> cls) {
    try {
      return MethodHandles.publicLookup().findConstructor(cls, MethodType.methodType(void.class));
    } catch (NoSuchMethodException _) {
      throw new IllegalArgumentException(
          "Abstract mapping value did not have public parameterless constructor " + cls.getName());
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException(cls.getName(), e);
    }
  }

  @SuppressWarnings("unchecked")
  private static void addImmutables(
      Iterable<Spec.Immutable<?>> entries, ClassHierarchyMap<Spec.Immutable<Object>> immutables) {
    for (var entry : entries) immutables.put(entry.type(), (Spec.Immutable<Object>) entry);
  }

  @Override
  public Spec<?> forType(Class<?> type, Field field) {
    var handle = handles.getSubclass(type, ClassHierarchyMap.Find.CLOSEST);
    if (handle == null) {
      if (Modifier.isAbstract(type.getModifiers()))
        throw new IllegalArgumentException("Missing abstract mapping for " + type.getName());

      handle = extractConstructor(type);
    }

    Spec.Immutable<Object> immutable = null;
    if (field.isAnnotationPresent(Immutable.class))
      immutable = immutables.getSubclass(type, ClassHierarchyMap.Find.CLOSEST);

    var finalHandle = handle;
    return new Spec<>(
        () -> {
          try {
            return finalHandle.invoke();
          } catch (Throwable e) {
            throw new RuntimeException(e);
          }
        },
        immutable);
  }
}
