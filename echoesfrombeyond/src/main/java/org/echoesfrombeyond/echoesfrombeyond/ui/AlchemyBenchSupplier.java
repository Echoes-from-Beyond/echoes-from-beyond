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

package org.echoesfrombeyond.echoesfrombeyond.ui;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import org.echoesfrombeyond.util.array.ArrayUtil;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class AlchemyBenchSupplier implements OpenCustomUIInteraction.CustomPageSupplier {
  public static ItemEntry[] EMPTY_ITEM_ENTRY_ARRAY = new ItemEntry[0];

  public record ItemEntry(String id, int quantity) {}

  public ItemEntry[] getValidItems(
      Ref<EntityStore> ref,
      ComponentAccessor<EntityStore> componentAccessor,
      InteractionContext context) {
    var combinedInventory =
        componentAccessor.getComponent(ref, InventoryComponent.Combined.getComponentType());
    if (combinedInventory == null) return EMPTY_ITEM_ENTRY_ARRAY;

    var targetBlock = context.getTargetBlock();
    if (targetBlock == null) return EMPTY_ITEM_ENTRY_ARRAY;

    var worldChunkComponent =
        ref.getStore()
            .getExternalData()
            .getWorld()
            .getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z));
    if (worldChunkComponent == null) return EMPTY_ITEM_ENTRY_ARRAY;

    var type =
        worldChunkComponent.getBlockType(
            ChunkUtil.localCoordinate(targetBlock.x),
            targetBlock.y,
            ChunkUtil.localCoordinate(targetBlock.z));
    if (type == null) return EMPTY_ITEM_ENTRY_ARRAY;

    var targetBlockId = type.getId();

    var items = new ArrayList<ItemEntry>();
    for (var inventory : combinedInventory.getInventories().values()) {
      inventory.forEach(
          (_, item) -> {
            var validBenches = item.getItem().getData().getRawTags().get("ValidAlchemyBenches");
            if (validBenches != null && ArrayUtil.containsIgnoreCase(validBenches, targetBlockId))
              items.add(new ItemEntry(item.getItemId(), item.getQuantity()));
          });
    }

    for (int i = 0; i < items.size(); i++) {
      var basis = items.get(i);

      for (int j = items.size() - 1; j > i; j--)
        if (items.get(j).id.equalsIgnoreCase(basis.id))
          items.set(i, basis = new ItemEntry(basis.id, basis.quantity + items.remove(j).quantity));
    }

    return items.toArray(ItemEntry[]::new);
  }
}
