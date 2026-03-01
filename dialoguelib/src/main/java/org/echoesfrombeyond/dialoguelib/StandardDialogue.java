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

package org.echoesfrombeyond.dialoguelib;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Collections;
import java.util.List;
import org.echoesfrombeyond.annotation.RunOnWorldThread;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.dialoguelib.ui.StandardDialogueUI;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

@NullMarked
@SuppressWarnings("FieldMayBeFinal")
@ModelBuilder
public class StandardDialogue extends IdentifiedAssetBase<String> implements Dialogue {
  public static final BuilderCodec<StandardDialogue> CODEC =
      CodecUtil.modelBuilder(
          StandardDialogue.class, DialoguePlugin.getResolver(), Plugin.getSharedCache());

  private List<DialogueChoice> Choices;

  public StandardDialogue() {
    this.Choices = List.of();
  }

  @Override
  @RunOnWorldThread
  public void display(Ref<EntityStore> activator) {
    var store = activator.getStore();
    var player = store.getComponent(activator, Player.getComponentType());
    var playerRef = store.getComponent(activator, PlayerRef.getComponentType());

    // standard dialogue only works for player activators that can be sent packets
    if (player == null || playerRef == null) return;

    var pageManager = player.getPageManager();
    var customPage = pageManager.getCustomPage();

    // close the current page if it's the same type
    if (customPage instanceof StandardDialogueUI) pageManager.setPage(activator, store, Page.None);

    pageManager.openCustomPage(activator, store, new StandardDialogueUI(playerRef, this));
  }

  public @Unmodifiable List<DialogueChoice> getChoices() {
    return Collections.unmodifiableList(Choices);
  }
}
