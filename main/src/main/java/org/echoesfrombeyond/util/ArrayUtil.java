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

/** Static utilities relating to arrays. */
public final class ArrayUtil {
  private ArrayUtil() {}

  /**
   * Reverse a subsequence of a byte array.
   *
   * @param array the array to reverse
   * @param startInclusive the start of the sequence to reverse
   * @param count the number of elements to reverse
   */
  public static void reverse(byte[] array, int startInclusive, int count) {
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
