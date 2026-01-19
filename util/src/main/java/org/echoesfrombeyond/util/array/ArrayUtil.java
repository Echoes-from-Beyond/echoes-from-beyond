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

package org.echoesfrombeyond.util.array;

import org.echoesfrombeyond.util.Check;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NullMarked;

/** Static utilities relating to arrays. */
@NullMarked
public final class ArrayUtil {
  private ArrayUtil() {}

  /**
   * Reverse a subsequence of a byte array.
   *
   * @param array the array to reverse
   * @param startInclusive the start of the sequence to reverse
   * @param count the number of elements to reverse
   * @throws NullPointerException if {@code array} is {@code null}
   * @throws IndexOutOfBoundsException if {@code startInclusive} is outside the bounds of {@code
   *     array}
   * @throws IllegalArgumentException if {@code startInclusive} is valid but {@code startInclusive +
   *     count} is outside the bounds of the array
   */
  @Contract(mutates = "param1")
  public static void reverse(
      byte[] array,
      @Range(from = 0, to = Integer.MAX_VALUE) int startInclusive,
      @Range(from = 0, to = Integer.MAX_VALUE) int count) {
    Check.inBounds(array, startInclusive, count);

    int endExclusive = startInclusive + count;

    for (int i = 0; i < count / 2; i++) {
      int firstIndex = startInclusive + i;
      int secondIndex = (endExclusive - i - 1);

      byte temp = array[firstIndex];

      array[firstIndex] = array[secondIndex];
      array[secondIndex] = temp;
    }
  }
}
