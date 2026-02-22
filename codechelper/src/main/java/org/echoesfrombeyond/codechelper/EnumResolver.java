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
import com.hypixel.hytale.codec.codecs.EnumCodec;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import org.echoesfrombeyond.util.type.TypeUtil;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Resolver for {@link EnumCodec}. */
@NullMarked
class EnumResolver implements CodecResolver {
  private final EnumCodec.EnumStyle style;

  /**
   * Creates a new instance of this class, which uses the specified style to interpret enum
   * constants.
   *
   * @param style the enum constant style
   */
  EnumResolver(EnumCodec.EnumStyle style) {
    this.style = style;
  }

  // suppression isn't redundant, IntelliJ misses the unsafe operation (javac doesn't)
  @Override
  @SuppressWarnings({"unchecked", "RedundantSuppression"})
  public @Nullable Codec<?> resolve(Type type, Field field) {
    var raw = TypeUtil.getRawType(type);
    if (raw == null || !raw.isEnum()) return null;

    return new EnumCodec<>(raw.asSubclass(Enum.class), style);
  }
}
