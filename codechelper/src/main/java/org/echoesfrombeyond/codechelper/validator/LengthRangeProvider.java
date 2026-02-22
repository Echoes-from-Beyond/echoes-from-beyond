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
import com.hypixel.hytale.codec.schema.config.ArraySchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import org.echoesfrombeyond.codechelper.annotation.validator.ValidateLengthRange;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** See {@link ValidateLengthRange} for validator behavior. */
@NullMarked
public class LengthRangeProvider implements ValidatorProvider<ValidateLengthRange> {
  /** The singleton instance of this provider. */
  @SuppressWarnings("unused")
  public static final LengthRangeProvider INSTANCE = new LengthRangeProvider();

  private record ValidatorImpl(int min, int max) implements Validator<Object> {
    @Override
    public void accept(@Nullable Object o, ValidationResults validationResults) {
      if (o == null) return;

      boolean ret = false;
      int size =
          switch (o) {
            case Collection<?> collection -> collection.size();
            case Map<?, ?> map -> map.size();
            case String string -> string.length();
            default -> {
              if (o.getClass().isArray()) yield Array.getLength(o);
              ret = true;
              yield -1;
            }
          };

      if (ret) return;
      if (size < this.min || size > this.max)
        validationResults.fail(
            String.format(
                "Length is invalid! Was %s, expected between %s and %s", size, this.min, this.max));
    }

    @Override
    public void updateSchema(SchemaContext schemaContext, Schema schema) {
      var arraySchema = (ArraySchema) schema;
      arraySchema.setMinItems(this.min);
      arraySchema.setMaxItems(this.max);
    }
  }

  private static boolean canProvideFor(Field field) {
    return field.getType().isArray();
  }

  private LengthRangeProvider() {}

  @Override
  public @Nullable Validator<?> getInstance(ValidateLengthRange args, Field field) {
    return canProvideFor(field) ? new ValidatorImpl(args.min(), args.max()) : null;
  }
}
