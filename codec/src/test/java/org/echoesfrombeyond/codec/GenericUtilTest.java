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

import java.util.List;
import org.junit.jupiter.api.Test;

class GenericUtilTest {
  public static List<List<String>> testRecursive;

  @Test
  void test() throws NoSuchFieldException {
    var resolver = CodecUtil.PRIMITIVE_RESOLVER.withListSupport();
    var codec =
        resolver.resolve(GenericUtilTest.class.getDeclaredField("testRecursive").getGenericType());

    System.out.println(codec);
  }
}
