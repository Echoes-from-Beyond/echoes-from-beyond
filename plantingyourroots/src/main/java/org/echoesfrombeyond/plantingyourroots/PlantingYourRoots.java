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
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.echoesfrombeyond.annotation.RunOnWorldThread;
import org.echoesfrombeyond.dialoguelib.action.ChoiceAction;
import org.echoesfrombeyond.plantingyourroots.command.ReadyForLove;
import org.echoesfrombeyond.plantingyourroots.component.RootsComponent;
import org.echoesfrombeyond.plantingyourroots.dialogue.AppendDiaryEntry;
import org.echoesfrombeyond.plantingyourroots.interaction.AdvanceDayInteraction;
import org.echoesfrombeyond.plantingyourroots.system.CleanWorldSystem;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("unused")
@NullMarked
public class PlantingYourRoots extends JavaPlugin {
  public static String KWEEBDRASIL_GAMEPLAY_CONFIG_NAME = "Kweebdrasil";
  public static int FINAL_DAY = 5;

  private record Spawn(String name, Vector3d position, Vector3f rotation) {}

  private static final Int2ObjectMap<List<Spawn>> SPAWNS = new Int2ObjectOpenHashMap<>();

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
        .register("AppendDiary", AppendDiaryEntry.class, AppendDiaryEntry.CODEC);

    getEntityStoreRegistry().registerSystem(new CleanWorldSystem());

    var entityStoreRegistry = getEntityStoreRegistry();
    RootsComponent.register(entityStoreRegistry);

    getCommandRegistry().registerCommand(new ReadyForLove());
  }

  public CompletableFuture<World> getKweebdrasil() {
    var defaultWorld = Universe.get().getDefaultWorld();
    if (defaultWorld == null) throw new IllegalStateException("Default world must exist");

    return InstancesPlugin.get()
        .spawnInstance("Kweebdrasil", defaultWorld, new Transform())
        .whenComplete(
            (world, err) -> {
              if (err != null || world == null) return;

              synchronized (entities) {
                entities.put(world.getWorldConfig().getUuid(), new ArrayList<>());
              }
            });
  }

  public boolean isKweebdrasilInstance(World world) {
    return world
        .getWorldConfig()
        .getGameplayConfig()
        .equals(PlantingYourRoots.KWEEBDRASIL_GAMEPLAY_CONFIG_NAME);
  }

  public void addKweebdrasilInstance(UUID uuid) {
    synchronized (entities) {
      entities.put(uuid, new ArrayList<>());
    }
  }

  public void removeKweebdrasilInstance(UUID uuid) {
    synchronized (entities) {
      entities.remove(uuid);
    }
  }

  private static void spawnEntitiesForDay(Store<EntityStore> store, int day, List<UUID> uuids) {
    var entities = SPAWNS.get(day);
    if (entities == null) return;

    for (var entity : entities) {
      var spawn =
          NPCPlugin.get().spawnNPC(store, entity.name, null, entity.position, entity.rotation);
      if (spawn == null) continue;

      var uuid = store.getComponent(spawn.first(), UUIDComponent.getComponentType());
      if (uuid != null) uuids.add(uuid.getUuid());
    }
  }

  @RunOnWorldThread
  public void advanceDay(CommandBuffer<EntityStore> buffer, RootsComponent roots) {
    var world = buffer.getStore().getExternalData().getWorld();
    if (!isKweebdrasilInstance(world)) return;

    if (roots.Day >= FINAL_DAY) {
      // TODO: "win" logic
      return;
    }

    List<UUID> spawnedEntities;
    synchronized (entities) {
      spawnedEntities = entities.get(world.getWorldConfig().getUuid());
    }

    if (spawnedEntities == null) return;

    var store = world.getEntityStore();
    Ref<?>[] entitiesToRemove =
        spawnedEntities.stream()
            .map(store::getRefFromUUID)
            .filter(Objects::nonNull)
            .filter(Ref::isValid)
            .toArray(Ref<?>[]::new);

    //noinspection unchecked
    store.getStore().removeEntities((Ref<EntityStore>[]) entitiesToRemove, RemoveReason.UNLOAD);
    spawnEntitiesForDay(store.getStore(), ++roots.Day, spawnedEntities);
  }
}
