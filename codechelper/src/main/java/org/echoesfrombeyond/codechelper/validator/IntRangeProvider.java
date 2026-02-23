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

package org.echoesfrombeyond.codechelper.validator;

import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.codec.validation.validator.RangeValidator;
import java.lang.reflect.Field;
import org.echoesfrombeyond.codechelper.annotation.validator.ValidateIntRange;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** See {@link ValidateIntRange} for validator behavior. */
@NullMarked
public class IntRangeProvider implements ValidatorProvider<ValidateIntRange> {
  /** The singleton instance of this provider. */
  @SuppressWarnings("unused")
  public static final IntRangeProvider INSTANCE = new IntRangeProvider();

  private static boolean canProvideFor(Field field) {
    var type = field.getType();
    return type.equals(int.class) || type.equals(Integer.class);
  }

  private IntRangeProvider() {}

  @Override
  public @Nullable Validator<?> getInstance(ValidateIntRange args, Field field) {
    return canProvideFor(field)
        ? new RangeValidator<>(args.min(), args.max(), args.inclusive())
        : null;
  }
}
