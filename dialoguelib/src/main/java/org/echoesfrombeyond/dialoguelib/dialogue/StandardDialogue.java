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

package org.echoesfrombeyond.dialoguelib.dialogue;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.List;
import org.echoesfrombeyond.annotation.RunOnWorldThread;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.Data;
import org.echoesfrombeyond.codechelper.annotation.Doc;
import org.echoesfrombeyond.codechelper.annotation.Id;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.dialoguelib.DialoguePlugin;
import org.echoesfrombeyond.dialoguelib.choice.DialogueChoice;
import org.echoesfrombeyond.dialoguelib.ui.StandardDialogueUI;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@Doc(
    """
    Dialogue implementation that is UI-based. Presents a "line" (e.g.
    what the NPC is saying) and a list of options that may be chosen.
    """)
@NullMarked
@ModelBuilder
@SuppressWarnings("FieldMayBeFinal")
public class StandardDialogue implements Dialogue {
  public static final AssetBuilderCodec<String, StandardDialogue> CODEC =
      CodecUtil.modelAssetBuilder(
          StandardDialogue.class, DialoguePlugin.getResolver(), Plugin.getSharedCache());

  @Id private @Nullable String Id;
  @Data private AssetExtraInfo.@Nullable Data Data;

  @Doc(
      """
      The "dialogue line", representing what the NPC is currently
      saying. If absent, nothing will be displayed.
      """)
  public @Nullable DialogueChoice Line;

  @Doc(
      """
      Choices to potentially display. Choices may be "conditional" and
      will only show up if their conditions are met.
      """)
  public List<DialogueChoice> Choices;

  @Doc(
      """
      The UI to render. It must contain a group with the tag
      #DialogueIdentifier. All dialogue choices will be dynamically
      appended to this group. It must also contain a group with the
      tag #DialogueLine.
      """)
  public String UiPageName;

  public StandardDialogue() {
    this.Choices = new ArrayList<>();
    this.UiPageName = "";
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

  @Override
  public void setId(String id) {
    this.Id = id;
  }

  @Override
  public void setData(AssetExtraInfo.@Nullable Data data) {
    this.Data = data;
  }

  @Override
  public AssetExtraInfo.@Nullable Data getData() {
    return Data;
  }

  @Override
  public @Nullable String getId() {
    return Id;
  }
}
