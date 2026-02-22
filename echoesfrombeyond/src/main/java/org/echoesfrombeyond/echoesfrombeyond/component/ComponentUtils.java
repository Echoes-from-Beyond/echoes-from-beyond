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

package org.echoesfrombeyond.echoesfrombeyond.component;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Utilities for working with {@link Component}s. */
@NullMarked
public final class ComponentUtils {
  private ComponentUtils() {}

  /**
   * Assumes that the provided {@link ArchetypeChunk} contains a component of {@link ComponentType}
   * {@code type} at index {@code i}, and returns it. This is useful in cases when the user
   * definitely knows that the component is present, like when working with a static {@link
   * Archetype}.
   *
   * @param chunk the chunk
   * @param i the index
   * @param type the component type
   * @return the component
   * @param <E> the component type
   * @param <C> the component
   */
  public static <E extends @Nullable Object, C extends @Nullable Component<E>> @NonNull C assume(
      ArchetypeChunk<E> chunk, int i, ComponentType<E, C> type) {
    var component = chunk.getComponent(i, type);
    assert component != null;
    return component;
  }
}
