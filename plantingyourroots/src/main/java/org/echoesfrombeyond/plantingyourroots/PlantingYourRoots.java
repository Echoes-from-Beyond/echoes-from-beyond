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

package org.echoesfrombeyond.plantingyourroots;

import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import java.util.concurrent.CompletableFuture;
import org.echoesfrombeyond.plantingyourroots.command.ReadyForLove;
import org.echoesfrombeyond.util.thread.Once;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("unused")
@NullMarked
public class PlantingYourRoots extends JavaPlugin {
  private static @Nullable PlantingYourRoots INSTANCE;

  private final Object sync = new Object();
  private final Once<CompletableFuture<World>> kweebdrasil =
      Once.of(
          () -> {
            var defaultWorld = Universe.get().getDefaultWorld();
            if (defaultWorld == null) throw new IllegalStateException("Default world must exist");

            return InstancesPlugin.get()
                .spawnInstance("Kweebdrasil", defaultWorld, new Transform());
          });

  public PlantingYourRoots(JavaPluginInit init) {
    super(init);
    INSTANCE = this;
  }

  public static PlantingYourRoots get() {
    var instance = INSTANCE;
    if (instance == null) throw new IllegalStateException("Plugin is not loaded");

    return instance;
  }

  @Override
  public @Nullable CompletableFuture<Void> preLoad() {
    return super.preLoad();
  }

  @Override
  protected void setup() {
    super.setup();
    getCommandRegistry().registerCommand(new ReadyForLove());
  }

  @Override
  protected void start() {
    super.start();
  }

  @Override
  protected void shutdown() {
    super.shutdown();
    INSTANCE = null;
  }

  public CompletableFuture<World> getKweebdrasil() {
    return kweebdrasil.get();
  }
}
