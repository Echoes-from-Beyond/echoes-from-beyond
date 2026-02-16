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

package org.echoesfrombeyond.codechelper;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.IntFunction;
import org.echoesfrombeyond.util.type.TypeUtil;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Internal implementation supporting resolution of N-dimensional arrays, including primitive
 * arrays.
 */
@NullMarked
class ArrayResolver implements CodecResolver {
  private static final Map<Class<?>, Codec<?>> PRIMITIVE_ARRAY_CODECS =
      Map.ofEntries(
          Map.entry(boolean.class, CodecUtil.BOOLEAN_ARRAY_CODEC),
          Map.entry(byte.class, CodecUtil.BYTE_ARRAY_CODEC),
          Map.entry(short.class, CodecUtil.SHORT_ARRAY_CODEC),
          Map.entry(char.class, CodecUtil.CHAR_ARRAY_CODEC),
          Map.entry(int.class, Codec.INT_ARRAY),
          Map.entry(float.class, Codec.FLOAT_ARRAY),
          Map.entry(long.class, Codec.LONG_ARRAY),
          Map.entry(double.class, Codec.DOUBLE_ARRAY));

  private final CodecResolver root;

  /**
   * @param root the root resolver, which will be used to resolve the component type codec
   */
  ArrayResolver(CodecResolver root) {
    this.root = root;
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public @Nullable Codec<?> resolve(Type type, Field field) {
    var componentType = TypeUtil.getArrayComponentType(type);
    if (componentType == null) return null;

    var rawComponentType = TypeUtil.getRawType(componentType);
    if (rawComponentType == null) return null;
    if (rawComponentType.isPrimitive()) return PRIMITIVE_ARRAY_CODECS.get(rawComponentType);

    var componentCodec = (Codec<Object>) root.resolve(componentType, field);
    if (componentCodec == null) return null;
    return new ArrayCodec<>(
        componentCodec, (IntFunction) value -> Array.newInstance(rawComponentType, value));
  }
}
