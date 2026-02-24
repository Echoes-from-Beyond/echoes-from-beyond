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

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A map-like data structure with {@link Class} keys. Supports queries to find values based on their
 * relation to a base class or superclass in the inheritance tree:
 *
 * <p>
 *
 * {@snippet lang=java :
 * ClassHierarchyMap<String> example = new HashClassHierarchyMap<>();
 * example.put(List.class, "List");
 *
 * // Since List subclasses Collection, closest.equals("List")
 * String closest = example.getSubclass(Collection.class, Find.CLOSEST);
 *
 * example.put(Collection.class, "Collection");
 *
 * // Now, closest.equals("Collection"), because Collection is closer to itself than it is to List
 * String closest = example.getSubclass(Collection.class, Find.CLOSEST);
 * }
 *
 * @param <V> the (non-null) value type
 */
@NullMarked
public sealed interface ClassHierarchyMap<V> permits HashClassHierarchyMap {
  /** Specifies the kind of query operation to perform on the map. */
  enum Find {
    /**
     * Find the closest subclass key, as determined by {@link TypeUtil#inheritanceDistance(Class,
     * Class)}. Exact matches are considered maximally close.
     */
    CLOSEST,

    /**
     * Find the furthest subclass key as determined by {@link TypeUtil#inheritanceDistance(Class,
     * Class)}.
     */
    FURTHEST,

    /**
     * Find only an exact mapping. When using this query type, {@link
     * ClassHierarchyMap#getSubclass(Class, Find)} and {@link ClassHierarchyMap#getSuperclass(Class,
     * Find)} work identically.
     */
    EXACT
  }

  /**
   * Find an entry whose key is assignable from {@code baseClass}, and return its associated value.
   *
   * @param baseClass the base class
   * @param find the query type
   * @return the associated value, or {@code null} if there is no matching entry
   */
  @Nullable V getSuperclass(Class<?> baseClass, Find find);

  /**
   * Find an entry such that {@code superClass} is assignable from the entry key, and return its
   * associated value.
   *
   * @param superClass the superclass
   * @param find the query type
   * @return the associated value, or {@code null} if there is no matching entry
   */
  @Nullable V getSubclass(Class<?> superClass, Find find);

  /**
   * Add an entry to this map.
   *
   * @param key the key type
   * @param value the value type
   * @return the old value that was previously associated with {@code key}, or {@code null} if there
   *     was none
   * @throws NullPointerException if {@code key} or {@code value} are null
   */
  @Contract("_, null -> fail")
  @Nullable V put(Class<?> key, V value);

  /**
   * Removes a value that was previously added by {@link ClassHierarchyMap#put(Class, Object)}.
   *
   * @param key the key type
   * @return the old value that was previously associated with {@code key}, or {@code null} if there
   *     was none
   */
  @Nullable V remove(Class<?> key);
}
