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

package org.echoesfrombeyond.echoesfrombeyond.util;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import org.echoesfrombeyond.echoesfrombeyond.ItemEntry;
import org.echoesfrombeyond.util.array.ArrayUtil;
import org.joml.Vector3i;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class AlchemyBenchUtils {
  private AlchemyBenchUtils() {
    throw new RuntimeException();
  }

  public static ItemEntry[] getValidIngredientsForBench(
      Ref<EntityStore> ref,
      ComponentAccessor<EntityStore> componentAccessor,
      Vector3i targetBlock) {
    var combinedInventory =
        componentAccessor.getComponent(ref, InventoryComponent.Combined.getComponentType());
    if (combinedInventory == null) return ItemEntry.EMPTY_ITEM_ENTRY_ARRAY;

    var worldChunkComponent =
        ref.getStore()
            .getExternalData()
            .getWorld()
            .getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z));
    if (worldChunkComponent == null) return ItemEntry.EMPTY_ITEM_ENTRY_ARRAY;

    var type =
        worldChunkComponent.getBlockType(
            ChunkUtil.localCoordinate(targetBlock.x),
            targetBlock.y,
            ChunkUtil.localCoordinate(targetBlock.z));
    if (type == null) return ItemEntry.EMPTY_ITEM_ENTRY_ARRAY;

    var targetBlockId = type.getId();

    var items = new ArrayList<ItemEntry>();
    for (var inventory : combinedInventory.getInventories().values())
      inventory.forEach(
          (_, item) -> {
            var validBenches = item.getItem().getData().getRawTags().get("ValidAlchemyBenches");
            if (validBenches != null && ArrayUtil.containsIgnoreCase(validBenches, targetBlockId))
              items.add(new ItemEntry(item));
          });

    for (int i = 0; i < items.size(); i++) {
      var basis = items.get(i);

      for (int j = items.size() - 1; j > i; j--)
        if (items.get(j).getId().equalsIgnoreCase(basis.getId()))
          items.set(i, basis = basis.add(items.remove(j).getQuantity()));
    }

    return items.toArray(ItemEntry[]::new);
  }
}
