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
import org.echoesfrombeyond.codechelper.inherit.InheritProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

/**
 * Specifies an inheritance policy where the child value overrides the parent value whenever the
 * child value is non-null. Default inheritance behavior is to <i>always</i> use the child value, so
 * using this differs from not including any {@link InheritSpec}-type annotation.
 *
 * <p>This annotation is compatible with all field types.
 *
 * @see InheritParent
 */
@Target(ElementType.FIELD)
@InheritSpec(InheritProvider.class)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NullMarked
@ApiStatus.Experimental
public @interface Inherit {}
