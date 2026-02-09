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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class HashClassHierarchyMapTest {
  interface Aif {}

  interface Bif extends Aif {}

  static class A implements Aif {}

  static class B extends A implements Bif {}

  static class C extends B {}

  static class Z {}

  @Test
  public void exactFind() {
    var map = new HashClassHierarchyMap<String>();
    map.put(Z.class, "Z");

    var first = map.getSubclass(Z.class, ClassHierarchyMap.Find.EXACT);
    var second = map.getSuperclass(Z.class, ClassHierarchyMap.Find.EXACT);

    assertEquals("Z", first);
    assertEquals("Z", second);
  }

  @Test
  public void closestSubclass() {
    var map = new HashClassHierarchyMap<String>();
    map.put(B.class, "B");
    map.put(C.class, "C");

    var closest = map.getSubclass(A.class, ClassHierarchyMap.Find.CLOSEST);
    assertEquals("B", closest);
  }

  @Test
  public void furthestSubclass() {
    var map = new HashClassHierarchyMap<String>();
    map.put(B.class, "B");
    map.put(C.class, "C");

    var furthest = map.getSubclass(A.class, ClassHierarchyMap.Find.FURTHEST);
    assertEquals("C", furthest);
  }

  @Test
  public void furthestSuperclass() {
    var map = new HashClassHierarchyMap<String>();
    map.put(A.class, "A");
    map.put(B.class, "B");

    var furthest = map.getSuperclass(C.class, ClassHierarchyMap.Find.FURTHEST);
    assertEquals("A", furthest);
  }

  @Test
  public void closestSuperclass() {
    var map = new HashClassHierarchyMap<String>();
    map.put(A.class, "A");
    map.put(B.class, "B");

    var closest = map.getSuperclass(C.class, ClassHierarchyMap.Find.CLOSEST);
    assertEquals("B", closest);
  }

  @Test
  public void putReturnsOldValue() {
    var map = new HashClassHierarchyMap<String>();
    var old = map.put(Z.class, "old");
    assertNull(old);
    old = map.put(Z.class, "new");
    assertEquals("old", old);
  }

  @Test
  public void removeReturnsRemovedValue() {
    var map = new HashClassHierarchyMap<String>();
    assertNull(map.remove(Z.class));

    map.put(Z.class, "old");
    assertEquals("old", map.remove(Z.class));

    assertNull(map.remove(Z.class));
  }

  @Test
  public void putNullThrows() {
    var map = new HashClassHierarchyMap<String>();
    assertThrows(NullPointerException.class, () -> map.put(Z.class, null));
  }
}
