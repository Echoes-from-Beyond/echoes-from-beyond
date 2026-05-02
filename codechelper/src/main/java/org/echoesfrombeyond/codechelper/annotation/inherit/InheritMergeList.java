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

package org.echoesfrombeyond.codechelper.annotation.inherit;

import java.lang.annotation.*;
import org.echoesfrombeyond.codechelper.inherit.ListMerger;
import org.jspecify.annotations.NullMarked;

/**
 * When inheriting from a parent codec, merges the values from the parent with the values from the
 * child. Works only for field types assignable to List.
 */
@InheritSpec(ListMerger.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NullMarked
public @interface InheritMergeList {
  /**
   * Specifies the index at which the parent values should be inserted. This value is clamped to the
   * actual size of the list and so cannot cause out of bounds errors. Set to a very large value
   * like {@link Integer#MAX_VALUE} to place parent values at the very end of the resulting list.
   * Set to a negative value (or 0) to place parent values at the beginning of the list.
   *
   * <p>Defaults to 0.
   *
   * @return the index where merges should occur
   */
  int mergeAt() default 0;
}
