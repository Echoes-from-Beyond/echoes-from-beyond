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

// TODO: check why the links are so ugly for this one
/**
 * Defines storage of metadata between stores (through {@code MetadataStoreKey}) and inside a store
 * (through {@code MetadataKey}). Inherited by various {@link
 * org.echoesfrombeyond.dialoguelib.action.ChoiceAction} to save persistent metadata to a player,
 * and {@link org.echoesfrombeyond.dialoguelib.condition.ChoiceCondition} to read and evaluate them.
 */
@NullMarked
@ModelBuilder
public abstract class MetadataAccessor {
  public static final String ASSET_STORE_KEY_PREFIX = "$$";

  public static final BuilderCodec<MetadataAccessor> CODEC =
      CodecUtil.modelBuilder(
          MetadataAccessor.class, DialoguePlugin.getResolver(), Plugin.getSharedCache());

  @Doc(
      """
      The metadata "store key". If unspecified, all metadata values
      will be "local" to the dialogue asset in which they are defined.

      Setting this value to something other than the default allows
      separate dialogue to access the same metadata.

      Keys that start with '$$', followed by a (case insensitive)
      dialogue identifier, will point to the metadata of that dialogue
      asset.
      """)
  public @Nullable String MetadataStoreKey;

  @Doc(
      """
      The key used to look up the metadata in the store. If left
      absent, attempting to read or write metadata will do nothing.
      """)
  public @Nullable String MetadataKey;

  private static String formatId(String dialogueId) {
    var builder = new StringBuilder(ASSET_STORE_KEY_PREFIX.length() + dialogueId.length());
    builder.append(ASSET_STORE_KEY_PREFIX);

    // this is done instead of String#toLowerCase because we want to mimic the way Hytale's
    // case-insensitive hash strategy works, warts and all
    for (int i = 0; i < dialogueId.length(); i++)
      builder.append(Character.toLowerCase(dialogueId.charAt(i)));

    return builder.toString();
  }

  private @Nullable String readStoreKey() {
    var current = MetadataStoreKey;
    if (current == null) return null;

    if (!current.startsWith(ASSET_STORE_KEY_PREFIX)) return current;

    var builder = new StringBuilder(current.length() - ASSET_STORE_KEY_PREFIX.length());
    for (int i = ASSET_STORE_KEY_PREFIX.length(); i < current.length(); i++)
      builder.append(Character.toLowerCase(current.charAt(i)));

    return builder.toString();
  }

  /**
   * Retrieves metadata that was stored for the calling entity (usually a player). If no {@code
   * MetadataStoreKey} was defined, it checks for locally-defined metadata. Returns early if:<br>
   * - no {@code MetadataKey} was passed in<br>
   * - the activating entity's {@link DialogueComponent} could not be found <br>
   * - no {@code MetadataStoreKey} was passed in <i>and</i> an alternative, locally-defined metadata
   * key could not be made.
   *
   * @param activator Reference to the entity that is interacting with this dialogue
   * @param parent The dialogue containing the asset that called this function
   * @return A {@link DialogueMetadata} of any type, if found. Otherwise, {@code null}.
   */
  @RunOnWorldThread
  public @Nullable DialogueMetadata getMetadata(Ref<EntityStore> activator, Dialogue parent) {
    var key = MetadataKey;
    if (key == null) return null;

    var component =
        activator.getStore().getComponent(activator, DialogueComponent.getComponentType());
    if (component == null) return null;

    var storeKey = readStoreKey();
    var metadataStore =
        component.getMetadataStore(storeKey == null ? formatId(parent.getId()) : storeKey);
    if (metadataStore == null) return null;

    return metadataStore.get(key);
  }

  /**
   * Inserts new metadata, or replaces one that was already stored for the calling entity (usually a
   * player). If no {@code MetadataStoreKey} was defined, it falls back on locally-defined metadata.
   * Returns early if:<br>
   * - no {@code MetadataKey} was passed in<br>
   * - the activating entity's {@link DialogueComponent} could not be found <br>
   * - no {@code MetadataStoreKey} was passed in <i>and</i> an alternative, locally-defined metadata
   * key could not be made.
   *
   * @param activator Reference to the entity that is interacting with this dialogue
   * @param parent The dialogue containing the asset that called this function
   * @return A {@link DialogueMetadata} that was replaced during this process. If this was a new
   *     insert, or the above 'do-nothing' criteria were met, returns {@code null}.
   */
  @RunOnWorldThread
  public @Nullable DialogueMetadata putMetadata(
      Ref<EntityStore> activator, Dialogue parent, @Nullable DialogueMetadata metadata) {
    var key = MetadataKey;
    if (key == null) return null;

    var storeKey = readStoreKey();
    var actualStoreKey = storeKey == null ? formatId(parent.getId()) : storeKey;

    var component =
        activator.getStore().ensureAndGetComponent(activator, DialogueComponent.getComponentType());

    var metadataStore = component.getMetadataStore(actualStoreKey);
    if (metadataStore == null)
      component.putMetadataStore(actualStoreKey, metadataStore = new DialogueMetadataStore());

    return metadata == null ? metadataStore.remove(key) : metadataStore.put(key, metadata);
  }
}
