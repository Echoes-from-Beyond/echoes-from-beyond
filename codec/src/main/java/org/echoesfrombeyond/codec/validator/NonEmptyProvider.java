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

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ArraySchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import org.echoesfrombeyond.codec.annotation.validator.ValidateNonEmpty;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** See {@link ValidateNonEmpty} for validator behavior. */
@NullMarked
public class NonEmptyProvider implements ValidatorProvider<ValidateNonEmpty> {
  /** The singleton instance of this provider. */
  @SuppressWarnings("unused")
  public static final NonEmptyProvider INSTANCE = new NonEmptyProvider();

  private static final Validator<?> VALIDATOR_INSTANCE =
      new Validator<>() {
        @Override
        public void accept(@Nullable Object o, ValidationResults validationResults) {
          if (o == null) return;

          var isEmpty =
              switch (o) {
                case Map<?, ?> map -> map.isEmpty();
                case Collection<?> collection -> collection.isEmpty();
                case String string -> string.isEmpty();
                default -> o.getClass().isArray() && Array.getLength(o) == 0;
              };

          if (isEmpty) validationResults.fail("Container can't be empty!");
        }

        @Override
        public void updateSchema(SchemaContext schemaContext, Schema schema) {
          if (schema instanceof ArraySchema arraySchema) arraySchema.setMinItems(1);
        }
      };

  private static boolean canProvideFor(Field field) {
    var type = field.getType();
    return type.isArray()
        || type.equals(String.class)
        || Collection.class.isAssignableFrom(type)
        || Map.class.isAssignableFrom(type);
  }

  private NonEmptyProvider() {}

  @Override
  public @Nullable Validator<?> getInstance(ValidateNonEmpty ignored, Field field) {
    return canProvideFor(field) ? VALIDATOR_INSTANCE : null;
  }
}
