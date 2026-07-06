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
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.List;
import org.echoesfrombeyond.echoesfrombeyond.EchoesFromBeyond;
import org.echoesfrombeyond.echoesfrombeyond.component.chunk.AlchemyStorage;
import org.echoesfrombeyond.modutil.component.ComponentUtils;
import org.echoesfrombeyond.util.Check;
import org.echoesfrombeyond.util.array.ArrayUtil;
import org.jetbrains.annotations.UnmodifiableView;
import org.joml.Vector3i;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class AlchemyBenchUtils {
  private AlchemyBenchUtils() {
    throw new RuntimeException();
  }

  public static @UnmodifiableView List<ItemStack> getItemsInStorageNetwork(
      World world, Vector3i position) {
    return EchoesFromBeyond.get()
        .accessAlchemyNetwork(
            world,
            (w, data) ->
                data.forEachInRangeUntil(position, 15, (pos, info) -> info.isOrigin() ? pos : null)
                    .map(
                        pos -> {
                          var storage =
                              ComponentUtils.getBlockComponent(
                                  w, pos, AlchemyStorage.getComponentType());
                          if (storage == null) return null;

                          return storage.getStorage();
                        }))
        .flatMap(optional -> optional)
        .orElse(List.of());
  }

  public static ItemStack[] getValidIngredientsForBench(
      Ref<EntityStore> ref,
      ComponentAccessor<EntityStore> componentAccessor,
      Vector3i targetBlock) {
    var worldChunkComponent =
        ref.getStore()
            .getExternalData()
            .getWorld()
            .getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z));
    if (worldChunkComponent == null) return ItemStack.EMPTY_ARRAY;

    var type =
        worldChunkComponent.getBlockType(
            ChunkUtil.localCoordinate(targetBlock.x),
            targetBlock.y,
            ChunkUtil.localCoordinate(targetBlock.z));
    if (type == null) return ItemStack.EMPTY_ARRAY;

    var combinedInventory =
        InventoryComponent.getCombined(componentAccessor, ref, InventoryComponent.EVERYTHING);
    var targetBlockId = type.getId();

    var items = new ArrayList<ItemStack>();

    combinedInventory.forEach(
        (_, item) -> {
          var validBenches = item.getItem().getData().getRawTags().get("ValidAlchemyBenches");
          if (validBenches != null && ArrayUtil.containsIgnoreCase(validBenches, targetBlockId))
            items.add(item);
        });

    for (int i = 0; i < items.size(); i++) {
      var basis = items.get(i);

      for (int j = items.size() - 1; j > i; j--)
        if (items.get(j).isStackableWith(basis))
          items.set(
              i,
              basis =
                  Check.nonNull(
                      basis.withQuantity(basis.getQuantity() + items.remove(j).getQuantity())));
    }

    return items.toArray(ItemStack[]::new);
  }
}
