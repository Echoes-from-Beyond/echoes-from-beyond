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

package org.echoesfrombeyond.codec.annotation;

import com.hypixel.hytale.codec.validation.Validator;
import java.lang.annotation.*;
import java.util.Map;

/**
 * Applied to a {@code public static} method defined in a {@link ModelBuilder} class. Such a method
 * must take no arguments and return a {@link Map} whose key type is {@link String}, and value type
 * is a collection of anything assignable to {@link Validator}.
 *
 * <p>Each key in the returned map represents the name of a field, and the corresponding value all
 * validators that should be applied to the field.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Validate {}
