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

package org.echoesfrombeyond.plantingyourroots.action;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.echoesfrombeyond.annotation.RunOnWorldThread;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.dialoguelib.action.ChoiceAction;
import org.echoesfrombeyond.dialoguelib.choice.DialogueChoice;
import org.echoesfrombeyond.dialoguelib.dialogue.Dialogue;
import org.echoesfrombeyond.plantingyourroots.ui.InfoUI;
import org.jspecify.annotations.NullMarked;

@NullMarked
@ModelBuilder
public class OpenInfoUI implements ChoiceAction {
  public static final BuilderCodec<OpenInfoUI> CODEC =
      CodecUtil.modelBuilder(OpenInfoUI.class, Plugin.getSharedResolver(), Plugin.getSharedCache());

  public String Title;
  public String Message;

  public OpenInfoUI() {
    this.Title = "";
    this.Message = "";
  }

  @Override
  @RunOnWorldThread
  public void onChosen(Ref<EntityStore> activator, Dialogue parent, DialogueChoice choice) {
    var store = activator.getStore();
    var playerRef = store.getComponent(activator, PlayerRef.getComponentType());
    var player = store.getComponent(activator, Player.getComponentType());

    if (playerRef == null || player == null) return;

    player.getPageManager().openCustomPage(activator, store, new InfoUI(playerRef, Title, Message));
  }
}
