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
import com.hypixel.hytale.codec.validation.validator.RequiredMapKeysValidator;
import java.lang.reflect.Field;
import java.util.Map;
import org.echoesfrombeyond.codec.TypeVariables;
import org.echoesfrombeyond.codec.annotation.validator.ValidateRequiredMapKeys;
import org.echoesfrombeyond.util.type.TypeUtil;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** See {@link ValidateRequiredMapKeys} for validator behavior. */
@NullMarked
public class RequiredMapKeysProvider implements ValidatorProvider<ValidateRequiredMapKeys> {
  /** The singleton instance of this provider. */
  @SuppressWarnings("unused")
  public static final RequiredMapKeysProvider INSTANCE = new RequiredMapKeysProvider();

  private static boolean canProvideFor(Field field) {
    if (!Map.class.isAssignableFrom(field.getType())) return false;

    var parameters = TypeUtil.resolveSupertypeParameters(field.getGenericType(), Map.class);

    // We already checked assignability
    assert parameters != null;

    return String.class.equals(parameters.get(TypeVariables.MAP_KEY_TYPE));
  }

  private RequiredMapKeysProvider() {}

  @Override
  public @Nullable Validator<?> getInstance(ValidateRequiredMapKeys args, Field field) {
    return canProvideFor(field) ? new RequiredMapKeysValidator<>(args.value()) : null;
  }
}
