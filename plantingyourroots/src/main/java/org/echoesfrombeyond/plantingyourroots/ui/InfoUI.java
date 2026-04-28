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

package org.echoesfrombeyond.plantingyourroots.ui;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class InfoUI extends InteractiveCustomUIPage<InfoUI.Data> {
  @ModelBuilder
  public static class Data {
    public static final BuilderCodec<Data> CODEC =
        CodecUtil.modelBuilder(Data.class, Plugin.getSharedResolver(), Plugin.getSharedCache());
  }

  private final String title;
  private final String message;

  public InfoUI(PlayerRef playerRef, String title, String message) {
    super(playerRef, CustomPageLifetime.CantClose, Data.CODEC);
    this.title = title;
    this.message = message;
  }

  @Override
  public void build(
      Ref<EntityStore> ref,
      UICommandBuilder uiCommandBuilder,
      UIEventBuilder uiEventBuilder,
      Store<EntityStore> store) {
    uiCommandBuilder.append("Info.ui");
    uiCommandBuilder.set("#TitleLabel.Text", title);
    uiCommandBuilder.set("#MessageLabel.Text", message);

    uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton");
  }

  @Override
  public void handleDataEvent(Ref<EntityStore> ref, Store<EntityStore> store, InfoUI.Data data) {
    var player = store.getComponent(ref, Player.getComponentType());
    if (player != null) player.getPageManager().setPage(ref, store, Page.None);
    sendUpdate();
  }
}
