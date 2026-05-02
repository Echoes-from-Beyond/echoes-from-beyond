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

package org.echoesfrombeyond.codechelper.inherit;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Merges the parent value with the child value. These are used during codec inheritance, as if by
 * calling {@link BuilderCodec.Builder#appendInherited(KeyedCodec, BiConsumer, Function,
 * BiConsumer)}.
 *
 * @param <T> the field type
 */
@NullMarked
public interface InheritMerger<T> {
  /**
   * Merges the parent value with the child value.
   *
   * @param defaultValue the value of the field
   * @param parentValue the parent value of the field
   * @return the merged value
   */
  @Nullable T merge(@Nullable T defaultValue, @Nullable T parentValue);
}
