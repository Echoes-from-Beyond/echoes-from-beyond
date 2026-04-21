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
import java.util.List;
import org.echoesfrombeyond.codechelper.annotation.inherit.InheritMergeList;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class ListMerger implements InheritMergerProvider<InheritMergeList> {
  private static boolean isValidFor(Field field) {
    return List.class.isAssignableFrom(field.getType());
  }

  @Override
  public Class<InheritMergeList> getArgsType() {
    return InheritMergeList.class;
  }

  private static class MergerImpl implements InheritMerger<List<Object>> {
    private final int index;

    private MergerImpl(int index) {
      this.index = index;
    }

    @Override
    public @Nullable List<Object> merge(
        @Nullable List<Object> value, @Nullable List<Object> parentValue) {
      if (value == null || parentValue == null) return value;

      value.addAll(Math.min(Math.max(index, 0), value.size()), parentValue);
      return value;
    }
  }

  @Override
  public @Nullable InheritMerger<?> getInstance(InheritMergeList inheritMergeList, Field field) {
    return isValidFor(field) ? new MergerImpl(inheritMergeList.mergeAt()) : null;
  }
}
