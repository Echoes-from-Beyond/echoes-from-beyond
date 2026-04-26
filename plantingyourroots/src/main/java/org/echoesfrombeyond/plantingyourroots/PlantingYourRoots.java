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

package org.echoesfrombeyond.plantingyourroots;

import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.BuilderCodecMapCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.echoesfrombeyond.annotation.RunOnWorldThread;
import org.echoesfrombeyond.codechelper.CodecResolver;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.dialoguelib.action.ChoiceAction;
import org.echoesfrombeyond.dialoguelib.condition.ChoiceCondition;
import org.echoesfrombeyond.plantingyourroots.action.AppendDiaryEntry;
import org.echoesfrombeyond.plantingyourroots.action.MarkTalkedTo;
import org.echoesfrombeyond.plantingyourroots.command.ReadyForLove;
import org.echoesfrombeyond.plantingyourroots.component.KindComponent;
import org.echoesfrombeyond.plantingyourroots.component.RootsComponent;
import org.echoesfrombeyond.plantingyourroots.condition.TalkedToCondition;
import org.echoesfrombeyond.plantingyourroots.interaction.AdvanceDayInteraction;
import org.echoesfrombeyond.plantingyourroots.interaction.TeleportToSpawnInteraction;
import org.echoesfrombeyond.plantingyourroots.npc.BuilderRootsOpenDialogue;
import org.echoesfrombeyond.plantingyourroots.system.CleanWorldSystem;
import org.echoesfrombeyond.plantingyourroots.system.ManageInventorySystem;
import org.echoesfrombeyond.plantingyourroots.system.PreventItemDropInKweebdrasilSystem;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("unused")
@NullMarked
public class PlantingYourRoots extends JavaPlugin {
  public static final String KWEEBDRASIL_GAMEPLAY_CONFIG_NAME = "Kweebdrasil";

  private record Spawn(String type, Vector3d position, Vector3f rotation) {}

  public static final RootsComponent.Dateable DEFAULT_DATEABLE = new RootsComponent.Dateable();
  private static final Map<String, Int2ObjectMap<Spawn>> SPAWNS = new HashMap<>();
  private static @Nullable CodecResolver RESOLVER;

  static {
    var frenchSpawns = new Int2ObjectOpenHashMap<Spawn>();

    // fallback value
    frenchSpawns.put(
        -1,
        new Spawn("French_Kweebec", new Vector3d(32.53, 210.0, -21.27), new Vector3f(-3, 0, 0)));

    // the spawnpoints of different variations of the same character
    // internally, each conversation is handled by a different NPC
    frenchSpawns.put(
        0, new Spawn("French_Kweebec", new Vector3d(32.53, 210.0, -21.27), new Vector3f(-3, 0, 0)));
    frenchSpawns.put(
        1,
        new Spawn("French_Kweebec_2", new Vector3d(31.42, 222.0, -37.46), new Vector3f(-3, 0, 0)));
    frenchSpawns.put(
        2,
        new Spawn(
            "French_Kweebec_3", new Vector3d(28.32, 222.0, -38.87), new Vector3f(-1.5f, 0, 0)));
    frenchSpawns.put(
        3,
        new Spawn(
            "French_Kweebec_4", new Vector3d(38.84, 124.0, -16.4), new Vector3f(-1.5f, 0, 0)));
    frenchSpawns.put(
        4,
        new Spawn("French_Kweebec_5", new Vector3d(29.6, 222.0, -37.23), new Vector3f(-3, 0, 0)));
    frenchSpawns.put(
        5,
        new Spawn("French_Kweebec_6", new Vector3d(22.5, 207.0, -28.44), new Vector3f(1.5f, 0, 0)));

    // unify all the NPCs into the same character
    SPAWNS.put("French_Kweebec", frenchSpawns);
  }

  private static @Nullable PlantingYourRoots INSTANCE;

  private final Map<UUID, List<UUID>> entities;
  private final BuilderCodecMapCodec<InventoryComponent> inventoryComponentCodec;

  public PlantingYourRoots(JavaPluginInit init) {
    super(init);
    INSTANCE = this;

    this.entities = new HashMap<>();
    this.inventoryComponentCodec = new BuilderCodecMapCodec<>();
  }

  public static PlantingYourRoots get() {
    var instance = INSTANCE;
    if (instance == null) throw new IllegalStateException("Plugin is not loaded");

    return instance;
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  protected void setup() {
    super.setup();

    RESOLVER =
        CodecResolver.builder()
            .chain(CodecResolver.PRIMITIVE)
            .withCollectionSupport()
            .withMapSupport()
            .withEnumSupport()
            .withArraySupport()
            .withRecursiveResolution(Plugin.getSharedCache())
            .withSubtypeMapping(List.class, ArrayList.class)
            .withSubtypeMapping(Set.class, HashSet.class)
            .withSubtypeMapping(Map.class, HashMap.class)
            .withDirectMapping(InventoryComponent.class, inventoryComponentCodec)
            .build();

    getCodecRegistry(Interaction.CODEC)
        .register("AdvanceDay", AdvanceDayInteraction.class, AdvanceDayInteraction.CODEC)
        .register(
            "TeleportToSpawn", TeleportToSpawnInteraction.class, TeleportToSpawnInteraction.CODEC);

    getCodecRegistry(ChoiceAction.CODEC)
        .register("AppendDiary", AppendDiaryEntry.class, AppendDiaryEntry.CODEC)
        .register("MarkTalkedTo", MarkTalkedTo.class, MarkTalkedTo.CODEC);

    getCodecRegistry(ChoiceCondition.CODEC)
        .register("TalkedTo", TalkedToCondition.class, TalkedToCondition.CODEC);

    var inventoryCodecRegistry = getCodecRegistry(inventoryComponentCodec);
    for (var inventoryComponentType : InventoryComponent.EVERYTHING) {
      var type = inventoryComponentType.getTypeClass();

      Field field;
      try {
        field = type.getDeclaredField("CODEC");
      } catch (NoSuchFieldException e) {
        continue;
      }

      int modifiers = field.getModifiers();
      if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers)) continue;

      Object value;
      try {
        value = field.get(null);
      } catch (IllegalAccessException e) {
        continue;
      }

      if (value == null || !BuilderCodec.class.isAssignableFrom(value.getClass())) continue;

      inventoryCodecRegistry.register(type.getSimpleName(), (Class) type, (BuilderCodec) value);
    }

    var entityStoreRegistry = getEntityStoreRegistry();
    RootsComponent.register(entityStoreRegistry);
    KindComponent.register(entityStoreRegistry);

    getEntityStoreRegistry().registerSystem(new CleanWorldSystem());
    getEntityStoreRegistry().registerSystem(new PreventItemDropInKweebdrasilSystem());
    getEntityStoreRegistry().registerSystem(new ManageInventorySystem());

    getCommandRegistry().registerCommand(new ReadyForLove());

    NPCPlugin.get().registerCoreComponentType("RootsOpenDialogue", BuilderRootsOpenDialogue::new);
  }

  private static @Nullable UUID spawnDateable(
      Store<EntityStore> storeStore, String kind, Spawn spawn) {
    var pair =
        NPCPlugin.get().spawnNPC(storeStore, spawn.type, null, spawn.position, spawn.rotation);
    if (pair == null) return null;

    var newEntityRef = pair.first();

    var uuid = storeStore.getComponent(newEntityRef, UUIDComponent.getComponentType());
    assert uuid != null;

    var kindComponent = new KindComponent();
    kindComponent.Kind = kind;

    storeStore.putComponent(newEntityRef, KindComponent.getComponentType(), kindComponent);
    return uuid.getUuid();
  }

  public CompletableFuture<World> getKweebdrasil(RootsComponent roots) {
    var defaultWorld = Universe.get().getDefaultWorld();
    if (defaultWorld == null) throw new IllegalStateException("Default world must exist");

    return InstancesPlugin.get()
        .spawnInstance("Kweebdrasil", defaultWorld, new Transform())
        .whenComplete(
            (world, err) -> {
              if (err != null || world == null) return;

              world.execute(
                  () -> {
                    var uuids = new ArrayList<UUID>();
                    for (var entry : SPAWNS.entrySet()) {
                      var kind = entry.getKey();
                      var spawnsForStage = entry.getValue();

                      var stage = roots.Dateables.getOrDefault(kind, DEFAULT_DATEABLE).Stage;

                      if (!spawnsForStage.containsKey(stage)) continue;
                      var spawn = spawnsForStage.get(stage);

                      var uuid = spawnDateable(world.getEntityStore().getStore(), kind, spawn);
                      if (uuid != null) uuids.add(uuid);
                    }

                    synchronized (entities) {
                      entities.put(world.getWorldConfig().getUuid(), uuids);
                    }
                  });
            });
  }

  public boolean isKweebdrasilInstance(World world) {
    return world
        .getWorldConfig()
        .getGameplayConfig()
        .equals(PlantingYourRoots.KWEEBDRASIL_GAMEPLAY_CONFIG_NAME);
  }

  public void removeKweebdrasilInstance(UUID uuid) {
    synchronized (entities) {
      entities.remove(uuid);
    }
  }

  public static CodecResolver getResolver() {
    var resolver = RESOLVER;
    if (resolver == null) throw new IllegalStateException("Plugin has not been initialized yet");
    return RESOLVER;
  }

  @RunOnWorldThread
  @SuppressWarnings("unchecked")
  public void advanceDay(CommandBuffer<EntityStore> buffer, RootsComponent roots) {
    var world = buffer.getStore().getExternalData().getWorld();
    if (!isKweebdrasilInstance(world)) return;

    List<UUID> spawnedEntities;
    synchronized (entities) {
      spawnedEntities = entities.get(world.getWorldConfig().getUuid());
    }

    if (spawnedEntities == null) return;

    var store = world.getEntityStore();
    var storeStore = store.getStore();

    var npcComponentType = NPCEntity.getComponentType();
    assert npcComponentType != null;

    Ref<?>[] entitiesToRemove =
        spawnedEntities.stream()
            .map(store::getRefFromUUID)
            .filter(Objects::nonNull)
            .filter(Ref::isValid)
            .filter(
                ref -> {
                  var npc = storeStore.getComponent(ref, npcComponentType);
                  var kind = storeStore.getComponent(ref, KindComponent.getComponentType());

                  if (npc == null || kind == null || kind.Kind == null) return false;

                  var dateable = roots.Dateables.get(kind.Kind);
                  return dateable != null && dateable.TalkedTo;
                })
            .toArray(Ref<?>[]::new);

    for (var holder :
        storeStore.removeEntities((Ref<EntityStore>[]) entitiesToRemove, RemoveReason.REMOVE)) {
      var npc = holder.getComponent(npcComponentType);
      var kind = holder.getComponent(KindComponent.getComponentType());

      if (npc == null || kind == null || kind.Kind == null) continue;

      var dateable = roots.Dateables.computeIfAbsent(kind.Kind, _ -> new RootsComponent.Dateable());
      var stage = dateable.Stage;

      var spawnsForStage = SPAWNS.get(kind.Kind);
      if (spawnsForStage == null) continue;

      var newSpawn =
          spawnsForStage.containsKey(stage + 1)
              ? spawnsForStage.get(stage + 1)
              : spawnsForStage.get(-1);

      var uuid = spawnDateable(storeStore, kind.Kind, newSpawn);
      if (uuid != null) spawnedEntities.add(uuid);

      dateable.Stage++;
      dateable.TalkedTo = false;
    }
  }
}
