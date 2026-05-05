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
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.PatchStyle;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.OptionalInt;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.dialoguelib.DialoguePlugin;
import org.echoesfrombeyond.dialoguelib.dialogue.StandardDialogue;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Sets up bindings for and serves the UI for dialogue that uses several "standard" functionalities:
 * <br>
 * - line display (i.e. text not directly associated with choice selection)<br>
 * - name display for the speaker, e.g. the character a player is talking with, "Narrator", etc.<br>
 * - button-based choices that each have their own label (text)<br>
 * - a sprite (image that's displayed on the screen during dialogue).<br>
 * <br>
 * Not all of these need to be used, but the dialogue must be constrained to them. Additionally,
 * this UI ensures that all choices are numbered.
 */
@NullMarked
public class StandardDialogueUI extends InteractiveCustomUIPage<StandardDialogueUI.Data> {
  public static final String DIALOGUE_LINE_SELECTOR = "#DialogueLine";
  public static final String DIALOGUE_NAME_SELECTOR = "#DialogueName";
  public static final String DIALOGUE_CHOICES_SELECTOR = "#DialogueChoices";
  public static final String DIALOGUE_LABEL_SELECTOR = "#DialogueLabel";
  public static final String DIALOGUE_BUTTON_SELECTOR = "#DialogueButton";
  public static final String SPRITE_SELECTOR = "#Sprite";

  private final StandardDialogue dialogue;

  public StandardDialogueUI(PlayerRef playerRef, StandardDialogue dialogue) {
    super(playerRef, dialogue.Lifetime, Data.CODEC);
    this.dialogue = dialogue;
  }

  @Override
  public void build(
      Ref<EntityStore> ref,
      UICommandBuilder uiCommandBuilder,
      UIEventBuilder uiEventBuilder,
      Store<EntityStore> store) {
    // the dialogue asset defines which UI page is shown
    uiCommandBuilder.append(dialogue.UiPage);

    var line = dialogue.Line;
    var name = dialogue.Name;
    var sprite = dialogue.Sprite;

    // only display line, name, sprite if they are defined in the dialogue
    if (line != null)
      uiCommandBuilder.set(DIALOGUE_LINE_SELECTOR + ".Text", line.getMessage(ref, dialogue));

    if (name != null)
      uiCommandBuilder.set(DIALOGUE_NAME_SELECTOR + ".Text", name.getMessage(ref, dialogue));

    if (sprite != null) {
      var patchStyle = new PatchStyle();
      patchStyle.setTexturePath(Value.of(sprite));

      uiCommandBuilder.setObject(SPRITE_SELECTOR + ".Background", patchStyle);
    }

    // likewise with choices
    // since 0-based indexing is generally unintuitive for people, a separate counter keeps track of
    // the natural 1-based
    // indexing to display with each choice
    var count = 0;
    var prefixCount = 0;
    for (var choice : dialogue.Choices) {
      int choiceIndex = count++;

      if (!choice.shouldDisplay(ref, dialogue)) continue;

      var message = (++prefixCount + ". ") + choice.getMessage(ref, dialogue);
      var indexSelector = DIALOGUE_CHOICES_SELECTOR + "[" + (prefixCount - 1) + "]";

      uiCommandBuilder.append(DIALOGUE_CHOICES_SELECTOR, dialogue.UiFragment);

      uiCommandBuilder.set(indexSelector + " " + DIALOGUE_LABEL_SELECTOR + ".Text", message);

      // the index of the choice is sent over as a string value to the client
      // this is because of a limitation with the API
      uiEventBuilder.addEventBinding(
          CustomUIEventBindingType.Activating,
          indexSelector + " " + DIALOGUE_BUTTON_SELECTOR,
          EventData.of("Choice", Integer.toString(choiceIndex)));
    }
  }

  private void handleData(Ref<EntityStore> ref, StandardDialogueUI.Data data) {
    var indexOptional = data.getChoice();
    if (indexOptional.isEmpty()) return;

    int index = indexOptional.getAsInt();

    var choices = dialogue.Choices;

    // Length check in case the client sends a bogus value for Choice.
    if (index < 0 || index >= choices.size()) return;

    var choice = choices.get(index);

    // Recheck if this choice should even be displayed. This is important because the client can
    // send whatever it wants: a hacked client could craft malicious event data to choose a dialogue
    // that shouldn't be available.
    if (choice.shouldDisplay(ref, dialogue)) choice.onChosen(ref, dialogue);
  }

  @Override
  public void handleDataEvent(
      Ref<EntityStore> ref, Store<EntityStore> store, StandardDialogueUI.Data data) {
    handleData(ref, data);
    sendUpdate();
  }

  /**
   * Event object for {@link StandardDialogueUI}. Contains the index of the choice selected by the
   * player.<br>
   * There is/was a limitation in the API where it expected a client's response to only be a string,
   * which this accounts for. The {@code getChoice} method converts the response back into an
   * integer.
   */
  @SuppressWarnings("unused")
  @ModelBuilder
  public static final class Data {
    public static final BuilderCodec<Data> CODEC =
        CodecUtil.modelBuilder(Data.class, DialoguePlugin.getResolver(), Plugin.getSharedCache());

    public @Nullable String Choice;

    /**
     * Attempts to convert the value returned by the client into an integer.
     *
     * @return An {@link OptionalInt} containing the integer if parsing was successful, {@code null}
     *     otherwise.
     */
    public OptionalInt getChoice() {
      var choice = Choice;
      if (choice == null || choice.length() > 16) return OptionalInt.empty();

      try {
        return OptionalInt.of(Integer.parseInt(Choice));
      } catch (NumberFormatException _) {
        return OptionalInt.empty();
      }
    }
  }
}
