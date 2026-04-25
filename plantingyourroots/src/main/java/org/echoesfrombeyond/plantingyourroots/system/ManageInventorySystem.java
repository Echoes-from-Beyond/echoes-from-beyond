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

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import org.echoesfrombeyond.plantingyourroots.PlantingYourRoots;
import org.echoesfrombeyond.plantingyourroots.component.RootsComponent;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ManageInventorySystem extends PlayerSystems.PlayerRemovedSystem {
  private static final Set<Dependency<EntityStore>> DEPENDENCIES =
      Set.of(
          new SystemDependency<>(Order.BEFORE, PlayerSystems.PlayerRemovedSystem.class),
          new SystemDependency<>(Order.BEFORE, PlayerSystems.PlayerAddedSystem.class));

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static void loadOldInventory(Holder<EntityStore> holder, RootsComponent roots) {
    for (int i = 0; i < InventoryComponent.EVERYTHING.length; i++) {
      var old = roots.OldInventory[i];
      var type = InventoryComponent.EVERYTHING[i];

      Component<EntityStore> copy;
      if (old == null || (copy = old.clone()) == null) holder.tryRemoveComponent(type);
      else holder.putComponent((ComponentType) type, copy);
    }
    roots.OldInventory = new InventoryComponent[InventoryComponent.EVERYTHING.length];
  }

  @Override
  public void onEntityAdd(Holder<EntityStore> holder, AddReason reason, Store<EntityStore> store) {
    var plugin = PlantingYourRoots.get();

    var roots = holder.getComponent(RootsComponent.getComponentType());
    assert roots != null;

    if (!plugin.isKweebdrasilInstance(store.getExternalData().getWorld())) {
      if (roots.HasOldInventory) loadOldInventory(holder, roots);
      roots.HasOldInventory = false;
      return;
    }

    for (int i = 0; i < InventoryComponent.EVERYTHING.length; i++) {
      var component = holder.getComponent(InventoryComponent.EVERYTHING[i]);
      roots.OldInventory[i] = component == null ? null : (InventoryComponent) component.clone();

      if (component != null) component.getInventory().clear();
    }

    roots.HasOldInventory = true;
  }

  @Override
  public void onEntityRemoved(
      Holder<EntityStore> holder, RemoveReason reason, Store<EntityStore> store) {}

  @Override
  public Query<EntityStore> getQuery() {
    return Query.and(super.getQuery(), RootsComponent.getComponentType());
  }

  @Override
  public Set<Dependency<EntityStore>> getDependencies() {
    return DEPENDENCIES;
  }
}
