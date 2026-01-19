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

package org.echoesfrombeyond.sigil;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.VisibleForTesting;
import org.jspecify.annotations.NullMarked;

/**
 * Opaque type created from Sigil canonicalization, designed for use as a key in a {@link HashMap}
 * or similar data structure. {@link Object#toString()} is implemented for ease of debugging, though
 * its format is not specified and is subject to change.
 *
 * <p>The only way to obtain an instance of this class is by calling {@link
 * SigilValidation#canonicalize(byte[])} or through deserializing a previously-serialized instance.
 */
@NullMarked
public final class SigilKey implements Comparable<SigilKey>, Serializable {
  @Serial private static final long serialVersionUID = 7587438460546625759L;

  // Used in place of SigilKey for (de)serialization; just a thin wrapper around a byte array.
  private record Proxy(byte @UnknownNullability [] untrustedPoints) implements Serializable {
    @Serial private static final long serialVersionUID = -6469837533138978770L;

    @Serial
    private Object readResolve() throws InvalidObjectException {
      if (untrustedPoints == null)
        // It would be fine to call `canonicalize` with a null array, because it would just throw a
        // NullPointerException. But this error message is better, keeps the exception type
        // consistent, and prevents static analysis from yelling at us.
        throw new InvalidObjectException("points array shouldn't have been null");

      // Canonicalize the data. If it's invalid, we'll throw an InvalidObjectException.
      return SigilValidation.canonicalize(untrustedPoints)
          .orElseThrow(() -> new InvalidObjectException("points array couldn't be canonicalized"));
    }
  }

  @Serial
  private Object writeReplace() {
    // Always write the Proxy object instead.
    return new Proxy(this.points);
  }

  @Serial
  private void readObject(ObjectInputStream ois) throws InvalidObjectException {
    // Deserialization should only occur through the proxy, as that performs the required
    // validation. Encountering directly-serialized SigilKey objects is always either a bug or
    // "malicious" input.
    throw new InvalidObjectException("proxy is required");
  }

  private final byte[] points;
  private final int hash;

  SigilKey(byte[] points) {
    this.points = points;
    this.hash = Arrays.hashCode(points);
  }

  /**
   * Visible only for unit testing. Checks this key for validity. This method should never return
   * false for a SigilKey obtained from {@link SigilValidation#canonicalize(byte[])}.
   *
   * <p>This check is performed on a best-effort basis and will not detect all invalid cases (it
   * returns {@code true} for some invalid keys). Though, if {@code false}, the key is
   * <i>definitely</i> invalid.
   *
   * @return true if this key is canonical; false otherwise
   */
  @VisibleForTesting
  boolean isCanonical() {
    if (this.points.length < 2) return false;

    for (int i = 0; i < this.points.length; i++) {
      byte first = this.points[i];

      for (int j = i + 1; j < this.points.length; j++) {
        byte second = this.points[j];
        if (first != second || j - i < 3) continue;

        byte small = this.points[i + 1];
        byte large = this.points[j - 1];

        if (Byte.compareUnsigned(small, large) > 0) return false;
      }
    }

    return true;
  }

  /**
   * Visible only for unit testing purposes. Returns a <i>serializable</i> proxy object from the raw
   * byte array. No validation is performed here; any array is valid.
   *
   * <p>This object will additionally deserialize to a {@link SigilKey} rather than its own class.
   * At this step, validation should be performed, resulting in an exception if the {@code bytes}
   * used to construct the proxy were invalid.
   *
   * <p>This is intended to be used to simulate invalid or malicious serialized data by allowing any
   * bytes to be read during deserialization.
   *
   * @param bytes the raw bytes
   * @return the serializable proxy object
   */
  @VisibleForTesting
  static Object serializationProxyFromBytes(byte @UnknownNullability [] bytes) {
    return new Proxy(bytes);
  }

  @Override
  public String toString() {
    return Arrays.toString(points);
  }

  @Override
  public int hashCode() {
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof SigilKey key) && Arrays.equals(points, key.points);
  }

  @Override
  public int compareTo(SigilKey sigilKey) {
    return Arrays.compare(points, sigilKey.points);
  }
}
