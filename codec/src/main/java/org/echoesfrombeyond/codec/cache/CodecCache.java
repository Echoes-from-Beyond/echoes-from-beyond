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

package org.echoesfrombeyond.codec.cache;

import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import java.util.function.Supplier;
import org.echoesfrombeyond.codec.CodecResolver;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;

/**
 * A generic, thread-safe cache of resolved {@link Codec}s.
 *
 * <p>Codec resolution can be costly, especially for large compound types. Using a cache ensures
 * that each type is only resolved once per resolver.
 *
 * <p>When using a cache, it is important to ensure that all resolved codecs are functionally
 * stateless, as the same codec instance may be shared in multiple locations.
 *
 * <p>Use {@link CodecCache#cache()} to construct a new implementation of this interface.
 */
@NullMarked
public sealed interface CodecCache permits CodecCacheImpl {
  /**
   * Returns a cached codec if it exists; otherwise invokes {@code resolveCodec} and inserts the
   * codec into the cache.
   *
   * <p>It is important to ensure that {@code resolver} is the same {@link CodecResolver} used to
   * resolve the codec returned by the {@code resolveCodec} supplier. Failing to do so may lead to
   * cache problems.
   *
   * <p>Implementations are tolerant of reentrancy: {@code resolveCodec} may directly or indirectly
   * invoke this method again.
   *
   * @param model the model class
   * @param codec the base codec class; e.g. {@link BuilderCodec} or {@link AssetBuilderCodec}
   * @param resolver the resolver used to resolve the codec
   * @param resolveCodec the actual resolved codec
   * @return the cached or freshly-resolved codec
   * @param <V> the type (de)serialized by the codec
   * @param <C> the codec type
   * @throws NullPointerException if there is a cache miss and {@code resolveCodec} yields a null
   *     value
   */
  <V, C extends Codec<V>> C compute(
      Class<V> model, Class<? super C> codec, CodecResolver resolver, Supplier<C> resolveCodec);

  /**
   * @return a new {@link CodecCache} implementation
   */
  @Contract(value = "-> new", pure = true)
  static CodecCache cache() {
    return new CodecCacheImpl();
  }
}
