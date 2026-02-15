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

package org.echoesfrombeyond.codec.annotation.validator;

import java.lang.annotation.*;
import org.echoesfrombeyond.codec.validator.IntRangeProvider;

/**
 * Validates that an {@code int} or {@code Integer} field is within a specified range, which may be
 * inclusive (default) or exclusive.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@ValidatorSpec(IntRangeProvider.class)
@Documented
public @interface ValidateIntRange {
  /** The minimum allowed value. */
  int min();

  /** The maximum allowed value. */
  int max();

  /** Whether {@code maximum} is inclusive (the default) or exclusive. */
  boolean inclusive() default true;
}
