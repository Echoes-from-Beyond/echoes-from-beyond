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
import java.util.OptionalInt;
import org.echoesfrombeyond.annotation.RunOnWorldThread;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.Doc;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.codechelper.annotation.Opt;
import org.echoesfrombeyond.dialoguelib.DialoguePlugin;
import org.echoesfrombeyond.dialoguelib.choice.DialogueChoice;
import org.echoesfrombeyond.dialoguelib.dialogue.Dialogue;
import org.echoesfrombeyond.dialoguelib.metadata.IntegerMetadata;
import org.echoesfrombeyond.dialoguelib.metadata.MetadataAccessor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@Doc(
    """
    ChoiceCondition that checks if the value of some integer metadata
    is less than, greater than, or equal to a specified value. This
    condition will always fail if the metadata exists and isn't an
    integer.

    May be used to compare metadata against a constant value, or some
    other metadata.
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

  @Doc(
      """
      The comparison type; for example, if LessThan, the condition
      will check [metadata] < Value. If unspecified, the default is
      EqualTo.

      If the metadata pointed at by StoreKey and MetadataKey exists
      but is not the right type, this condition will always return
      false.
      """)
  public BooleanCompare Comparison;

  @Doc(
      """
      The value to compare against. If unspecified, defaults to 0.
      This is always on the right side of the operator specified by
      Comparison.

      If OtherMetadataKey is set, and it points to a metadata key that
      is also an integer, then the value of that other metadata will
      be used as the right-hand side of the comparison operation.
      """)
  public int Value;

  @Doc(
      """
      If set to true, this choice will display when the metadata value
      is missing. If unspecified, defaults to false.

      This is only considered for the first (left-hand side) metadata.
      If OtherMetadataKey is non-null but the value does not exist,
      the condition will always evaluate to false.
      """)
  @Opt
  public boolean AbsentShouldDisplay;

  @Doc(
      """
      The store key of the other metadata to compare against. Works
      the same as StoreKey does.
      """)
  @Opt
  public @Nullable String OtherMetadataStoreKey;

  @Doc(
      """
      The key of the other metadata to compare against. Works the
      same as MetadataKey does.

      If unspecified, will use the constant Value as the right-hand
      side of the comparison operation. If specified, but the actual
      metadata is missing or not the right type, the comparison will
      always evaluate to false.
      """)
  @Opt
  public @Nullable String OtherMetadataKey;

  public CompareIntegerCondition() {
    this.Comparison = BooleanCompare.EqualTo;
  }

  @Override
  @RunOnWorldThread
  public boolean shouldDisplay(Ref<EntityStore> activator, Dialogue parent, DialogueChoice choice) {
    var value = getMetadata(activator, parent);
    if (value == null) return AbsentShouldDisplay;

    if (!(value instanceof IntegerMetadata integerMetadata)) return false;

    var otherMetadataStoreKey = OtherMetadataStoreKey;
    var otherMetadataKey = OtherMetadataKey;

    OptionalInt otherValueOptional =
        otherMetadataKey == null
            ? OptionalInt.of(Value)
            : (new MetadataAccessor() {
                      {
                        MetadataStoreKey = otherMetadataStoreKey;
                        MetadataKey = otherMetadataKey;
                      }
                    }.getMetadata(activator, parent)
                    instanceof IntegerMetadata otherIntegerMetadata
                ? OptionalInt.of(otherIntegerMetadata.Value)
                : OptionalInt.empty());

    // return false when the other metadata has its key specified but does not exist, or if it does
    // exist and is the wrong type
    if (otherValueOptional.isEmpty()) return false;

    return Comparison.compare(integerMetadata.Value, otherValueOptional.getAsInt());
  }
}
