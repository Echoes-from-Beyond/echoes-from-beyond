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
import org.echoesfrombeyond.dialoguelib.metadata.DialogueMetadataStore;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@ModelBuilder
@SuppressWarnings("FieldMayBeFinal")
public class DialogueComponent implements Component<EntityStore> {
  public static final BuilderCodec<DialogueComponent> CODEC =
      CodecUtil.modelBuilder(
          DialogueComponent.class, DialoguePlugin.getResolver(), Plugin.getSharedCache());

  private static @Nullable ComponentType<EntityStore, DialogueComponent> TYPE;

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
    for (var entry : other.MetadataStorage.entrySet())
      newMap.put(entry.getKey(), entry.getValue().clone());

    this.MetadataStorage = newMap;
  }

  @Override
  @SuppressWarnings("MethodDoesntCallSuperMethod")
  public Component<EntityStore> clone() {
    return new DialogueComponent(this);
  }

  public @Nullable DialogueMetadataStore getMetadataStore(String key) {
    return MetadataStorage.get(key);
  }

  public @Nullable DialogueMetadataStore putMetadataStore(String key, DialogueMetadataStore store) {
    return MetadataStorage.put(key, store);
  }
}
