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
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.List;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.*;
import org.echoesfrombeyond.dialoguelib.DialoguePlugin;
import org.echoesfrombeyond.dialoguelib.choice.StandardChoice;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@Doc(
    """
    A Dialogue implementation that exists to remove repetition
    from single-choice dialogue (e.g. those where several dialogue
    lines in a row only have "Continue" as a choice).
    """)
@NullMarked
@ModelBuilder
public class ChainDialogue extends UIDialogueBase implements Dialogue {
  @Doc(
      """
      An entry can be understood as mimicking
      StandardDialogue, except considerably simplified.
      You don't manually define any choices for it, and
      the fields require text instead of DialogueChoice
      objects.
      """)
  @ModelBuilder
  public static class Entry {
    @Doc(
        """
        A name to assign to the speaker.
        """)
    public String Name;

    @Doc(
        """
        The text to display above the choices.
        """)
    public String Line;

    @Doc(
        """
        Location of the sprite's image file. Should be
        in UI/Custom or a subdirectory thereof.
        """)
    @Opt
    public @Nullable String Sprite;

    public Entry() {
      this.Name = "";
      this.Line = "";
    }
  }

  public static final AssetBuilderCodec<String, ChainDialogue> CODEC =
      CodecUtil.modelAssetBuilder(
          ChainDialogue.class,
          UIDialogueBase.CODEC,
          DialoguePlugin.getResolver(),
          Plugin.getSharedCache());

  @Id private @Nullable String Id;
  @Data private AssetExtraInfo.@Nullable Data Data;

  @Doc(
      """
      Collection of associated lines, names and sprites.
      Each entry has only one choice, the text of which
      is defined in AdvanceText.
      """)
  public List<Entry> Entries;

  @Doc(
      """
      A dialogue asset displayed at the end of a chain of simple entries.
      Use StandardDialogue, where you can define your own choices.
      """)
  public @Nullable Dialogue End;

  @Doc(
      """
      The text assigned to the single choice that exists for each entry.
      Defaults to "Continue" if unspecified.
      """)
  public String AdvanceText;

  public ChainDialogue() {
    this.Entries = new ArrayList<>();
    this.AdvanceText = "Continue";
  }

  @Override
  public void display(Ref<EntityStore> activator) {
    var entries = Entries;
    var end = End;

    if (entries.isEmpty() || end == null) return;

    var dialogue = new StandardDialogue();
    var first = dialogue;

    // turn each entry into an actual StandardDialogue and assign it an action for moving to other
    // entries
    for (int i = 0; i < entries.size(); i++) {
      init(dialogue, entries.get(i));

      StandardDialogue nextStandard = null;
      var next = i < entries.size() - 1 ? (nextStandard = new StandardDialogue()) : end;

      var advance = new StandardChoice();
      advance.Text = AdvanceText;
      advance.Actions = new ArrayList<>();

      advance.Actions.add((player, _, _) -> next.display(player));

      dialogue.Choices.add(advance);
      dialogue = nextStandard;
    }

    first.display(activator);
  }

  private void init(StandardDialogue dialogue, Entry entry) {
    var nameChoice = new StandardChoice();
    var lineChoice = new StandardChoice();

    nameChoice.Text = entry.Name;
    lineChoice.Text = entry.Line;

    dialogue.Name = nameChoice;
    dialogue.Line = lineChoice;
    dialogue.Sprite = entry.Sprite;

    dialogue.UiPage = UiPage;
    dialogue.UiFragment = UiFragment;
    dialogue.Lifetime = Lifetime;
  }

  @Override
  public void setId(String id) {
    Id = id;
  }

  @Override
  public void setData(AssetExtraInfo.@Nullable Data data) {
    Data = data;
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
