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

package org.echoesfrombeyond.dialoguelib.condition;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.List;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.Doc;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.codechelper.annotation.Opt;
import org.echoesfrombeyond.dialoguelib.DialoguePlugin;
import org.echoesfrombeyond.dialoguelib.choice.DialogueChoice;
import org.echoesfrombeyond.dialoguelib.dialogue.Dialogue;
import org.jspecify.annotations.NullMarked;

@Doc(
    """
    ChoiceCondition that performs a boolean operation on any
    number of delegate conditions.
    """)
@NullMarked
@ModelBuilder
public class BooleanCondition implements ChoiceCondition {
  public static final BuilderCodec<BooleanCondition> CODEC =
      CodecUtil.modelBuilder(
          BooleanCondition.class, DialoguePlugin.getResolver(), Plugin.getSharedCache());

  public enum Type {
    And,
    Or
  }

  @Doc(
      """
      The type of boolean operation to perform. May be one of
      the following: And, Or. Defaults to And.
      """)
  @Opt
  public Type Kind;

  @Doc(
      """
      The conditions to check. If empty, and Kind is And, this
      condition will always display. If empty, and Kind is Or,
      this condition will never display.
      """)
  public List<ChoiceCondition> Conditions;

  public BooleanCondition() {
    this.Kind = Type.And;
    this.Conditions = new ArrayList<>();
  }

  @Override
  public boolean shouldDisplay(Ref<EntityStore> activator, Dialogue parent, DialogueChoice choice) {
    return switch (Kind) {
      case And -> {
        for (var condition : Conditions)
          if (!condition.shouldDisplay(activator, parent, choice)) yield false;

        yield true;
      }
      case Or -> {
        for (var condition : Conditions)
          if (condition.shouldDisplay(activator, parent, choice)) yield true;

        yield false;
      }
    };
  }
}
