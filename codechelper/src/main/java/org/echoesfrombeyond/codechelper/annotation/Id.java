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

import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.codec.Codec;
import java.lang.annotation.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Marks a field as the identifier field.
 *
 * <p>There can only be one identifier field per model class. Identifier fields are ignored for
 * field resolution. They are read from/written to as if by being assigned through {@code
 * idGetter}/{@code idSetter} lambdas passed to {@link AssetBuilderCodec#builder(Class, Supplier,
 * Codec, BiConsumer, Function, BiConsumer, Function)}.
 *
 * <p>Identifier fields are treated as regular fields unless resolving an {@link AssetBuilderCodec}
 * or if otherwise annotated with {@link Skip}.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Id {}
