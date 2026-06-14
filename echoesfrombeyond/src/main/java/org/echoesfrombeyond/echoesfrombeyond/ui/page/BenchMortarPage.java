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

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
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
import java.util.OptionalInt;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.echoesfrombeyond.EchoesFromBeyond;
import org.echoesfrombeyond.echoesfrombeyond.component.chunk.MortarAndPestle;
import org.echoesfrombeyond.echoesfrombeyond.util.AlchemyBenchUtils;
import org.echoesfrombeyond.modutil.component.ComponentUtils;
import org.joml.Vector3i;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class BenchMortarPage extends InteractiveCustomUIPage<BenchMortarPage.BenchMortarData> {
  private final Vector3i position;
  private final List<String> ingredientTypes;

  public BenchMortarPage(
      PlayerRef playerRef,
      Vector3i position,
      int baselineInteractions,
      int interactionsPerIngredient) {
    super(playerRef, CustomPageLifetime.CanDismiss, BenchMortarData.CODEC);
    this.position = position;
    this.ingredientTypes = new ArrayList<>();
  }

  private void buildInternal(
      Ref<EntityStore> ref,
      Store<EntityStore> store,
      UICommandBuilder commandBuilder,
      UIEventBuilder eventBuilder) {
    commandBuilder.append("BenchMortarPage.ui");

    ComponentUtils.getBlockComponent(
            store.getExternalData().getWorld(), position, MortarAndPestle.getComponentType())
        .ifPresent(
            mortarAndPestle -> {
              var items = mortarAndPestle.getItems();
              for (int i = 0; i < items.size(); i++) {
                var item = items.get(i);
                var itemButtonSelect = "#SharedInventory[" + i + "]";

                commandBuilder.append("#SharedInventory", "ItemSlotPreset.ui");
                if (item == null) continue;

                commandBuilder.set(itemButtonSelect + " #Slot.ItemId", item);

                eventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    itemButtonSelect,
                    EventData.of("Index", Integer.toString(i)).append("Type", "RemoveItem"));
              }
            });

    var validIngredients = AlchemyBenchUtils.getValidIngredientsForBench(ref, store, position);

    ingredientTypes.clear();

    for (int i = 0; i < validIngredients.length; i++) {
      var ingredient = validIngredients[i];
      var itemButtonSelect = "#PlayerInventory[" + i + "]";

      commandBuilder.append("#PlayerInventory", "ItemSlotPreset.ui");
      commandBuilder.set(itemButtonSelect + " #Slot.ItemId", ingredient.getId());
      commandBuilder.set(itemButtonSelect + " #Slot.Quantity", ingredient.getQuantity());
      commandBuilder.set(itemButtonSelect + " #Slot.ShowQuantity", true);

      eventBuilder.addEventBinding(
          CustomUIEventBindingType.Activating,
          itemButtonSelect,
          EventData.of("Index", Integer.toString(i)).append("Type", "AddItem"));

      ingredientTypes.add(ingredient.getId());
    }

    for (int i = 0; i < 5; i++) {
      // TODO: currently proof of concept for display; rework this to pull from the OVERFLOW
      String overflowSelect = "#Overflow[" + i + "]";

      commandBuilder.append("#Overflow", "ItemSlotPreset.ui");
    }
  }

  @Override
  public void build(
      Ref<EntityStore> ref,
      UICommandBuilder commandBuilder,
      UIEventBuilder eventBuilder,
      Store<EntityStore> store) {
    buildInternal(ref, store, commandBuilder, eventBuilder);
  }

  @Override
  public void handleDataEvent(
      Ref<EntityStore> ref, Store<EntityStore> store, BenchMortarData data) {
    var mortarAndPestleOptional =
        ComponentUtils.getBlockComponent(
            store.getExternalData().getWorld(), position, MortarAndPestle.getComponentType());

    if (mortarAndPestleOptional.isEmpty()) {
      sendUpdate();
      return;
    }

    var mortarAndPestle = mortarAndPestleOptional.get();

    switch (data.Type) {
      case "AddItem" ->
          data.getIndex(ingredientTypes.size())
              .ifPresent(
                  index ->
                      mortarAndPestle.tryAddItem(
                          ingredientTypes.get(index),
                          itemType -> {
                            var combinedInventory =
                                store.getComponent(
                                    ref, InventoryComponent.Combined.getComponentType());
                            assert combinedInventory != null;

                            for (var inventory : combinedInventory.getInventories().values())
                              if (inventory
                                  .removeItemStack(new ItemStack((String) itemType, 1))
                                  .succeeded()) return true;
                            return false;
                          }));
      case "RemoveItem" -> {}
      case null, default -> {}
    }

    var commandBuilder = new UICommandBuilder();
    var eventBuilder = new UIEventBuilder();

    buildInternal(ref, store, commandBuilder, eventBuilder);
    sendUpdate(commandBuilder, eventBuilder, true);
  }

  @NullMarked
  @ModelBuilder
  public static class BenchMortarData {
    public static final BuilderCodec<BenchMortarData> CODEC =
        CodecUtil.modelBuilder(BenchMortarData.class, EchoesFromBeyond.get().getResolver());

    @SuppressWarnings({"FieldMayBeFinal", "unused"})
    private @Nullable String Index;

    public @Nullable String Type;

    public OptionalInt getIndex(int len) {
      var index = Index;
      if (index == null) return OptionalInt.empty();

      try {
        var actualIndex = Integer.parseInt(index);
        if (actualIndex < 0 && actualIndex >= len) return OptionalInt.empty();
        return OptionalInt.of(actualIndex);
      } catch (NumberFormatException _) {
        return OptionalInt.empty();
      }
    }
  }
}
