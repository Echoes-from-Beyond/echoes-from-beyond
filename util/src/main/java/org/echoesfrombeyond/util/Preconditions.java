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

package org.echoesfrombeyond.util;

import java.lang.reflect.Array;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.VisibleForTesting;

/** Static input validation methods. */
public final class Preconditions {
  /**
   * Range check internals used by {@link Preconditions#inBounds(Object, int, int)}.
   *
   * @param arrayLen the array length
   * @param index the index into the array, inclusive
   * @param len the number of elements in the range
   */
  @VisibleForTesting
  static void checkInRange(
      @Range(from = 0, to = Integer.MAX_VALUE) int arrayLen, int index, int len) {
    if (index < 0 || index >= arrayLen) throw new ArrayIndexOutOfBoundsException(index);

    int toIndexExclusive = index + len;
    if (toIndexExclusive < index || toIndexExclusive > arrayLen)
      throw new IllegalArgumentException(
          "index " + index + " length " + len + " for array length " + arrayLen);
  }

  /**
   * Checks that {@code start} and {@code len} form a subsequence that is in range for {@code
   * array}.
   *
   * @param array the array object
   * @param start the start index
   * @param len the length of the subsequence
   * @throws NullPointerException if {@code array} is null
   * @throws ArrayIndexOutOfBoundsException if {@code start} is outside the bounds of {@code array}
   * @throws IllegalArgumentException if {@code array} is not an array type
   * @throws IllegalArgumentException if {@code start} is valid but {@code start + len} is outside
   *     the bounds of {@code array}
   */
  @Contract("null, _, _ -> fail")
  public static void inBounds(
      Object array,
      @Range(from = 0, to = Integer.MAX_VALUE) int start,
      @Range(from = 0, to = Integer.MAX_VALUE) int len) {
    checkInRange(Array.getLength(array), start, len);
  }
}
