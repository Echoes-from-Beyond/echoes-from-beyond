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

package org.echoesfrombeyond.dialoguelib.choice;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.Doc;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.dialoguelib.*;
import org.echoesfrombeyond.dialoguelib.action.ChoiceAction;
import org.echoesfrombeyond.dialoguelib.condition.ChoiceCondition;
import org.echoesfrombeyond.dialoguelib.dialogue.Dialogue;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@ModelBuilder
@Doc(
    """
    Standard DialogueChoice implementation. Accepts an optional
    condition to determine if the choice should be shown, and an
    action to take when the choice is selected.
    """)
public class StandardChoice implements DialogueChoice {
  public static final BuilderCodec<StandardChoice> CODEC =
      CodecUtil.modelBuilder(
          StandardChoice.class, DialoguePlugin.getResolver(), Plugin.getSharedCache());

  @Doc("The text to display as part of the choice.")
  public String Text;

  @Doc(
      """
      Condition determining if this choice should appear. If left
      absent, this choice will always be available.
      """)
  public @Nullable ChoiceCondition Condition;

  @Doc(
      """
      Action that is taken when the choice is selected (e.g. clicked
      if the dialogue is UI-based.) If left absent, selecting the
      choice will do nothing.
      """)
  public @Nullable ChoiceAction Action;

  public StandardChoice() {
    this.Text = "";
  }

  @Override
  public String getMessage(Ref<EntityStore> activator, Dialogue parent) {
    return Text;
  }

  @Override
  public void onChosen(Ref<EntityStore> activator, Dialogue parent) {
    var action = Action;
    if (action != null) action.onChosen(activator, parent, this);
  }

  @Override
  public boolean shouldDisplay(Ref<EntityStore> activator, Dialogue parent) {
    var condition = Condition;
    if (condition == null) return true;

    return condition.shouldDisplay(activator, parent, this);
  }
}
