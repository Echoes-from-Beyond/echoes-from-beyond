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

package org.echoesfrombeyond.dialoguelib.ui;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.dialoguelib.DialoguePlugin;
import org.echoesfrombeyond.dialoguelib.dialogue.StandardDialogue;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class StandardDialogueUI extends InteractiveCustomUIPage<StandardDialogueUI.Data> {
  private final StandardDialogue dialogue;

  public StandardDialogueUI(PlayerRef playerRef, StandardDialogue dialogue) {
    super(playerRef, CustomPageLifetime.CantClose, Data.CODEC);
    this.dialogue = dialogue;
  }

  @Override
  public void build(
      Ref<EntityStore> ref,
      UICommandBuilder uiCommandBuilder,
      UIEventBuilder uiEventBuilder,
      Store<EntityStore> store) {
    uiCommandBuilder.append(dialogue.UiPageName);

    var line = dialogue.Line;
    if (line != null) {
      uiCommandBuilder.appendInline("#DialogueLine", "Label #Line { }");
      uiCommandBuilder.set("#Line.Text", line.getMessage(ref, dialogue));
    }

    var count = 0;
    for (var choice : dialogue.Choices) {
      int choiceIndex = count++;

      if (!choice.shouldDisplay(ref, dialogue)) continue;

      var message = choice.getMessage(ref, dialogue);
      var selector = "#Choice" + choiceIndex;

      uiCommandBuilder.appendInline("#DialogueContainer", String.format("Label %s { }", selector));
      uiCommandBuilder.set(selector + ".Text", message);
      uiEventBuilder.addEventBinding(
          CustomUIEventBindingType.Activating,
          selector,
          EventData.of("Choice", Integer.toString(choiceIndex)));
    }
  }

  @Override
  public void handleDataEvent(
      Ref<EntityStore> ref, Store<EntityStore> store, StandardDialogueUI.Data data) {
    var choices = dialogue.Choices;
    int index = data.Choice;

    // Length check in case the client sends a bogus value for Choice.
    if (index < 0 || index >= choices.size()) return;

    var choice = choices.get(index);

    // Recheck if this choice should even be displayed. This is important because the client can
    // send whatever it wants: a hacked client could craft malicious event data to choose a dialogue
    // that shouldn't be available.
    if (choice.shouldDisplay(ref, dialogue)) choice.onChosen(ref, dialogue);
  }

  @SuppressWarnings("unused")
  public static final class Data {
    public static final BuilderCodec<Data> CODEC =
        CodecUtil.modelBuilder(Data.class, DialoguePlugin.getResolver(), Plugin.getSharedCache());

    public int Choice;
  }
}
