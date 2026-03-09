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
import java.util.List;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.Doc;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.dialoguelib.DialoguePlugin;
import org.echoesfrombeyond.dialoguelib.choice.DialogueChoice;
import org.echoesfrombeyond.dialoguelib.dialogue.Dialogue;
import org.jspecify.annotations.NullMarked;

@Doc(
    """
    ChoiceAction that sequentially executes a series of child actions.
    """)
@NullMarked
@ModelBuilder
public class CompositeAction implements ChoiceAction {
  public static final BuilderCodec<CompositeAction> CODEC =
      CodecUtil.modelBuilder(
          CompositeAction.class, DialoguePlugin.getResolver(), Plugin.getSharedCache());

  @Doc(
      """
      The list of actions to run when this one is activated. These are
      executed in-order.
      """)
  public List<ChoiceAction> Actions;

  public CompositeAction() {
    this.Actions = List.of();
  }

  @Override
  public void onChosen(Ref<EntityStore> activator, Dialogue parent, DialogueChoice choice) {
    var choices = Actions;
    for (var child : choices) child.onChosen(activator, parent, choice);
  }
}
