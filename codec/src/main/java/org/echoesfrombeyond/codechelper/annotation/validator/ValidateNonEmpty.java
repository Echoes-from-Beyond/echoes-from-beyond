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

package org.echoesfrombeyond.codechelper.annotation.validator;

import java.lang.annotation.*;
import java.util.Collection;
import java.util.Map;
import org.echoesfrombeyond.codechelper.validator.NonEmptyProvider;

/**
 * Validates that the field is non-empty.
 *
 * <p>If the field is a {@link String}, validates that the string is not zero length. If the field
 * is a {@link Collection} or {@link Map}, validates that the number of elements/entries,
 * respectively, is non-zero. If the field is an array, validates that the length is non-zero.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@ValidatorSpec(NonEmptyProvider.class)
@Documented
public @interface ValidateNonEmpty {}
