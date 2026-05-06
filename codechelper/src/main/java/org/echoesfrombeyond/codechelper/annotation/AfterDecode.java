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

import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import java.lang.annotation.*;
import java.util.function.Consumer;
import org.jspecify.annotations.NullMarked;

/**
 * Annotation applied to a method in a {@link ModelBuilder} class that will be run after an instance
 * is loaded. Such methods must be non-{@code static}, non-{@code native}, and non-{@code abstract}.
 * Additionally, they must either be parameterless or specify a single parameter assignable from
 * {@link ExtraInfo}.
 *
 * <p>If a given model class contains multiple {@code AfterDecode} methods, they will all be
 * executed sequentially in an <b>unspecified order.</b>
 *
 * <p>Otherwise, these work equivalently to supplying a lambda to {@link
 * BuilderCodec.Builder#afterDecode(Consumer)}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@NullMarked
public @interface AfterDecode {}
