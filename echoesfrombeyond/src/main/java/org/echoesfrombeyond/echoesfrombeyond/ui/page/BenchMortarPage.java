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

package org.echoesfrombeyond.echoesfrombeyond.ui.page;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.List;
import org.echoesfrombeyond.echoesfrombeyond.component.chunk.MortarAndPestle;
import org.echoesfrombeyond.echoesfrombeyond.ui.data.BenchInteractionType;
import org.echoesfrombeyond.echoesfrombeyond.ui.data.GenericBenchData;
import org.echoesfrombeyond.echoesfrombeyond.ui.data.ItemSource;
import org.echoesfrombeyond.echoesfrombeyond.util.AlchemyBenchUtils;
import org.echoesfrombeyond.modutil.component.ComponentUtils;
import org.echoesfrombeyond.util.Check;
import org.joml.Vector3i;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class BenchMortarPage extends InteractiveCustomUIPage<GenericBenchData> {
  private final Vector3i position;
  private final List<ItemStack> ingredientTypes;
  private final int baselineInteractions;
  private final int interactionsPerIngredient;

  public BenchMortarPage(
      PlayerRef playerRef,
      Vector3i position,
      int baselineInteractions,
      int interactionsPerIngredient) {
    super(playerRef, CustomPageLifetime.CanDismiss, GenericBenchData.CODEC);
    this.position = position;
    this.ingredientTypes = new ArrayList<>();
    this.baselineInteractions = baselineInteractions;
    this.interactionsPerIngredient = interactionsPerIngredient;
  }

  private boolean grind(MortarAndPestle mortar) {
    if (!mortar.hasItems()) return false;

    mortar.CurrentInteractions++;
    double prog = mortar.getProgress(baselineInteractions, interactionsPerIngredient);

    if (prog >= 1.0) {
      mortar.CurrentInteractions = 0;
      // TODO: recipe logic
    }

    return true;
  }

  private void buildInternal(
      Ref<EntityStore> ref,
      Store<EntityStore> store,
      UICommandBuilder commandBuilder,
      UIEventBuilder eventBuilder,
      MortarAndPestle mortarAndPestle) {
    commandBuilder.append("BenchMortarPage.ui");

    var items = mortarAndPestle.ItemsToGrind.getAllItems();

    // MAIN UI
    var maxSize = mortarAndPestle.ItemsToGrind.getMaxSize();
    var occupiedSlots = items.size();

    // populate occupied slots first
    for(int i = 0; i < occupiedSlots; i++) {
      var item = items.get(i);
      var itemButtonSelect = "#MortarInput[" + i + "]";

      commandBuilder.append("#MortarInput", "ItemSlotPreset.ui");

      commandBuilder.set(itemButtonSelect + " #Slot.ItemId", item.getItemId());

      eventBuilder.addEventBinding(
              CustomUIEventBindingType.Activating,
              itemButtonSelect,
              EventData.of("Index", Integer.toString(i))
                      .append("Type", BenchInteractionType.RemoveItemFromInput));
    }

    for(int j = occupiedSlots; j < maxSize; j++) {
      commandBuilder.append("#MortarInput", "ItemSlotPreset.ui");
    }

    var validIngredients = AlchemyBenchUtils.getValidIngredientsForBench(ref, store, position);
    var itemsInStorage =
        AlchemyBenchUtils.getItemsInStorageNetwork(store.getExternalData().getWorld(), position);

    // STORAGE
    for (int i = 0; i < itemsInStorage.size(); i++) {
      var select = "#SharedInventory[" + i + "]";

      commandBuilder.append("#SharedInventory", "ItemSlotPreset.ui");
      commandBuilder.set(select + " #Slot.ItemId", itemsInStorage.get(i).getItemId());
    }

    // PLAYER INVENTORY
    ingredientTypes.clear();
    for (int i = 0; i < validIngredients.length; i++) {
      var ingredient = validIngredients[i];
      var itemButtonSelect = "#PlayerInventory[" + i + "]";

      commandBuilder.append("#PlayerInventory", "ItemSlotPreset.ui");
      commandBuilder.set(itemButtonSelect + " #Slot.ItemId", ingredient.getItemId());
      commandBuilder.set(itemButtonSelect + " #Slot.Quantity", ingredient.getQuantity());
      commandBuilder.set(itemButtonSelect + " #Slot.ShowQuantity", true);

      eventBuilder.addEventBinding(
          CustomUIEventBindingType.Activating,
          itemButtonSelect,
          EventData.of("Index", Integer.toString(i))
              .append("Type", BenchInteractionType.AddItemToInput)
              .append("Source", ItemSource.PlayerInventory));

      ingredientTypes.add(Check.nonNull(ingredient.withQuantity(1)));
    }

    var overflow = mortarAndPestle.Overflow;

    // OVERFLOW
    for (int i = 0; i < overflow.getSlotCount(); i++) {
      var item = overflow.getItem(i);
      assert item != null;
      String overflowSelect = "#Overflow[" + i + "]";

      commandBuilder.append("#Overflow", "ItemSlotPreset.ui");
      commandBuilder.set(overflowSelect + " #Slot.ItemId", item.getItemId());
      commandBuilder.set(overflowSelect + " #Slot.Quantity", item.getQuantity());
      commandBuilder.set(overflowSelect + " #Slot.ShowQuantity", true);

      eventBuilder.addEventBinding(
          CustomUIEventBindingType.Activating,
          overflowSelect,
          EventData.of("Index", Integer.toString(i))
              .append("Type", BenchInteractionType.RemoveItemFromOverflow));
    }

    commandBuilder.set(
        "#ProgressBar.Value",
        mortarAndPestle.getProgress(baselineInteractions, interactionsPerIngredient));

    if (!mortarAndPestle.hasItems()) commandBuilder.set("#GrindButton.Disabled", true);

    // the index here is not important
    eventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#GrindButton",
        EventData.of("Type", "Grind").append("Type", BenchInteractionType.Grind));
  }

  @Override
  public void build(
      Ref<EntityStore> ref,
      UICommandBuilder commandBuilder,
      UIEventBuilder eventBuilder,
      Store<EntityStore> store) {
    var mortarAndPestle =
        ComponentUtils.getBlockComponent(
            store.getExternalData().getWorld(), position, MortarAndPestle.getComponentType());
    if (mortarAndPestle == null) return;

    buildInternal(ref, store, commandBuilder, eventBuilder, mortarAndPestle);
  }

  @Override
  public void handleDataEvent(
      Ref<EntityStore> ref, Store<EntityStore> store, GenericBenchData data) {
    var world = store.getExternalData().getWorld();
    var mortarAndPestle =
        ComponentUtils.getBlockComponent(world, position, MortarAndPestle.getComponentType());

    if (mortarAndPestle == null) {
      sendUpdate();
      return;
    }

    var combinedInventory =
        InventoryComponent.getCombined(store, ref, InventoryComponent.EVERYTHING);

    var modification =
        switch (data.Type) {
          case AddItemToInput ->
              (!mortarAndPestle.isInProgress())
                  && data.useIndex(
                          ingredientTypes,
                          (_, item) ->
                              mortarAndPestle.tryAddItemToInput(
                                  item,
                                  itemStack ->
                                      combinedInventory
                                          .removeItemStack((ItemStack) itemStack)
                                          .succeeded()))
                      .orElse(false);

          case RemoveItemFromInput ->
              (!mortarAndPestle.isInProgress())
                  && data.useIndex(
                          mortarAndPestle.ItemsToGrind.getAllItems(),
                          (index, itemToRemove) -> {
                            mortarAndPestle.ItemsToGrind.removeItem(index);

                            if (!combinedInventory.addItemStack(itemToRemove).succeeded())
                              ItemUtils.dropItem(ref, itemToRemove, store);

                            return true;
                          })
                      .orElse(false);

          case RemoveItemFromOverflow ->
              data.useIndex(
                      mortarAndPestle.Overflow.getAllItems(),
                      (index, item) -> {
                        // to remove from overflow, no need to check for progress
                        // however, there *shouldn't* be progress if overflow exists - a player must
                        // take out all
                        // overflow before grinding more ingredients
                        mortarAndPestle.Overflow.removeItem(index);

                        if (!combinedInventory.addItemStack(item).succeeded())
                          ItemUtils.dropItem(ref, item, store);

                        return true;
                      })
                  .orElse(false);

          case Grind -> grind(mortarAndPestle);

          case null -> false;
        };

    if (modification) ComponentUtils.markNeedsSaving(world, position);

    var commandBuilder = new UICommandBuilder();
    var eventBuilder = new UIEventBuilder();

    buildInternal(ref, store, commandBuilder, eventBuilder, mortarAndPestle);
    sendUpdate(commandBuilder, eventBuilder, true);
  }
}
