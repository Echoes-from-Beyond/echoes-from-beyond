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

package org.echoesfrombeyond.codec;

import com.hypixel.hytale.codec.Codec;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Simple resolver that delegates to a list of sub resolvers, each evaluated in-order. The first
 * resolver returning a non-null value is returned.
 */
@NullMarked
class ChainedResolver implements CodecResolver {
  private final List<CodecResolver> resolvers;

  /**
   * Creates a new instance of this class from the provided resolvers list.
   *
   * <p>The list is not copied, so changing it will modify the behavior of the resolver.
   *
   * @param resolvers the resolvers list
   */
  ChainedResolver(List<CodecResolver> resolvers) {
    this.resolvers = resolvers;
  }

  @Override
  public @Nullable Codec<?> resolve(Type type, Field field) {
    for (var resolver : resolvers) {
      var codec = resolver.resolve(type, field);
      if (codec != null) return codec;
    }

    return null;
  }
}
