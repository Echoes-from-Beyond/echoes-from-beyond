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

package org.echoesfrombeyond.plantingyourroots.system;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.system.StoreSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSavingSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import java.util.logging.Level;
import org.echoesfrombeyond.plantingyourroots.PlantingYourRoots;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class CleanWorldSystem extends StoreSystem<EntityStore> {
  private static final Set<Dependency<EntityStore>> DEPENDENCIES =
      Set.of(new SystemDependency<>(Order.AFTER, PlayerSavingSystems.WorldRemovedSystem.class));

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  @Override
  public void onSystemAddedToStore(Store<EntityStore> store) {}

  @Override
  public void onSystemRemovedFromStore(Store<EntityStore> store) {
    var world = store.getExternalData().getWorld();
    var roots = PlantingYourRoots.get();

    if (!roots.isKweebdrasilInstance(world)) return;

    LOGGER.at(Level.INFO).log("Removing Kweebdrasil instance...");
    roots.removeKweebdrasilInstance(world.getWorldConfig().getUuid());
  }

  @Override
  public Set<Dependency<EntityStore>> getDependencies() {
    return DEPENDENCIES;
  }
}
