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

package org.echoesfrombeyond.codechelper.cache;

import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import java.util.function.Supplier;
import org.echoesfrombeyond.codechelper.CodecResolver;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

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
   * @param parentCodec the parent codec; {@code null} if there is no parent
   * @param idCodec the id codec; should be {@code null} if {@code idClass} is {@code null}
   * @param codecFamily the base codec class; e.g. {@link BuilderCodec} or {@link AssetBuilderCodec}
   * @param resolver the resolver used to resolve the codec
   * @param resolveCodec a supplier for the actual resolved codec
   * @return the cached or freshly-resolved codec
   * @param <V> the type (de)serialized by the codec
   * @param <C> the codec type
   * @throws NullPointerException if there is a cache miss and {@code resolveCodec} yields a null
   *     value
   */
  @ApiStatus.Internal
  <K, V, C extends Codec<V>> C compute(
      Class<V> model,
      @Nullable Codec<? super V> parentCodec,
      @Nullable Codec<K> idCodec,
      Class<? super C> codecFamily,
      CodecResolver resolver,
      Supplier<C> resolveCodec);

  /**
   * Equivalent to {@link CodecCache#compute(Class, Codec, Codec, Class, CodecResolver, Supplier)},
   * but with {@code parentCodec} and {@code idCodec} set to {@code null}.
   *
   * @param model the model class
   * @param codecFamily the base codec class; e.g. {@link BuilderCodec} or {@link AssetBuilderCodec}
   * @param resolver the resolver used to resolve the codec
   * @param resolveCodec a supplier for the actual resolved codec
   * @return the cached or freshly-resolved codec
   * @param <V> the type (de)serialized by the codec
   * @param <C> the codec type
   * @throws NullPointerException if there is a cache miss and {@code resolveCodec} yields a null
   *     value
   */
  @ApiStatus.Internal
  default <V, C extends Codec<V>> C compute(
      Class<V> model,
      Class<? super C> codecFamily,
      CodecResolver resolver,
      Supplier<C> resolveCodec) {
    return compute(model, null, null, codecFamily, resolver, resolveCodec);
  }

  /**
   * @return a new {@link CodecCache} implementation
   */
  @Contract(value = "-> new", pure = true)
  static CodecCache cache() {
    return new CodecCacheImpl();
  }
}
