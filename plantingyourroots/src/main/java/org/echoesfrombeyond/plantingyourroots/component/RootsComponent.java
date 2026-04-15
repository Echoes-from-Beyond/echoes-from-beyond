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
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.plantingyourroots.diary.DiaryEntry;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@ModelBuilder
public class RootsComponent implements Component<EntityStore> {
  public static final BuilderCodec<RootsComponent> CODEC =
      CodecUtil.modelBuilder(
          RootsComponent.class, Plugin.getSharedResolver(), Plugin.getSharedCache());

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

    public Dateable() {
      this.Stage = 1;
    }

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
  public List<DiaryEntry> DiaryEntries;
  public Map<String, Dateable> Dateables;

  @SuppressWarnings("unused")
  public RootsComponent() {
    this.Day = 1;
    this.DiaryEntries = new ArrayList<>();
    this.Dateables = new HashMap<>();
  }

  public RootsComponent(RootsComponent other) {
    this.Day = other.Day;
    this.DiaryEntries = new ArrayList<>(other.DiaryEntries.size());
    this.Dateables = new HashMap<>(other.Dateables.size());

    for (var otherEntry : other.DiaryEntries) this.DiaryEntries.add(otherEntry.clone());
    for (var otherEntry : other.Dateables.entrySet())
      this.Dateables.put(otherEntry.getKey(), otherEntry.getValue().clone());
  }

  @Override
  @SuppressWarnings("MethodDoesntCallSuperMethod")
  public RootsComponent clone() {
    return new RootsComponent(this);
  }
}
