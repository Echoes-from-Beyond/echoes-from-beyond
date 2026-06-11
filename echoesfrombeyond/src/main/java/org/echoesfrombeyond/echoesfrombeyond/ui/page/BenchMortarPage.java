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
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.echoesfrombeyond.ui.AlchemyBenchSupplier;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class BenchMortarPage extends InteractiveCustomUIPage<BenchMortarPage.BenchMortarData> {
  private final AlchemyBenchSupplier.ItemEntry[] validIngredients;

  public BenchMortarPage(PlayerRef playerRef, AlchemyBenchSupplier.ItemEntry[] validIngredients) {
    super(playerRef, CustomPageLifetime.CanDismiss, BenchMortarData.CODEC);
    this.validIngredients = validIngredients;
  }

  @Override
  public void build(
      Ref<EntityStore> ref,
      UICommandBuilder commandBuilder,
      UIEventBuilder eventBuilder,
      Store<EntityStore> store) {
    commandBuilder.append("BenchMortarPage.ui");
    // TODO: store bench data persistently and populate all the input buttons with it

    for (int i = 0; i < 5; i++) {
      // TODO: currently proof of concept for display; rework this to pull from the STORAGE SYSTEM
      String sharedSelect = "#SharedInventory[" + i + "]";

      commandBuilder.append("#SharedInventory", "ItemSlotPreset.ui");
    }

    for (int i = 0; i < 5; i++) {
      // TODO: currently proof of concept for display; rework this to pull from the PLAYER INVENTORY
      String invSelect = "#PlayerInventory[" + i + "]";

      commandBuilder.append("#PlayerInventory", "ItemSlotPreset.ui");
    }

    for (int i = 0; i < 5; i++) {
      // TODO: currently proof of concept for display; rework this to pull from the OVERFLOW
      String overflowSelect = "#Overflow[" + i + "]";

      commandBuilder.append("#Overflow", "ItemSlotPreset.ui");
    }
  }

  @Override
  public void handleDataEvent(
      Ref<EntityStore> ref, Store<EntityStore> store, BenchMortarData data) {}

  @NullMarked
  @ModelBuilder
  public static class BenchMortarData {
    public static final BuilderCodec<BenchMortarData> CODEC =
        CodecUtil.modelBuilder(BenchMortarData.class, Plugin.getSharedResolver());

    public BenchMortarData() {
      ClickedSlot = "invalid";
    }

    public String ClickedSlot;

    public String getClickedSlot() {
      return ClickedSlot;
    }
  }
}
