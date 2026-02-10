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

package org.echoesfrombeyond.util.type;

import java.util.HashMap;
import java.util.Map;
import org.echoesfrombeyond.util.Check;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class HashClassHierarchyMap<V> implements ClassHierarchyMap<V> {
  private final Map<Class<?>, V> inner;

  public HashClassHierarchyMap() {
    this.inner = new HashMap<>();
  }

  @Override
  public V getSuperclass(Class<?> baseClass, Find find) {
    if (find == Find.EXACT) return inner.get(baseClass);

    if (find == Find.CLOSEST) {
      var direct = inner.get(baseClass);
      if (direct != null) return direct;
    }

    V value = null;
    for (var candidate : TypeUtil.traverseHierarchy(baseClass, Object.class)) {
      var candidateValue = inner.get(candidate);
      if (candidateValue == null) continue;

      if (find == Find.CLOSEST) return candidateValue;
      else value = candidateValue;
    }

    return value;
  }

  @Override
  public V getSubclass(Class<?> superClass, Find find) {
    if (find == Find.EXACT) return inner.get(superClass);

    var bestDistance = find == Find.FURTHEST ? Integer.MIN_VALUE : Integer.MAX_VALUE;
    V bestValue = null;

    for (var entry : inner.entrySet()) {
      var key = entry.getKey();
      var value = entry.getValue();

      var distance = TypeUtil.inheritanceDistance(key, superClass);
      if (distance < 0) continue;

      if (find == Find.FURTHEST ? distance > bestDistance : distance < bestDistance) {
        bestDistance = distance;
        bestValue = value;
      }
    }

    return bestValue;
  }

  @Override
  public V put(Class<?> key, V value) {
    return inner.put(key, Check.nonNull(value));
  }

  @Override
  public V remove(Class<?> key) {
    return inner.remove(key);
  }
}
