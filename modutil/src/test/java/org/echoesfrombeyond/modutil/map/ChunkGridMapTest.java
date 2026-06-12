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

package org.echoesfrombeyond.modutil.map;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.stream.Stream;
import org.joml.Vector3i;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ChunkGridMapTest {
  public static Stream<Arguments> coordinates() {
    var list = new ArrayList<Vector3i>();

    for (int offset = 0; offset <= 64; offset += 32) {
      for (int x = -2; x <= 2; x++) {
        for (int y = -2; y <= 2; y++) {
          for (int z = -2; z <= 2; z++) {
            list.add(new Vector3i(offset + x, offset + y, offset + z));
          }
        }
      }
    }

    return list.stream().map(vec -> Arguments.of(vec.x, vec.y, vec.z));
  }

  @ParameterizedTest
  @MethodSource("coordinates")
  public void basicWriteRead(int x, int y, int z) {
    var map = new ChunkGridMap<String>();

    var value = x + ", " + y + ", " + z;
    map.put(new Vector3i(x, y, z), value);

    assertEquals(value, map.get(new Vector3i(x, y, z)));
  }

  @ParameterizedTest
  @MethodSource("coordinates")
  public void missingReadReturnsNull(int x, int y, int z) {
    var map = new ChunkGridMap<String>();

    map.put(new Vector3i(x, y, z), "test");

    assertNull(map.get(new Vector3i(x + 1, y, z)));
    assertNull(map.get(new Vector3i(x, y + 1, z)));
    assertNull(map.get(new Vector3i(x, y, z + 1)));

    assertNull(map.get(new Vector3i(x - 1, y, z)));
    assertNull(map.get(new Vector3i(x, y, z - 1)));
  }

  @ParameterizedTest
  @MethodSource("coordinates")
  public void removeRemovesElement(int x, int y, int z) {
    var map = new ChunkGridMap<String>();

    map.put(new Vector3i(x, y, z), "value");
    assertEquals("value", map.remove(new Vector3i(x, y, z)));

    assertNull(map.get(new Vector3i(x, y, z)));
  }

  public static Stream<Arguments> forEachInRangeVisitsAppropriateValuesOffsets() {
    var list = new ArrayList<Vector3i>();

    for (int x = -64; x <= 64; x += 16)
      for (int y = 0; y <= 128; y += 16)
        for (int z = -64; z <= 64; z += 16) list.add(new Vector3i(x, y == 0 ? 1 : y, z));

    return list.stream().map(vec -> Arguments.of(vec.x, vec.y, vec.z));
  }

  @Test
  public void forEachInRangeVisitsNoValuesWhenEmpty() {
    new ChunkGridMap<String>().forEachInRange(new Vector3i(0, 0, 0), 10, (_, _) -> fail());
  }

  @ParameterizedTest
  @MethodSource("forEachInRangeVisitsAppropriateValuesOffsets")
  public void forEachInRangeVisitsAppropriateValues(int x, int y, int z) {
    var map = new ChunkGridMap<String>();

    map.put(new Vector3i(0, 0, 0).add(x, y, z), "in range");
    map.put(new Vector3i(1, 0, 0).add(x, y, z), "in range");
    map.put(new Vector3i(0, 1, 0).add(x, y, z), "in range");
    map.put(new Vector3i(0, 0, 1).add(x, y, z), "in range");

    map.put(new Vector3i(-1, 0, 0).add(x, y, z), "in range");
    map.put(new Vector3i(0, -1, 0).add(x, y, z), "in range");
    map.put(new Vector3i(0, 0, -1).add(x, y, z), "in range");

    map.put(new Vector3i(1, 1, 0).add(x, y, z), "out of range");
    map.put(new Vector3i(1, 0, 1).add(x, y, z), "out of range");
    map.put(new Vector3i(0, 1, 1).add(x, y, z), "out of range");
    map.put(new Vector3i(1, 1, 1).add(x, y, z), "out of range");

    map.put(new Vector3i(-1, 1, 0).add(x, y, z), "out of range");
    map.put(new Vector3i(1, -1, 0).add(x, y, z), "out of range");
    map.put(new Vector3i(-1, -1, 0).add(x, y, z), "out of range");

    map.put(new Vector3i(-1, 0, 1).add(x, y, z), "out of range");
    map.put(new Vector3i(1, 0, -1).add(x, y, z), "out of range");
    map.put(new Vector3i(-1, 0, -1).add(x, y, z), "out of range");

    map.put(new Vector3i(0, -1, 1).add(x, y, z), "out of range");
    map.put(new Vector3i(0, 1, -1).add(x, y, z), "out of range");
    map.put(new Vector3i(0, -1, -1).add(x, y, z), "out of range");

    map.put(new Vector3i(-1, 1, 1).add(x, y, z), "out of range");
    map.put(new Vector3i(1, -1, 1).add(x, y, z), "out of range");
    map.put(new Vector3i(1, 1, -1).add(x, y, z), "out of range");
    map.put(new Vector3i(-1, 1, -1).add(x, y, z), "out of range");
    map.put(new Vector3i(1, -1, -1).add(x, y, z), "out of range");
    map.put(new Vector3i(-1, -1, -1).add(x, y, z), "out of range");

    var results = new ArrayList<String>();
    map.forEachInRange(new Vector3i(0, 0, 0).add(x, y, z), 1, (_, data) -> results.add(data));

    assertEquals(7, results.size());
    for (var result : results) assertEquals("in range", result);
  }
}
