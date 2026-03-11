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

package org.echoesfrombeyond.dialoguelib.action;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.echoesfrombeyond.annotation.RunOnWorldThread;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.Doc;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.dialoguelib.DialoguePlugin;
import org.echoesfrombeyond.dialoguelib.choice.DialogueChoice;
import org.echoesfrombeyond.dialoguelib.condition.ChoiceCondition;
import org.echoesfrombeyond.dialoguelib.dialogue.Dialogue;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@Doc(
    """
    ChoiceAction that only activates a delegate if a condition is met.
    """)
@NullMarked
@ModelBuilder
public class ConditionalAction implements ChoiceAction {
  public static final BuilderCodec<ConditionalAction> CODEC =
      CodecUtil.modelBuilder(
          ConditionalAction.class, DialoguePlugin.getResolver(), Plugin.getSharedCache());

  @Doc(
      """
      The ChoiceCondition. If absent, the condition is always
      considered to be met.
      """)
  public @Nullable ChoiceCondition Condition;

  @Doc(
      """
      ChoiceAction to execute if the condition is met. If absent,
      nothing will happen if the condition is met.
      """)
  public @Nullable ChoiceAction IfMet;

  @Doc(
      """
      ChoiceAction to execute if the condition is not met. If absent,
      nothing will happen if the condition is not met.
      """)
  public @Nullable ChoiceAction IfNotMet;

  @Override
  @RunOnWorldThread
  public void onChosen(Ref<EntityStore> activator, Dialogue parent, DialogueChoice choice) {
    var condition = Condition;
    var met = IfMet;
    var notMet = IfNotMet;

    var shouldDisplay = condition == null || condition.shouldDisplay(activator, parent, choice);

    if (shouldDisplay) {
      if (met != null) met.onChosen(activator, parent, choice);
    } else if (notMet != null) notMet.onChosen(activator, parent, choice);
  }
}
