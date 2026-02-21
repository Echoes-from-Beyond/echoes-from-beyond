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

import com.hypixel.hytale.codec.Codec;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import org.echoesfrombeyond.codechelper.CodecResolver;
import org.echoesfrombeyond.util.Check;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Standard cache implementation of {@link CodecCache}, backed by a {@link HashMap} and a {@link
 * ReentrantReadWriteLock}.
 */
@NullMarked
final class CodecCacheImpl implements CodecCache {
  private static final class CacheKey {
    private final Class<?> model;
    private final @Nullable Class<?> inheritFrom;
    private final @Nullable Class<?> idClass;
    private final Class<?> codec;

    private final @Nullable Reference<@Nullable Codec<?>> idCodec;
    private final Reference<@Nullable CodecResolver> resolver;

    private final int baseHash;

    private CacheKey(
        Class<?> model,
        @Nullable Class<?> inheritFrom,
        @Nullable Class<?> idClass,
        Class<?> codec,
        @Nullable Reference<@Nullable Codec<?>> idCodec,
        Reference<@Nullable CodecResolver> resolver) {
      this.model = model;
      this.inheritFrom = inheritFrom;
      this.idClass = idClass;
      this.codec = codec;

      this.idCodec = idCodec;
      this.resolver = resolver;

      var hash = 7;
      hash = 31 * hash + model.hashCode();
      hash = 31 * hash + Objects.hashCode(inheritFrom);
      hash = 31 * hash + Objects.hashCode(idClass);
      hash = 31 * hash + codec.hashCode();

      this.baseHash = hash;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof CacheKey other)) return false;

      return model.equals(other.model)
          && Objects.equals(inheritFrom, other.inheritFrom)
          && Objects.equals(idClass, other.idClass)
          && Objects.equals(codec, other.codec)
          && refSame(idCodec, other.idCodec)
          && refSame(resolver, other.resolver);
    }

    @Override
    public int hashCode() {
      int hash = baseHash;
      if (idCodec != null) hash = 31 * hash + System.identityHashCode(idCodec.get());
      hash = 31 * hash + System.identityHashCode(resolver.get());
      return hash;
    }
  }

  private static <T> boolean refSame(
      @Nullable Reference<? super @Nullable T> first,
      @Nullable Reference<? extends @Nullable T> second) {
    if (first == null && second == null) return true;
    if (first == null ^ second == null) return false;

    return first.refersTo(second.get());
  }

  private final Map<CacheKey, Codec<?>> cache;
  private final ReentrantReadWriteLock rwl;

  /** Creates a new, initially empty cache. */
  CodecCacheImpl() {
    this.cache = new HashMap<>();
    this.rwl = new ReentrantReadWriteLock();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <K, V, C extends Codec<V>> C compute(
      Class<V> model,
      @Nullable Class<? super V> inheritFrom,
      @Nullable Class<K> idClass,
      @Nullable Codec<K> idCodec,
      Class<? super C> codec,
      CodecResolver resolver,
      Supplier<C> resolveCodec) {
    var key =
        new CacheKey(
            model,
            inheritFrom,
            idClass,
            codec,
            idCodec == null ? null : new WeakReference<>(idCodec),
            new WeakReference<>(resolver));

    var read = rwl.readLock();
    var write = rwl.writeLock();

    read.lock();

    var readLocked = true;
    try {
      var cached = cache.get(key);
      if (cached != null) return (C) cached;

      read.unlock();
      readLocked = false;
      write.lock();

      cache
          .keySet()
          .removeIf(
              oldKey ->
                  oldKey.resolver.refersTo(null)
                      || (oldKey.idCodec != null && oldKey.idCodec.refersTo(null)));

      cached = cache.get(key);
      if (cached != null) return (C) cached;

      var computed = Check.nonNull(resolveCodec.get());
      cache.put(key, computed);
      return computed;
    } finally {
      if (readLocked) read.unlock();
      else write.unlock();
    }
  }
}
