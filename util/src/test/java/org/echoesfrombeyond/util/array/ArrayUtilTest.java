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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ArrayUtilTest {
  @Test
  public void throwsNullPointerExceptionOnNullArray() {
    assertThrows(NullPointerException.class, () -> ArrayUtil.reverse(null, 0, 0));
  }

  @Test
  public void reverseSingleElementArray() {
    byte[] array = new byte[] {42};
    ArrayUtil.reverse(array, 0, 1);
    assertEquals(42, array[0]);
  }

  @Test
  public void reverseEmptyRange() {
    byte[] array = new byte[] {42, 24};
    ArrayUtil.reverse(array, 0, 0);

    assertArrayEquals(new byte[] {42, 24}, array);
  }

  @Test
  public void reverseTwoElements() {
    byte[] array = new byte[] {42, 24};

    ArrayUtil.reverse(array, 0, 2);
    assertArrayEquals(new byte[] {24, 42}, array);
  }

  @Test
  public void reverseOneElementDoesNothing() {
    byte[] array = new byte[] {42, 24};

    ArrayUtil.reverse(array, 0, 0);
    assertArrayEquals(new byte[] {42, 24}, array);
  }

  @Test
  public void reverseFirstHalfOfArray() {
    byte[] array = new byte[] {1, 2, 3, 4};
    ArrayUtil.reverse(array, 0, 2);
    assertArrayEquals(new byte[] {2, 1, 3, 4}, array);
  }

  @Test
  public void reverseLastHalfOfArray() {
    byte[] array = new byte[] {1, 2, 3, 4};
    ArrayUtil.reverse(array, 2, 2);
    assertArrayEquals(new byte[] {1, 2, 4, 3}, array);
  }
}
