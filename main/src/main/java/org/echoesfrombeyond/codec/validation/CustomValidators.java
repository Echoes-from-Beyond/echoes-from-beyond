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

package org.echoesfrombeyond.codec.validation;

import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.codec.validation.Validators;
import org.echoesfrombeyond.codec.SigilPoint;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;

/** Equivalent to {@link Validators} but for our own custom codecs. */
@NullMarked
public final class CustomValidators {
  private CustomValidators() {}

  /**
   * @return a validator that checks if a Sigil is canonicalizable.
   */
  @Contract(pure = true)
  public static Validator<SigilPoint[]> canonicalSigil() {
    return CanonicalSigilValidator.INSTANCE;
  }
}
