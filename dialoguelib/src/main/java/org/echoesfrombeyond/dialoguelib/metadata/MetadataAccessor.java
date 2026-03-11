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

package org.echoesfrombeyond.dialoguelib.metadata;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.echoesfrombeyond.annotation.RunOnWorldThread;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.Doc;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.dialoguelib.DialoguePlugin;
import org.echoesfrombeyond.dialoguelib.component.DialogueComponent;
import org.echoesfrombeyond.dialoguelib.dialogue.Dialogue;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@ModelBuilder
public abstract class MetadataAccessor {
  public static final BuilderCodec<MetadataAccessor> CODEC =
      CodecUtil.modelBuilder(
          MetadataAccessor.class, DialoguePlugin.getResolver(), Plugin.getSharedCache());

  @Doc(
      """
      The metadata "store key". If unspecified, all metadata values
      will be "local" to the dialogue asset. This is equivalent to
      having the group key set to the dialogue asset's ID key.

      Setting this value to something other than the default allows
      separate dialogue to access the same metadata.
      """)
  public @Nullable String MetadataStoreKey;

  @Doc(
      """
      The key used to look up the metadata in the store. If left
      absent, attempting to read or write metadata will do nothing.
      """)
  public @Nullable String MetadataKey;

  @RunOnWorldThread
  public @Nullable DialogueMetadata getMetadata(Ref<EntityStore> activator, Dialogue parent) {
    var key = MetadataKey;
    if (key == null) return null;

    var component =
        activator.getStore().getComponent(activator, DialogueComponent.getComponentType());
    if (component == null) return null;

    var storeKey = MetadataStoreKey;
    var metadataStore = component.getMetadataStore(storeKey == null ? parent.getId() : storeKey);
    if (metadataStore == null) return null;

    return metadataStore.get(key);
  }

  @RunOnWorldThread
  public @Nullable DialogueMetadata putMetadata(
      Ref<EntityStore> activator, Dialogue parent, @Nullable DialogueMetadata metadata) {
    var key = MetadataKey;
    if (key == null) return null;

    var storeKey = MetadataStoreKey;
    var actualStoreKey = storeKey == null ? parent.getId() : storeKey;

    var component =
        activator.getStore().ensureAndGetComponent(activator, DialogueComponent.getComponentType());

    var metadataStore = component.getMetadataStore(actualStoreKey);
    if (metadataStore == null)
      component.putMetadataStore(actualStoreKey, metadataStore = new DialogueMetadataStore());

    return metadata == null ? metadataStore.remove(key) : metadataStore.put(key, metadata);
  }
}
