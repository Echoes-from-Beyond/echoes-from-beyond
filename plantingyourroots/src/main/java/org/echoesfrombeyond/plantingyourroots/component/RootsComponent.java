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

package org.echoesfrombeyond.plantingyourroots.component;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.*;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.plantingyourroots.PlantingYourRoots;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@ModelBuilder
public class RootsComponent implements Component<EntityStore> {
  public static final BuilderCodec<RootsComponent> CODEC =
      CodecUtil.modelBuilder(
          RootsComponent.class, PlantingYourRoots.getResolver(), Plugin.getSharedCache());

  private static @Nullable ComponentType<EntityStore, RootsComponent> TYPE;

  @ApiStatus.Internal
  public static void register(ComponentRegistryProxy<EntityStore> proxy) {
    TYPE = proxy.registerComponent(RootsComponent.class, "RootsComponent", CODEC);
  }

  public static ComponentType<EntityStore, RootsComponent> getComponentType() {
    var type = TYPE;
    if (type == null) throw new IllegalStateException("Plugin has not been initialized yet");

    return type;
  }

  @ModelBuilder
  public static class Dateable implements Cloneable {
    public int Stage;
    public boolean TalkedTo;

    @Override
    public Dateable clone() {
      try {
        return (Dateable) super.clone();
      } catch (CloneNotSupportedException e) {
        throw new AssertionError();
      }
    }
  }

  public int Day;
  public Map<String, Dateable> Dateables;
  public @Nullable InventoryComponent[] OldInventory;
  public boolean HasOldInventory;
  public boolean HasJoinedKweebdrasil;

  @SuppressWarnings("unused")
  public RootsComponent() {
    this.Day = 1;
    this.Dateables = new HashMap<>();
    this.OldInventory = new InventoryComponent[InventoryComponent.EVERYTHING.length];
    this.HasOldInventory = false;
    this.HasJoinedKweebdrasil = false;
  }

  public RootsComponent(RootsComponent other) {
    this.Day = other.Day;
    this.Dateables = new HashMap<>(other.Dateables.size());

    for (var otherEntry : other.Dateables.entrySet())
      this.Dateables.put(otherEntry.getKey(), otherEntry.getValue().clone());
    this.OldInventory = new InventoryComponent[InventoryComponent.EVERYTHING.length];
    for (int i = 0; i < InventoryComponent.EVERYTHING.length; i++) {
      var otherInventoryComponent = other.OldInventory[i];
      this.OldInventory[i] =
          otherInventoryComponent == null
              ? null
              : (InventoryComponent) otherInventoryComponent.clone();
    }
    this.HasOldInventory = other.HasOldInventory;
    this.HasJoinedKweebdrasil = other.HasJoinedKweebdrasil;
  }

  @Override
  @SuppressWarnings("MethodDoesntCallSuperMethod")
  public RootsComponent clone() {
    return new RootsComponent(this);
  }
}
