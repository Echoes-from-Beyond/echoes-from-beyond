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
import org.echoesfrombeyond.codec.annotation.ModelBuilder;
import org.echoesfrombeyond.codec.cache.CodecCache;
import org.echoesfrombeyond.util.type.TypeUtil;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Resolver capable of recursively resolving types annotated with {@link ModelBuilder}. */
@NullMarked
class RecursiveResolver implements CodecResolver {
  private final CodecResolver root;
  private final @Nullable CodecCache cache;

  /**
   * Creates a new instance of this class.
   *
   * @param root the root type
   * @param cache the codec cache to be used for sub-codecs
   */
  RecursiveResolver(CodecResolver root, @Nullable CodecCache cache) {
    this.root = root;
    this.cache = cache;
  }

  @Override
  public @Nullable Codec<?> resolve(Type type, Field field) {
    var raw = TypeUtil.getRawType(type);
    if (raw == null || !raw.isAnnotationPresent(ModelBuilder.class)) return null;

    return cache == null
        ? CodecUtil.modelBuilder(raw, root)
        : CodecUtil.modelBuilder(raw, root, cache);
  }
}
