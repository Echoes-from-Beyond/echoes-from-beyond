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

package org.echoesfrombeyond.codechelper.inherit;

import com.hypixel.hytale.codec.exception.CodecException;
import java.lang.reflect.Field;
import org.echoesfrombeyond.codechelper.annotation.inherit.InheritMathOp;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Provider for {@link InheritMathOp}. */
@NullMarked
@ApiStatus.Internal
public class InheritMathOpProvider implements InheritMergerProvider<InheritMathOp> {
  /** Singleton instance of this class. */
  @SuppressWarnings("unused")
  public static final InheritMathOpProvider INSTANCE = new InheritMathOpProvider();

  private static boolean canProvideFor(Field field) {
    var type = field.getType();
    return (type.isPrimitive() && !type.equals(boolean.class) && !type.equals(char.class))
        || Number.class.isAssignableFrom(field.getType());
  }

  private InheritMathOpProvider() {}

  private record Provider(InheritMathOp.Op op) implements InheritMerger<Object> {
    @Override
    public @Nullable Object merge(@Nullable Object value, @Nullable Object parentValue) {
      if (value == null && parentValue == null) return null;
      if (value == null ^ parentValue == null) return value == null ? parentValue : value;

      var valueNumber = (Number) value;
      var parentNumber = (Number) parentValue;

      assert valueNumber.getClass().equals(parentNumber.getClass());

      return switch (value) {
        case Byte other ->
            switch (op) {
              case ADD -> parentNumber.byteValue() + other;
              case SUBTRACT -> parentNumber.byteValue() - other;
              case DIVIDE -> parentNumber.byteValue() / other;
              case MULTIPLY -> parentNumber.byteValue() * other;
            };
        case Short other ->
            switch (op) {
              case ADD -> parentNumber.shortValue() + other;
              case SUBTRACT -> parentNumber.shortValue() - other;
              case DIVIDE -> parentNumber.shortValue() / other;
              case MULTIPLY -> parentNumber.shortValue() * other;
            };
        case Integer other ->
            switch (op) {
              case ADD -> parentNumber.intValue() + other;
              case SUBTRACT -> parentNumber.intValue() - other;
              case DIVIDE -> parentNumber.intValue() / other;
              case MULTIPLY -> parentNumber.intValue() * other;
            };
        case Float other ->
            switch (op) {
              case ADD -> parentNumber.floatValue() + other;
              case SUBTRACT -> parentNumber.floatValue() - other;
              case DIVIDE -> parentNumber.floatValue() / other;
              case MULTIPLY -> parentNumber.floatValue() * other;
            };
        case Long other ->
            switch (op) {
              case ADD -> parentNumber.longValue() + other;
              case SUBTRACT -> parentNumber.longValue() - other;
              case DIVIDE -> parentNumber.longValue() / other;
              case MULTIPLY -> parentNumber.longValue() * other;
            };
        case Double other ->
            switch (op) {
              case ADD -> parentNumber.doubleValue() + other;
              case SUBTRACT -> parentNumber.doubleValue() - other;
              case DIVIDE -> parentNumber.doubleValue() / other;
              case MULTIPLY -> parentNumber.doubleValue() * other;
            };
        default ->
            throw new CodecException("Unexpected numeric type " + value.getClass().getName());
      };
    }
  }

  @Override
  public Class<InheritMathOp> getArgsType() {
    return InheritMathOp.class;
  }

  @Override
  public @Nullable InheritMerger<?> getInstance(InheritMathOp args, Field field) {
    return canProvideFor(field) ? new Provider(args.value()) : null;
  }
}
