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

package org.echoesfrombeyond.codechelper.annotation.inherit;

import java.lang.annotation.*;
import org.echoesfrombeyond.codechelper.inherit.InheritMathOpProvider;
import org.jspecify.annotations.NullMarked;

/**
 * Specifies an inheritance policy that performs a numeric operation to compute the child value.
 *
 * <p>The left-hand side of the operation is always the parent value. The result of the operation
 * will become the actual value of the child.
 *
 * <p>If both the parent and child values are {@code null}, the result is null. If only one value is
 * null, the result is simply the non-null value as-is, without any mathematical operations
 * performed.
 *
 * <p>This annotation is compatible with boxed and primitive numeric types
 */
@InheritSpec(InheritMathOpProvider.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NullMarked
public @interface InheritMathOp {
  /** Math operations supported by this annotation. */
  enum Op {
    /** Adds the parent value to the child value. */
    ADD,

    /** Subtracts the child value from the parent value. */
    SUBTRACT,

    /** Divides the parent value with the child value. */
    DIVIDE,

    /** Multiplies the parent value with the child value. */
    MULTIPLY
  }

  /**
   * @return the math operation to perform
   */
  Op value();
}
