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

package org.echoesfrombeyond.echoesfrombeyond.codec;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import org.echoesfrombeyond.echoesfrombeyond.asset.SigilPattern;
import org.echoesfrombeyond.echoesfrombeyond.sigil.SigilValidation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Intermediate data class used during {@link SigilPattern} deserialization and elsewhere to store
 * parts of a Sigil.
 *
 * @param x the sigil x-coordinate
 * @param y the sigil y-coordinate
 */
@NullMarked
public record SigilPoint(int x, int y) {
  /** The codec. */
  public static final Codec<SigilPoint> CODEC = new SigilPointCodec();

  /** The array codec. */
  public static final Codec<SigilPoint[]> ARRAY_CODEC = new ArrayCodec<>(CODEC, SigilPoint[]::new);

  /** The point (0, 0) */
  public static final SigilPoint ZERO = new SigilPoint(0, 0);

  /**
   * Determines if one point is adjacent to another. Points are considered adjacent if they are not
   * the same point, but do not differ by more than 1 unit on either axis. For example, (0, 0) and
   * (1, 1) are adjacent; (0, 1) and (0, 2) are adjacent, but (0, 0) and (0, 2) are not.
   *
   * @param other the other point to consider
   * @return {@code true} if this point is adjacent to {@code other}, {@code false} otherwise
   */
  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public boolean isAdjacentTo(SigilPoint other) {
    int diffX = Math.abs(x - other.x);
    int diffY = Math.abs(y - other.y);

    return (!(diffX == 0 && diffY == 0)) && diffX <= 1 && diffY <= 1;
  }

  /**
   * @return {@code true} if this point is in-bounds of the Sigil grid; {@code false} otherwise
   */
  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public boolean isInBounds() {
    return x >= 0 && x < SigilValidation.GRID_SIZE && y >= 0 && y < SigilValidation.GRID_SIZE;
  }

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

    var points = new byte[objects.length];
    for (int i = 0; i < objects.length; i++) {
      var point = objects[i];
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

    var objects = new SigilPoint[bytes.length];
    for (int i = 0; i < bytes.length; i++) {
      byte point = bytes[i];
      objects[i] = new SigilPoint(SigilValidation.unpackX(point), SigilValidation.unpackY(point));
    }

    return objects;
  }
}
