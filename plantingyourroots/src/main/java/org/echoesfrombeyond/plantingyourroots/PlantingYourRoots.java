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
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.echoesfrombeyond.annotation.RunOnWorldThread;
import org.echoesfrombeyond.dialoguelib.action.ChoiceAction;
import org.echoesfrombeyond.plantingyourroots.command.ReadyForLove;
import org.echoesfrombeyond.plantingyourroots.component.KindComponent;
import org.echoesfrombeyond.plantingyourroots.component.RootsComponent;
import org.echoesfrombeyond.plantingyourroots.dialogue.AppendDiaryEntry;
import org.echoesfrombeyond.plantingyourroots.dialogue.MarkTalkedTo;
import org.echoesfrombeyond.plantingyourroots.interaction.AdvanceDayInteraction;
import org.echoesfrombeyond.plantingyourroots.system.CleanWorldSystem;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("unused")
@NullMarked
public class PlantingYourRoots extends JavaPlugin {
  public static final String KWEEBDRASIL_GAMEPLAY_CONFIG_NAME = "Kweebdrasil";

  private record Spawn(String type, Vector3d position, Vector3f rotation) {}

  private static final RootsComponent.Dateable DEFAULT_DATEABLE = new RootsComponent.Dateable();
  private static final Map<String, Int2ObjectMap<Spawn>> SPAWNS = new HashMap<>();

  static {
    // TODO: init spawns
  }

  private static @Nullable PlantingYourRoots INSTANCE;

  private final Map<UUID, List<UUID>> entities;

  public PlantingYourRoots(JavaPluginInit init) {
    super(init);
    INSTANCE = this;

    this.entities = new HashMap<>();
  }

  public static PlantingYourRoots get() {
    var instance = INSTANCE;
    if (instance == null) throw new IllegalStateException("Plugin is not loaded");

    return instance;
  }

  @Override
  protected void setup() {
    super.setup();

    getCodecRegistry(Interaction.CODEC)
        .register("AdvanceDay", AdvanceDayInteraction.class, AdvanceDayInteraction.CODEC);

    getCodecRegistry(ChoiceAction.CODEC)
        .register("AppendDiary", AppendDiaryEntry.class, AppendDiaryEntry.CODEC)
        .register("MarkTalkedTo", MarkTalkedTo.class, MarkTalkedTo.CODEC);

    getEntityStoreRegistry().registerSystem(new CleanWorldSystem());

    var entityStoreRegistry = getEntityStoreRegistry();
    RootsComponent.register(entityStoreRegistry);
    KindComponent.register(entityStoreRegistry);

    getCommandRegistry().registerCommand(new ReadyForLove());
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

  @RunOnWorldThread
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

    //noinspection unchecked
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
    }
  }
}
