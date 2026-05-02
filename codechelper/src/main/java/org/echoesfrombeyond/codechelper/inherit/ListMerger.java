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

package org.echoesfrombeyond.codechelper.inherit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.echoesfrombeyond.codechelper.annotation.inherit.InheritMergeList;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Provider for {@link InheritMergeList}. */
@NullMarked
public class ListMerger implements InheritMergerProvider<InheritMergeList> {
  /** Singleton instance of this class. */
  @SuppressWarnings("unused")
  public static final ListMerger INSTANCE = new ListMerger();

  private static boolean isValidFor(Field field) {
    return List.class.isAssignableFrom(field.getType());
  }

  private ListMerger() {}

  @Override
  public Class<InheritMergeList> getArgsType() {
    return InheritMergeList.class;
  }

  private record MergerImpl(int index) implements InheritMerger<List<Object>> {
    @Override
    @SuppressWarnings("unchecked")
    public @Nullable List<Object> merge(
        @Nullable List<Object> defaultValue, @Nullable List<Object> parentValue) {
      if (parentValue == null || parentValue.isEmpty()) return defaultValue;

      // if value is null, it should have the same type as the parent value
      if (defaultValue == null) {
        try {
          var newList =
              (List<Object>) parentValue.getClass().getDeclaredConstructor().newInstance();
          newList.addAll(parentValue);
          return newList;
        } catch (NoSuchMethodException
            | InvocationTargetException
            | InstantiationException
            | IllegalAccessException _) {
          return null;
        }
      }

      defaultValue.addAll(Math.min(Math.max(index, 0), defaultValue.size()), parentValue);
      return defaultValue;
    }
  }

  @Override
  public @Nullable InheritMerger<?> getInstance(InheritMergeList inheritMergeList, Field field) {
    return isValidFor(field) ? new MergerImpl(inheritMergeList.mergeAt()) : null;
  }
}
