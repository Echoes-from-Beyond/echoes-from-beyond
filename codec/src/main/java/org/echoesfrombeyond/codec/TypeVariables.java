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

import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Map;
import org.jspecify.annotations.NullMarked;

/** Static {@link TypeVariable}s. */
@NullMarked
class TypeVariables {
  /** Collection element type variable. */
  static final TypeVariable<?> COLLECTION_ELEMENT_TYPE;

  /** Map key type variable. */
  static final TypeVariable<?> MAP_KEY_TYPE;

  /** Map value type variable. */
  static final TypeVariable<?> MAP_VALUE_TYPE;

  static {
    var collectionParams = Collection.class.getTypeParameters();
    var mapParams = Map.class.getTypeParameters();

    COLLECTION_ELEMENT_TYPE = collectionParams[0];
    MAP_KEY_TYPE = mapParams[0];
    MAP_VALUE_TYPE = mapParams[1];
  }
}
