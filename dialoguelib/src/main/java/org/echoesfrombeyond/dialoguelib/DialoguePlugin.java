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

package org.echoesfrombeyond.dialoguelib;

import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.event.RemovedAssetsEvent;
import com.hypixel.hytale.assetstore.map.CaseInsensitiveHashStrategy;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.npc.NPCPlugin;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import java.util.*;
import org.echoesfrombeyond.codechelper.CodecResolver;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.dialoguelib.action.*;
import org.echoesfrombeyond.dialoguelib.choice.DialogueChoice;
import org.echoesfrombeyond.dialoguelib.choice.SelectChoice;
import org.echoesfrombeyond.dialoguelib.choice.StandardChoice;
import org.echoesfrombeyond.dialoguelib.component.DialogueComponent;
import org.echoesfrombeyond.dialoguelib.condition.*;
import org.echoesfrombeyond.dialoguelib.dialogue.ChainDialogue;
import org.echoesfrombeyond.dialoguelib.dialogue.Dialogue;
import org.echoesfrombeyond.dialoguelib.dialogue.StandardDialogue;
import org.echoesfrombeyond.dialoguelib.metadata.BooleanMetadata;
import org.echoesfrombeyond.dialoguelib.metadata.DialogueMetadata;
import org.echoesfrombeyond.dialoguelib.metadata.IntegerMetadata;
import org.echoesfrombeyond.dialoguelib.metadata.StringMetadata;
import org.echoesfrombeyond.dialoguelib.npc.BuilderOpenDialogue;
import org.echoesfrombeyond.dialoguelib.trigger.JoinTrigger;
import org.echoesfrombeyond.dialoguelib.trigger.Trigger;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@SuppressWarnings("unused")
public class DialoguePlugin extends JavaPlugin {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  private static @Nullable CodecResolver RESOLVER;
  private static @Nullable Map<String, Trigger> TRIGGERS;

  public DialoguePlugin(JavaPluginInit init) {
    super(init);
  }

  @Override
  protected void setup() {
    RESOLVER =
        CodecResolver.builder()
            .chain(CodecResolver.PRIMITIVE)
            .withCollectionSupport()
            .withMapSupport()
            .withEnumSupport()
            .withRecursiveResolution(Plugin.getSharedCache())
            .withSubtypeMapping(List.class, ArrayList.class)
            .withSubtypeMapping(Set.class, HashSet.class)
            .withSubtypeMapping(Map.class, HashMap.class)
            .withDirectMapping(ChoiceAction.class, ChoiceAction.CODEC)
            .withDirectMapping(ChoiceCondition.class, ChoiceCondition.CODEC)
            .withDirectMapping(Dialogue.class, Dialogue.CODEC)
            .withDirectMapping(DialogueChoice.class, DialogueChoice.CODEC)
            .withDirectMapping(Message.class, Message.CODEC)
            .withDirectMapping(Trigger.class, Trigger.CODEC)
            .withDirectMapping(DialogueMetadata.class, DialogueMetadata.CODEC)
            .build();

    TRIGGERS = new Object2ObjectOpenCustomHashMap<>(CaseInsensitiveHashStrategy.getInstance());

    getAssetRegistry()
        .register(
            HytaleAssetStore.builder(Dialogue.class, new DefaultAssetMap<>())
                .setCodec(Dialogue.CODEC)
                .setPath("Dialogue")
                .setKeyFunction(Dialogue::getId)
                .build());

    getAssetRegistry()
        .register(
            HytaleAssetStore.builder(Trigger.class, new DefaultAssetMap<>())
                .setCodec(Trigger.CODEC)
                .setPath("DialogueTrigger")
                .loadsAfter(Dialogue.class)
                .setKeyFunction(Trigger::getId)
                .build());

    getCodecRegistry(ChoiceAction.CODEC)
        .register("AdjustInteger", AddIntegerMetadataAction.class, AddIntegerMetadataAction.CODEC)
        .register("Advance", AdvanceAction.class, AdvanceAction.CODEC)
        .register("Close", CloseDialogueAction.class, CloseDialogueAction.CODEC)
        .register("Composite", CompositeAction.class, CompositeAction.CODEC)
        .register("Conditional", ConditionalAction.class, ConditionalAction.CODEC)
        .register("SetMetadata", SetMetadataAction.class, SetMetadataAction.CODEC);

    getCodecRegistry(ChoiceCondition.CODEC)
        .register("Boolean", BooleanCondition.class, BooleanCondition.CODEC)
        .register("CompareInteger", CompareIntegerCondition.class, CompareIntegerCondition.CODEC)
        .register("Equals", MetadataEqualsCondition.class, MetadataEqualsCondition.CODEC)
        .register("Not", NotCondition.class, NotCondition.CODEC);

    getCodecRegistry(Dialogue.CODEC)
        .register("Chain", ChainDialogue.class, ChainDialogue.CODEC)
        .register("Standard", StandardDialogue.class, StandardDialogue.CODEC);

    getCodecRegistry(DialogueChoice.CODEC)
        .register("Select", SelectChoice.class, SelectChoice.CODEC)
        .register("Standard", StandardChoice.class, StandardChoice.CODEC);

    getCodecRegistry(DialogueMetadata.CODEC)
        .register("Boolean", BooleanMetadata.class, BooleanMetadata.CODEC)
        .register("Integer", IntegerMetadata.class, IntegerMetadata.CODEC)
        .register("String", StringMetadata.class, StringMetadata.CODEC);

    getCodecRegistry(Trigger.CODEC).register("Join", JoinTrigger.class, JoinTrigger.CODEC);

    var entityStoreRegistry = getEntityStoreRegistry();
    DialogueComponent.register(entityStoreRegistry);

    NPCPlugin.get().registerCoreComponentType("OpenDialogue", BuilderOpenDialogue::new);

    getEventRegistry()
        .register(LoadedAssetsEvent.class, Trigger.class, this::onTriggerAssetsLoaded);

    getEventRegistry()
        .register(LoadedAssetsEvent.class, Dialogue.class, this::onDialogueAssetsLoaded);

    getEventRegistry()
        .register(RemovedAssetsEvent.class, Trigger.class, this::onTriggerAssetsRemoved);

    getEventRegistry()
        .register(RemovedAssetsEvent.class, Dialogue.class, this::onDialogueAssetsRemoved);
  }

  private void onTriggerAssetsLoaded(
      LoadedAssetsEvent<String, Trigger, DefaultAssetMap<String, Trigger>> event) {
    var dialogueStoreMap = Dialogue.ASSET_STORE.get().getAssetMap();
    var triggers = event.getLoadedAssets();

    var triggersSet = TRIGGERS;
    if (triggersSet != null) {
      synchronized (triggersSet) {
        triggersSet.putAll(triggers);
      }
    }

    for (var entry : triggers.entrySet()) {
      var trigger = entry.getValue();
      var targets = trigger.getTargetIds();

      if (targets.isEmpty())
        LOGGER.atWarning().log(
            "Trigger `%s` did not reference any dialogue assets", trigger.getId());

      for (var target : targets) {
        var referenced = dialogueStoreMap.getAsset(target);
        if (referenced == null) {
          LOGGER.atWarning().log(
              "Trigger `%s` referenced non-existent dialogue asset `%s`", trigger.getId(), target);

          continue;
        }

        trigger.link(this, referenced);
      }
    }
  }

  private void onDialogueAssetsLoaded(
      LoadedAssetsEvent<String, Dialogue, DefaultAssetMap<String, Dialogue>> event) {
    if (event.isInitial()) return;

    var triggers = Trigger.ASSET_STORE.get().getAssetMap().getAssetMap().values();

    for (var dialogue : event.getLoadedAssets().values()) {
      for (var trigger : triggers) {
        if (!trigger.getTargetIds().contains(dialogue.getId())) continue;

        trigger.link(this, dialogue);
      }
    }
  }

  private void onTriggerAssetsRemoved(
      RemovedAssetsEvent<String, Trigger, DefaultAssetMap<String, Trigger>> event) {
    var triggers = TRIGGERS;
    if (triggers == null) return;

    synchronized (triggers) {
      for (var removed : event.getRemovedAssets()) {
        var trigger = triggers.remove(removed);
        if (trigger == null) continue;

        trigger.unlinkAll();
      }
    }
  }

  private void onDialogueAssetsRemoved(
      RemovedAssetsEvent<String, Dialogue, DefaultAssetMap<String, Dialogue>> event) {
    var triggers = Trigger.ASSET_STORE.get().getAssetMap().getAssetMap().values();

    for (var removed : event.getRemovedAssets())
      for (var trigger : triggers)
        if (trigger.getTargetIds().contains(removed)) trigger.unlink(removed);
  }

  @Override
  protected void shutdown() {
    RESOLVER = null;
    TRIGGERS = null;
  }

  @ApiStatus.Internal
  public static CodecResolver getResolver() {
    var resolver = RESOLVER;
    if (resolver == null) throw new IllegalStateException("Plugin must be initialized");

    return resolver;
  }
}
