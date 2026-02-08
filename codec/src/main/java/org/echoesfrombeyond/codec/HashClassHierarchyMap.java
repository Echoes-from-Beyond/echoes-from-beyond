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

import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class HashClassHierarchyMap<V> implements ClassHierarchyMap<V> {
  private final Map<Class<?>, V> inner;

  public HashClassHierarchyMap() {
    this.inner = new HashMap<>();
  }

  @Override
  public V getSuperclass(Class<?> key, Find find) {
    if (find == Find.EXACT) return inner.get(key);

    if (find == Find.CLOSEST) {
      var direct = inner.get(key);
      if (direct != null) return direct;
    }

    V value = null;
    for (var candidate : GenericUtil.traverseHierarchy(key, Object.class)) {
      value = inner.get(candidate);
      if (find == Find.CLOSEST && value != null) return value;
    }

    return value;
  }

  @Override
  public V getSubclass(Class<?> superKey, Find find) {
    if (find == Find.EXACT) return inner.get(superKey);

    var bestDistance = find == Find.FURTHEST ? Integer.MIN_VALUE : Integer.MAX_VALUE;
    V bestValue = null;

    for (var entry : inner.entrySet()) {
      var key = entry.getKey();
      var value = entry.getValue();

      var distance = GenericUtil.hierarchyDistance(key, superKey);
      if (distance < 0) continue;

      if (find == Find.FURTHEST ? distance > bestDistance : distance < bestDistance) {
        bestDistance = distance;
        bestValue = value;
      }
    }

    return bestValue;
  }

  @Override
  public void put(Class<?> key, V value) {
    inner.put(key, value);
  }

  @Override
  public void remove(Class<?> key) {
    inner.remove(key);
  }
}
