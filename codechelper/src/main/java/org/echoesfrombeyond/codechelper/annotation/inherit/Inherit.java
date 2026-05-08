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

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import java.lang.annotation.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.echoesfrombeyond.codechelper.inherit.InheritProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

/**
 * Enables basic field inheritance, as if by calling {@link
 * BuilderCodec.Builder#appendInherited(KeyedCodec, BiConsumer, Function, BiConsumer)}.
 *
 * <p>The parent value will be used if the child value is absent in the config.
 *
 * <p>This annotation is compatible with any field type.
 */
@Target(ElementType.FIELD)
@InheritSpec(InheritProvider.class)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NullMarked
@ApiStatus.Experimental
public @interface Inherit {}
