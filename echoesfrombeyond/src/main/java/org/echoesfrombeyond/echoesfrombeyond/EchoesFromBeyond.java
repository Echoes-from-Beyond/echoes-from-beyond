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

package org.echoesfrombeyond.echoesfrombeyond;

import com.hypixel.hytale.math.vector.Vector3iUtil;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.World;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import org.echoesfrombeyond.codechelper.CodecResolver;
import org.echoesfrombeyond.codechelper.cache.CodecCache;
import org.echoesfrombeyond.echoesfrombeyond.inventory.AlchemyIntermediateStackContainer;
import org.echoesfrombeyond.echoesfrombeyond.inventory.AlchemyStackContainer;
import org.echoesfrombeyond.modutil.map.ChunkGridMap;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3i;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Main entrypoint of the mod. All initialization happens here.
 *
 * <p>Hytale instantiates this via reflection, so there are not necessarily any direct references
 * here.
 */
@SuppressWarnings("unused")
@NullMarked
public class EchoesFromBeyond extends JavaPlugin {
  private static @Nullable EchoesFromBeyond instance;

  /**
   * Early-stage {@link CodecResolver} that doesn't require the plugin to be initialized yet. Used
   * to break dependency cycles.
   *
   * <p>Should only contain direct mappings of classes whose codecs are already defined outside the
   * scope of this repository; e.g. {@link ItemStack#CODEC}.
   */
  public static final CodecResolver EARLY_RESOLVER =
      CodecResolver.builder()
          .withDirectMapping(ItemStack.class, ItemStack.CODEC)
          .withDirectMapping(Vector3i.class, Vector3iUtil.CODEC)
          .build();

  private @Nullable CodecResolver resolver;

  private final Map<UUID, ChunkGridMap<AlchemyBenchInfo>> alchemyNetworks;

  /**
   * First entrypoint. Actual initialization tasks should probably go in the various load methods.
   *
   * @param init the initialization argument
   */
  public EchoesFromBeyond(JavaPluginInit init) {
    super(init);

    this.alchemyNetworks = new ConcurrentHashMap<>();
  }

  /**
   * Generic configuration happens here. May be done in parallel using {@link CompletableFuture}s.
   *
   * @return a future representing our plugin's pre-load phase
   */
  @Override
  public @Nullable CompletableFuture<Void> preLoad() {
    // This loads all the plugin configs. So plugins must always call this unless they don't need to
    // bother with configuration.
    return super.preLoad();
  }

  /** Setup. Most asset registration happens here. */
  @Override
  protected void setup() {
    var cache = CodecCache.cache();
    resolver =
        CodecResolver.builder()
            .withStandardSettings(cache)
            .chain(EARLY_RESOLVER)
            .withDirectMapping(
                AlchemyIntermediateStackContainer.class, AlchemyIntermediateStackContainer.CODEC)
            .withDirectMapping(AlchemyStackContainer.class, AlchemyStackContainer.CODEC)
            .build();

    EchoesFromBeyond.instance = this;

    Init.registerAssetRegistries(this);
    Init.registerCodecs(this);
    Init.registerCommands(this);
    Init.registerComponents(this);
    Init.registerSystems(this);

    // This is a no-op currently, but because this has an actual implementation something may be
    // done in the future, so it should always be called.
    super.setup();
  }

  @Override
  protected void start() {
    // Also a no-op.
    super.start();
  }

  public static EchoesFromBeyond get() {
    var instance = EchoesFromBeyond.instance;
    if (instance == null) throw new IllegalStateException("Plugin is not loaded");

    return instance;
  }

  public CodecResolver getResolver() {
    assert resolver != null;
    return resolver;
  }

  @ApiStatus.Internal
  public void removeAlchemyNetworksForWorld(World world) {
    alchemyNetworks.remove(world.getWorldConfig().getUuid());
  }

  @ApiStatus.Internal
  public void addAlchemyBench(World world, Vector3i location, AlchemyBenchInfo info) {
    var data =
        alchemyNetworks.computeIfAbsent(
            world.getWorldConfig().getUuid(), _ -> new ChunkGridMap<>());

    synchronized (data) {
      data.put(location, info);
    }
  }

  @ApiStatus.Internal
  public @Nullable AlchemyBenchInfo removeAlchemyBench(World world, Vector3i location) {
    var uuid = world.getWorldConfig().getUuid();

    var data = alchemyNetworks.get(uuid);
    if (data == null) return null;

    synchronized (data) {
      var returnValue = data.remove(location);
      if (data.size() == 0) alchemyNetworks.remove(uuid);

      return returnValue;
    }
  }

  public <R> Optional<R> accessAlchemyNetwork(
      World world,
      BiFunction<? super World, ? super ChunkGridMap<AlchemyBenchInfo>, @Nullable R> accessor) {
    var data = alchemyNetworks.get(world.getWorldConfig().getUuid());
    if (data == null) return Optional.empty();

    synchronized (data) {
      return Optional.ofNullable(accessor.apply(world, data));
    }
  }
}
