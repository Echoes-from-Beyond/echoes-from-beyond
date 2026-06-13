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

package org.echoesfrombeyond.echoesfrombeyond.component.chunk;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.echoesfrombeyond.EchoesFromBeyond;
import org.echoesfrombeyond.echoesfrombeyond.ui.AlchemyBenchSupplier;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class MortarAndPestle implements Component<ChunkStore> {
  public static final int MAX_GRINDABLE_ITEMS = 6;

  public static final BuilderCodec<MortarAndPestle> CODEC =
      CodecUtil.modelBuilder(MortarAndPestle.class, EchoesFromBeyond.get().getResolver());

  private static @Nullable ComponentType<ChunkStore, MortarAndPestle> COMPONENT_TYPE;

  @SuppressWarnings("FieldMayBeFinal")
  private AlchemyBenchSupplier.@Nullable ItemEntry[] ItemsToGrind;

  /**
   * Called internally during plugin initialization.
   *
   * @param proxy the registry proxy
   */
  @ApiStatus.Internal
  public static void registerComponentType(ComponentRegistryProxy<ChunkStore> proxy) {
    COMPONENT_TYPE = proxy.registerComponent(MortarAndPestle.class, "MortarAndPestle", CODEC);
  }

  /**
   * @return the component type
   */
  public static ComponentType<ChunkStore, MortarAndPestle> getComponentType() {
    assert COMPONENT_TYPE != null;
    return COMPONENT_TYPE;
  }

  @SuppressWarnings("unused")
  public MortarAndPestle() {
    this.ItemsToGrind = new AlchemyBenchSupplier.ItemEntry[MAX_GRINDABLE_ITEMS];
  }

  private MortarAndPestle(MortarAndPestle that) {
    this.ItemsToGrind = new AlchemyBenchSupplier.ItemEntry[that.ItemsToGrind.length];
    System.arraycopy(that.ItemsToGrind, 0, this.ItemsToGrind, 0, ItemsToGrind.length);
  }

  public boolean addItem(AlchemyBenchSupplier.ItemEntry entry) {
    for (int i = 0; i < ItemsToGrind.length; i++) {
      if (ItemsToGrind[i] == null) {
        ItemsToGrind[i] = entry;
        return true;
      }
    }

    return false;
  }

  @Override
  @SuppressWarnings("MethodDoesntCallSuperMethod")
  public Component<ChunkStore> clone() {
    return new MortarAndPestle(this);
  }
}
