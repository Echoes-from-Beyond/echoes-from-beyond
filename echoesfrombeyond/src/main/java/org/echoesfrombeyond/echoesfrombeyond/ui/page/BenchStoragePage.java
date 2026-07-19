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
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.echoesfrombeyond.echoesfrombeyond.component.chunk.AlchemyStorage;
import org.echoesfrombeyond.echoesfrombeyond.ui.data.GenericBenchData;
import org.echoesfrombeyond.modutil.component.ComponentUtils;
import org.joml.Vector3i;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class BenchStoragePage extends InteractiveCustomUIPage<GenericBenchData> {
  private final Vector3i position;

  public BenchStoragePage(PlayerRef playerRef, Vector3i position) {
    super(playerRef, CustomPageLifetime.CanDismiss, GenericBenchData.CODEC);
    this.position = position;
  }

  @Override
  public void build(
      Ref<EntityStore> ref,
      UICommandBuilder commandBuilder,
      UIEventBuilder eventBuilder,
      Store<EntityStore> store) {
    var storageBlock =
        ComponentUtils.getBlockComponent(
            store.getExternalData().getWorld(), position, AlchemyStorage.getComponentType());
    if (storageBlock == null) return;

    buildInternal(ref, store, commandBuilder, eventBuilder, storageBlock);
  }

  private void buildInternal(
      Ref<EntityStore> ref,
      Store<EntityStore> store,
      UICommandBuilder commandBuilder,
      UIEventBuilder eventBuilder,
      AlchemyStorage storageBlock) {
    commandBuilder.append("BenchStoragePage.ui");

    var storage = storageBlock.ItemsStored;
  }

  @Override
  public void handleDataEvent(
      Ref<EntityStore> ref, Store<EntityStore> store, GenericBenchData data) {}
}
