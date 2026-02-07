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

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import java.lang.reflect.Type;
import java.util.Map;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class CodecUtil {
  public static final CodecResolver PRIMITIVE_RESOLVER =
      new CodecResolver() {
        private static final Map<Class<?>, Codec<?>> PRIMITIVE_CODEC_MAP =
            Map.ofEntries(
                Map.entry(boolean.class, Codec.BOOLEAN),
                Map.entry(Boolean.class, Codec.BOOLEAN),
                Map.entry(byte.class, Codec.BYTE),
                Map.entry(Byte.class, Codec.BYTE),
                Map.entry(short.class, Codec.SHORT),
                Map.entry(Short.class, Codec.SHORT),
                Map.entry(int.class, Codec.INTEGER),
                Map.entry(Integer.class, Codec.INTEGER),
                Map.entry(float.class, Codec.FLOAT),
                Map.entry(Float.class, Codec.FLOAT),
                Map.entry(long.class, Codec.LONG),
                Map.entry(Long.class, Codec.LONG),
                Map.entry(double.class, Codec.DOUBLE),
                Map.entry(Double.class, Codec.DOUBLE),
                Map.entry(String.class, Codec.STRING));

        @Override
        public @Nullable Codec<?> resolve(Type type) {
          if (!(type instanceof Class<?> raw)) return null;
          return PRIMITIVE_CODEC_MAP.get(raw);
        }
      };

  private CodecUtil() {}

  public static <T> BuilderCodec<T> modelBuilderCodec(Type model, CodecResolver resolver) {
    return null;
  }
}
