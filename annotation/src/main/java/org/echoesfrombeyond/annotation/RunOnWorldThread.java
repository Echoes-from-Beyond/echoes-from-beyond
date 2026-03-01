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

package org.echoesfrombeyond.annotation;

import java.lang.annotation.*;

/**
 * Specifies that the annotated method must be executed on the world thread. Failure to do so may
 * result in explicit exceptions or threading mishaps. This method is intended solely for
 * documentation purposes or consumption by static analysis tooling.
 *
 * <p>If method A is overridden by method B, and A is annotated with this annotation, method B
 * <i>may or may not</i> specify it. However, the case where A is <i>not</i> annotated but B is,
 * should be considered a semantic error by static analysis tooling.
 */
@Retention(RetentionPolicy.SOURCE)
@Documented
@Target(ElementType.METHOD)
public @interface RunOnWorldThread {}
