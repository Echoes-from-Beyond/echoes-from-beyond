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

package org.echoesfrombeyond.codechelper;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import java.util.*;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.codechelper.cache.CodecCache;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Library plugin containing instances intended to be shared between plugins. */
@NullMarked
@SuppressWarnings("unused")
public final class Plugin extends JavaPlugin {
  private static @Nullable CodecCache SHARED_CACHE;
  private static @Nullable CodecResolver SHARED_RESOLVER;

  /**
   * Invoked reflectively by Hytale.
   *
   * @param init plugin settings
   */
  public Plugin(JavaPluginInit init) {
    super(init);
  }

  @Override
  protected void setup() {
    var cache = CodecCache.cache();

    SHARED_CACHE = cache;
    SHARED_RESOLVER =
        CodecResolver.builder()
            .chain(CodecResolver.PRIMITIVE)
            .withArraySupport()
            .withCollectionSupport()
            .withMapSupport()
            .withRecursiveResolution(cache)
            .withSubtypeMapping(List.class, ArrayList.class)
            .withSubtypeMapping(Map.class, HashMap.class)
            .withSubtypeMapping(Set.class, HashSet.class)
            .build();

    super.setup();
  }

  @Override
  protected void shutdown() {
    SHARED_CACHE = null;
    SHARED_RESOLVER = null;
    super.shutdown();
  }

  private static IllegalStateException uninitializedPluginException() {
    return new IllegalStateException("Plugin has not been initialized yet");
  }

  /**
   * Returns the shared {@link CodecCache} instance.
   *
   * <p>It is also possible to obtain a new {@link CodecCache} instance by just calling {@link
   * CodecCache#cache()}. The instance returned by this method is intended to be shared between
   * multiple plugins.
   *
   * @return the shared cache
   * @throws IllegalStateException if this plugin has not been initialized
   */
  public static CodecCache getSharedCache() {
    var cache = SHARED_CACHE;
    if (cache == null) throw uninitializedPluginException();

    return cache;
  }

  /**
   * Returns the shared {@link CodecResolver} instance.
   *
   * <p>It is also possible to obtain a new CodecResolver instance by using the builder {@link
   * CodecResolver#builder()}. The shared resolver is intended to be used by multiple plugins.
   *
   * <p>This resolver will always support every capability specified in {@link
   * CodecResolver.Builder}, in addition to {@link CodecResolver#PRIMITIVE}. It also defines subtype
   * mappings for the following types:
   *
   * <ul>
   *   <li>List -> ArrayList
   *   <li>Set -> HashSet
   *   <li>Map -> HashMap
   * </ul>
   *
   * <p>Recursive {@link ModelBuilder} resolution will use {@link Plugin#getSharedCache()}.
   *
   * @return the shared codec resolver
   * @throws IllegalStateException if this plugin has not been initialized
   * @see CodecResolver#PRIMITIVE a resolver that can only resolve basic types
   */
  public static CodecResolver getSharedResolver() {
    var resolver = SHARED_RESOLVER;
    if (resolver == null) throw uninitializedPluginException();

    return resolver;
  }
}
