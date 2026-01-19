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
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import org.echoesfrombeyond.asset.SigilPattern;
import org.echoesfrombeyond.sigil.SigilValidation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Intermediate data class used during {@link SigilPattern} deserialization.
 *
 * @param x the sigil x-coordinate
 * @param y the sigil y-coordinate
 */
@ApiStatus.Internal
@NullMarked
public record SigilPoint(
    @Range(from = 0, to = SigilValidation.GRID_SIZE - 1) int x,
    @Range(from = 0, to = SigilValidation.GRID_SIZE - 1) int y) {
  /** The codec. */
  public static final Codec<SigilPoint> CODEC = new SigilPointCodec();

  /** The array codec. */
  public static final Codec<SigilPoint[]> ARRAY_CODEC = new ArrayCodec<>(CODEC, SigilPoint[]::new);

  /**
   * Converts an array of {@link SigilPoint} to a byte array, as per {@link
   * SigilValidation#encodePoint(int, int)}.
   *
   * @param objects the point array
   * @return the encoded byte array
   * @see SigilPoint#decodeArray(byte[]) for the inverse of this method
   */
  @Contract(value = "null -> null; !null -> !null", pure = true)
  public static byte @Nullable [] encodeArray(SigilPoint @Nullable [] objects) {
    if (objects == null) return null;

    byte[] points = new byte[objects.length];
    for (int i = 0; i < objects.length; i++) {
      SigilPoint point = objects[i];
      points[i] = SigilValidation.encodePoint(point.x(), point.y());
    }

    return points;
  }

  /**
   * Converts a byte array into an array of {@link SigilPoint}, as per {@link
   * SigilValidation#unpackX(byte)} and {@link SigilValidation#unpackY(byte)}.
   *
   * @param bytes the byte array
   * @return the decoded point array
   * @see SigilPoint#encodeArray(SigilPoint[]) for the inverse of this method
   */
  @Contract(value = "null -> null; !null -> !null", pure = true)
  public static SigilPoint @Nullable [] decodeArray(byte @Nullable [] bytes) {
    if (bytes == null) return null;

    SigilPoint[] objects = new SigilPoint[bytes.length];
    for (int i = 0; i < bytes.length; i++) {
      byte point = bytes[i];
      objects[i] = new SigilPoint(SigilValidation.unpackX(point), SigilValidation.unpackY(point));
    }

    return objects;
  }
}
