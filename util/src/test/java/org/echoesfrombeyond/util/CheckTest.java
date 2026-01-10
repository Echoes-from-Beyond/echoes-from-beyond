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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CheckTest {
  @Test
  public void emptyRangeIsValid() {
    byte[] array = new byte[] {42};
    Check.inBounds(array, 0, 0);
  }

  @Test
  public void emptyRangeOnEmptyArrayIsInvalid() {
    byte[] array = new byte[] {};
    assertThrows(IndexOutOfBoundsException.class, () -> Check.inBounds(array, 0, 0));
  }

  @Test
  public void fullLengthIsValid() {
    byte[] array = new byte[] {1, 2, 3};
    Check.inBounds(array, 0, array.length);
  }

  @Test
  public void fullLengthPlusOneIsInvalid() {
    byte[] array = new byte[] {1, 2, 3};
    assertThrows(IllegalArgumentException.class, () -> Check.inBounds(array, 0, array.length + 1));
  }

  @Test
  public void largeNegativeLenIsInvalid() {
    assertThrows(
        IllegalArgumentException.class,
        () -> Check.inRange(Integer.MAX_VALUE, Integer.MAX_VALUE - 1, Integer.MIN_VALUE));
  }

  @Test
  public void smallNegativeLenIsInvalid() {
    assertThrows(IllegalArgumentException.class, () -> Check.inRange(10, 5, -1));
  }
}
