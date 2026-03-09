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

package org.echoesfrombeyond.dialoguelib.component;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.BuilderCodecMapCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.HashMap;
import java.util.Map;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.dialoguelib.DialoguePlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@SuppressWarnings("FieldMayBeFinal")
public class DialogueComponent implements Component<EntityStore> {
  public static final BuilderCodec<DialogueComponent> CODEC =
      CodecUtil.modelBuilder(
          DialogueComponent.class, DialoguePlugin.getResolver(), Plugin.getSharedCache());


  public static final BuilderCodecMapCodec<DialogueMetadata> METADATA_CODEC;

  private static @Nullable ComponentType<EntityStore, DialogueComponent> TYPE;

  static {
    METADATA_CODEC = new BuilderCodecMapCodec<>();

    METADATA_CODEC.register("String", StringMetadata.class, StringMetadata.CODEC);
    METADATA_CODEC.register("Integer", IntegerMetadata.class, IntegerMetadata.CODEC);
    METADATA_CODEC.register("Boolean", BooleanMetadata.class, BooleanMetadata.CODEC);
  }

  @ApiStatus.Internal
  public static void register(ComponentRegistryProxy<EntityStore> proxy) {
    TYPE = proxy.registerComponent(DialogueComponent.class, "DialogueComponent", CODEC);
  }

  public static ComponentType<EntityStore, DialogueComponent> getComponentType() {
    var type = TYPE;
    if (type == null) throw new IllegalStateException("Plugin has not been initialized yet");

    return type;
  }

  private Map<String, DialogueMetadataStore> MetadataStorage;

  public DialogueComponent() {
    this.MetadataStorage = new HashMap<>();
  }

  public DialogueComponent(DialogueComponent other) {
    var newMap = new HashMap<String, DialogueMetadataStore>(other.MetadataStorage.size());

    for (var entry : other.MetadataStorage.entrySet()) {
      var newStore = new DialogueMetadataStore();

      for (var storeEntry : entry.getValue().Metadata.entrySet()) {
        newStore.Metadata.put(storeEntry.getKey(), storeEntry.getValue().clone());
      }

      newMap.put(entry.getKey(), newStore);
    }

    this.MetadataStorage = newMap;
  }

  @Override
  @SuppressWarnings("MethodDoesntCallSuperMethod")
  public Component<EntityStore> clone() {
    return new DialogueComponent(this);
  }

  public @Nullable DialogueMetadataStore getMetadata(String key) {
    return MetadataStorage.get(key);
  }

  public sealed interface DialogueMetadata extends Cloneable permits StringMetadata, IntegerMetadata, BooleanMetadata {
    default String asString() { throw new IllegalStateException(); }

    default int asInteger() { throw new IllegalStateException(); }

    default boolean asBoolean() { throw new IllegalStateException(); }

    DialogueMetadata clone();
  }

  @ModelBuilder
  @SuppressWarnings("FieldMayBeFinal")
  public static final class StringMetadata implements DialogueMetadata {
    private static final BuilderCodec<StringMetadata> CODEC =
        CodecUtil.modelBuilder(
            StringMetadata.class, DialoguePlugin.getResolver(), Plugin.getSharedCache());

    public String Value;

    public StringMetadata() {
      this.Value = "";
    }

    @Override
    public String asString() {
      return Value;
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public DialogueMetadata clone() {
      var newMetadata = new StringMetadata();
      newMetadata.Value = Value;
      return newMetadata;
    }
  }

  @ModelBuilder
  @SuppressWarnings("FieldMayBeFinal")
  public static final class IntegerMetadata implements DialogueMetadata {
    private static final BuilderCodec<IntegerMetadata> CODEC =
        CodecUtil.modelBuilder(
            IntegerMetadata.class, DialoguePlugin.getResolver(), Plugin.getSharedCache());

    public int Value;

    public IntegerMetadata() {
      this.Value = 0;
    }

    @Override
    public int asInteger() {
      return Value;
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public DialogueMetadata clone() {
      var newMetadata = new IntegerMetadata();
      newMetadata.Value = Value;
      return newMetadata;
    }
  }

  @ModelBuilder
  @SuppressWarnings("FieldMayBeFinal")
  public static final class BooleanMetadata implements DialogueMetadata {
    private static final BuilderCodec<BooleanMetadata> CODEC =
        CodecUtil.modelBuilder(
            BooleanMetadata.class, DialoguePlugin.getResolver(), Plugin.getSharedCache());

    public boolean Value;

    @Override
    public boolean asBoolean() {
      return Value;
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public DialogueMetadata clone() {
      var newMetadata = new BooleanMetadata();
      newMetadata.Value = Value;
      return newMetadata;
    }
  }

  @ModelBuilder
  @SuppressWarnings("FieldMayBeFinal")
  public static final class DialogueMetadataStore {
    private Map<String, DialogueMetadata> Metadata;

    public DialogueMetadataStore() {
      this.Metadata = new HashMap<>();
    }

    public @Nullable DialogueMetadata get(String key) {
      return Metadata.get(key);
    }

    public @Nullable DialogueMetadata putString(String key, String value) {
      var metadata = new StringMetadata();
      metadata.Value = value;

      return Metadata.put(key, metadata);
    }

    public @Nullable DialogueMetadata putInteger(String key, int value) {
      var metadata = new IntegerMetadata();
      metadata.Value = value;

      return Metadata.put(key, metadata);
    }

    public @Nullable DialogueMetadata putBoolean(String key, boolean value) {
      var metadata = new BooleanMetadata();
      metadata.Value = value;

      return Metadata.put(key, metadata);
    }
  }
}
