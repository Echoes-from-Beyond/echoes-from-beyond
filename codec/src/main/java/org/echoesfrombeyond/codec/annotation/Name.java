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

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import java.lang.annotation.*;
import org.jspecify.annotations.NonNull;

/**
 * When applied to a field, sets the name that will be used as a key in the serialized data. If this
 * annotation is not present, the name of the field as it appears in the source code will be used.
 *
 * <p>Multiple fields in the same class should not share names.
 *
 * <p>{@link Name#value()} is passed as-is to the {@code key} parameter of {@link
 * KeyedCodec#KeyedCodec(String, Codec)}. Values should therefore follow the Hytale naming
 * convention: {@code PascalCase}. Currently, empty names or names starting with a non-uppercase
 * character are explicitly not permitted and will cause errors during resolution.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Name {
  /**
   * @return the name, which will be used instead of the field's source code name
   */
  @NonNull String value();
}
