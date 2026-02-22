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

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import java.lang.reflect.Field;
import java.util.regex.Pattern;
import org.echoesfrombeyond.codechelper.annotation.validator.ValidateRegex;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** See {@link ValidateRegex} for validator behavior. */
@NullMarked
public class RegexProvider implements ValidatorProvider<ValidateRegex> {
  /** The singleton instance of this provider. */
  @SuppressWarnings("unused")
  public static final RegexProvider INSTANCE = new RegexProvider();

  private static boolean canProvideFor(Field field) {
    return field.getType().equals(String.class);
  }

  private record ValidatorImpl(Pattern pattern) implements Validator<String> {
    @Override
    public void accept(String s, ValidationResults validationResults) {
      if (pattern.matcher(s).matches()) return;

      validationResults.fail(String.format("String was invalid! Must match the regex %s", pattern));
    }

    @Override
    public void updateSchema(SchemaContext schemaContext, Schema schema) {
      ((StringSchema) schema).setPattern(pattern);
    }
  }

  private RegexProvider() {}

  @Override
  public @Nullable Validator<?> getInstance(ValidateRegex args, Field field) {
    return canProvideFor(field) ? new ValidatorImpl(Pattern.compile(args.value())) : null;
  }
}
