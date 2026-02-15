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

package org.echoesfrombeyond.codec.validator;

import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.codec.validation.Validators;
import java.lang.reflect.Field;
import org.echoesfrombeyond.codec.annotation.validator.ValidateNonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** See {@link ValidateNonNull} for validator behavior. */
@NullMarked
public class NonNullProvider implements ValidatorProvider<ValidateNonNull> {
  /** The singleton instance of this provider. */
  @SuppressWarnings("unused")
  public static final NonNullProvider INSTANCE = new NonNullProvider();

  private static boolean canProvideFor(Field field) {
    return !field.getType().isPrimitive();
  }

  private NonNullProvider() {}

  @Override
  public @Nullable Validator<?> getInstance(ValidateNonNull args, Field field) {
    return canProvideFor(field) ? Validators.nonNull() : null;
  }
}
