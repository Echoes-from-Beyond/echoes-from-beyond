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

import com.hypixel.hytale.codec.Codec;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import org.echoesfrombeyond.codec.CodecResolver;
import org.jspecify.annotations.NullMarked;

/**
 * Standard cache implementation of {@link CodecCache}, backed by a {@link HashMap} and a {@link
 * ReentrantReadWriteLock}.
 */
@NullMarked
final class CodecCacheImpl implements CodecCache {
  private record CacheKey(Class<?> model, CodecResolver resolver) {
    @Override
    public boolean equals(Object obj) {
      return obj instanceof CacheKey(Class<?> otherModel, CodecResolver otherResolver)
          && resolver == otherResolver
          && model.equals(otherModel);
    }

    @Override
    public int hashCode() {
      var hash = 7;
      hash = 31 * hash + model.hashCode();
      hash = 31 * hash + System.identityHashCode(resolver);
      return hash;
    }
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
  public <V, C extends Codec<V>> C compute(
      Class<V> model, CodecResolver resolver, Supplier<C> resolveCodec) {
    var key = new CacheKey(model, resolver);

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

      cached = cache.get(key);
      if (cached != null) return (C) cached;

      var computed = resolveCodec.get();
      cache.put(key, computed);
      return computed;
    } finally {
      if (readLocked) read.unlock();
      else write.unlock();
    }
  }
}
