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

package org.echoesfrombeyond.codechelper.annotation;

import java.lang.annotation.*;

/**
 * Marker annotation applied to a field to indicate that it should be skipped during codec
 * resolution. This is the semantic equivalent of the {@code transient} modifier.
 *
 * <p>Skipped fields will not be written to or read from during codec (de)serialization, even if
 * they appear in the input data.
 *
 * <p>Note that non-{@code static} or {@code final} fields will be skipped regardless, so it is
 * redundant to apply this annotation to them.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Skip {}
