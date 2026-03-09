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
import org.echoesfrombeyond.dialoguelib.DialoguePlugin;
import org.echoesfrombeyond.dialoguelib.choice.DialogueChoice;
import org.echoesfrombeyond.dialoguelib.component.DialogueComponent;
import org.echoesfrombeyond.dialoguelib.dialogue.Dialogue;
import org.echoesfrombeyond.dialoguelib.metadata.DialogueMetadata;
import org.echoesfrombeyond.dialoguelib.metadata.DialogueMetadataStore;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class SetMetadataAction implements ChoiceAction {
  public static final BuilderCodec<SetMetadataAction> CODEC =
      CodecUtil.modelBuilder(
          SetMetadataAction.class, DialoguePlugin.getResolver(), Plugin.getSharedCache());

  @Doc(
      """
      The metadata "group key". If unspecified, all metadata values
      will be "local" to the dialogue asset. This is equivalent to
      having the group key set to the dialogue asset's ID key.
      """)
  public @Nullable String MetadataGroupKey;

  @Doc(
      """
      The key associated with the metadata.
      """)
  public String MetadataKey;

  @Doc(
      """
      The metadata value to set.
      """)
  public @Nullable DialogueMetadata Metadata;

  public SetMetadataAction() {
    this.MetadataKey = "";
  }

  @Override
  @RunOnWorldThread
  public void onChosen(Ref<EntityStore> activator, Dialogue parent, DialogueChoice choice) {
    var meta = Metadata;
    if (meta == null) return;

    var specificKey = MetadataGroupKey;
    var key = specificKey == null ? parent.getId() : specificKey;

    var component =
        activator.getStore().ensureAndGetComponent(activator, DialogueComponent.getComponentType());

    var metadata = component.getMetadata(key);
    if (metadata == null) component.putMetadata(key, metadata = new DialogueMetadataStore());

    metadata.put(MetadataKey, meta);
  }
}
