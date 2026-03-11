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
import org.echoesfrombeyond.annotation.RunOnWorldThread;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.Doc;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.dialoguelib.DialoguePlugin;
import org.echoesfrombeyond.dialoguelib.choice.DialogueChoice;
import org.echoesfrombeyond.dialoguelib.dialogue.Dialogue;
import org.echoesfrombeyond.dialoguelib.metadata.IntegerMetadata;
import org.echoesfrombeyond.dialoguelib.metadata.MetadataAccessor;
import org.jspecify.annotations.NullMarked;

@Doc(
    """
    ChoiceCondition that checks if the value of some integer metadata
    is less than, greater than, or equal to a specified value. This
    condition will always fail if the metadata exists and isn't an
    integer.
    """)
@NullMarked
@ModelBuilder
public class CompareIntegerCondition extends MetadataAccessor implements ChoiceCondition {
  public static final BuilderCodec<CompareIntegerCondition> CODEC =
      CodecUtil.modelBuilder(
          CompareIntegerCondition.class,
          MetadataAccessor.CODEC,
          DialoguePlugin.getResolver(),
          Plugin.getSharedCache());

  public enum Compare {
    LessThan,
    GreaterThan,
    LessThanOrEqualTo,
    GreaterThanOrEqualTo,
    EqualTo
  }

  @Doc(
      """
      The comparison type; for example, if LessThan, the condition
      will check [metadata] < Value. If unspecified, the default is
      EqualTo.
      """)
  public Compare Comparison;

  @Doc(
      """
      The value to compare against. If unspecified, defaults to 0.
      This is always on the right side of the operator specified by
      Comparison.
      """)
  public int Value;

  @Doc(
      """
      If set to true, this choice will display when the metadata value
      is missing. If unspecified, defaults to false.
      """)
  public boolean AbsentShouldDisplay;

  public CompareIntegerCondition() {
    this.Comparison = Compare.EqualTo;
  }

  @Override
  @RunOnWorldThread
  public boolean shouldDisplay(Ref<EntityStore> activator, Dialogue parent, DialogueChoice choice) {
    var value = getMetadata(activator, parent);
    if (value == null) return AbsentShouldDisplay;

    if (!(value instanceof IntegerMetadata integerMetadata)) return false;

    return switch (Comparison) {
      case LessThan -> integerMetadata.Value < Value;
      case GreaterThan -> integerMetadata.Value > Value;
      case LessThanOrEqualTo -> integerMetadata.Value <= Value;
      case GreaterThanOrEqualTo -> integerMetadata.Value >= Value;
      case EqualTo -> integerMetadata.Value == Value;
    };
  }
}
