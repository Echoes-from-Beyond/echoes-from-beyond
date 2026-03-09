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
import java.util.Objects;
import org.echoesfrombeyond.annotation.RunOnWorldThread;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.Doc;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.dialoguelib.DialoguePlugin;
import org.echoesfrombeyond.dialoguelib.choice.DialogueChoice;
import org.echoesfrombeyond.dialoguelib.component.DialogueComponent;
import org.echoesfrombeyond.dialoguelib.dialogue.Dialogue;
import org.echoesfrombeyond.dialoguelib.metadata.DialogueMetadata;
import org.echoesfrombeyond.dialoguelib.metadata.MetadataAccessor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@ModelBuilder
public class MetadataEqualsCondition extends MetadataAccessor implements ChoiceCondition {
  public static final BuilderCodec<MetadataEqualsCondition> CODEC =
      CodecUtil.modelBuilder(
          MetadataEqualsCondition.class,
          MetadataAccessor.CODEC,
          DialoguePlugin.getResolver(),
          Plugin.getSharedCache());

  @Doc(
      """
      The metadata value to compare against. If left unset, checks
      if the actual metadata value is unset.
      """)
  public @Nullable DialogueMetadata Metadata;

  @Override
  @RunOnWorldThread
  public boolean shouldDisplay(Ref<EntityStore> activator, Dialogue parent, DialogueChoice choice) {
    var component =
        activator.getStore().getComponent(activator, DialogueComponent.getComponentType());
    if (component == null) return false;

    var storeKey = MetadataStoreKey;
    var metadataStore = component.getMetadataStore(storeKey == null ? parent.getId() : storeKey);
    if (metadataStore == null) return false;

    return Objects.equals(metadataStore.get(MetadataKey), Metadata);
  }
}
