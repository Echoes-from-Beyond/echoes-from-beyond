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
import org.echoesfrombeyond.dialoguelib.dialogue.Dialogue;
import org.echoesfrombeyond.dialoguelib.metadata.IntegerMetadata;
import org.echoesfrombeyond.dialoguelib.metadata.MetadataAccessor;
import org.jspecify.annotations.NullMarked;

@Doc(
    """
    ChoiceAction that can add or subtract from integer metadata.
    """)
@NullMarked
@ModelBuilder
public class AddIntegerMetadataAction extends MetadataAccessor implements ChoiceAction {
  public static final BuilderCodec<AddIntegerMetadataAction> CODEC =
      CodecUtil.modelBuilder(
          AddIntegerMetadataAction.class,
          MetadataAccessor.CODEC,
          DialoguePlugin.getResolver(),
          Plugin.getSharedCache());

  @Doc(
      """
      How much to adjust the metadata by. If unspecified, increases
      the value by 1.
      """)
  public int Delta;

  @Doc(
      """
      If the metadata entry does not exist yet, it is initialized to
      this value. If unspecified, the initial value will be 0.
      """)
  public int Initial;

  public AddIntegerMetadataAction() {
    this.Delta = 1;
  }

  @Override
  @RunOnWorldThread
  public void onChosen(Ref<EntityStore> activator, Dialogue parent, DialogueChoice choice) {
    var metadata = getMetadata(activator, parent);
    if (metadata == null) putMetadata(activator, parent, metadata = new IntegerMetadata(Initial));

    if (!(metadata instanceof IntegerMetadata integerMetadata)) return;
    integerMetadata.Value += Delta;
  }
}
