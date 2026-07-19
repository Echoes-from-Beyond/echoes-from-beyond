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
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import it.unimi.dsi.fastutil.objects.Object2BooleanFunction;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.echoesfrombeyond.EchoesFromBeyond;
import org.echoesfrombeyond.echoesfrombeyond.inventory.AlchemyIntermediateStackContainer;
import org.echoesfrombeyond.echoesfrombeyond.inventory.AlchemyStackContainer;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@ModelBuilder
public class MortarAndPestle implements Component<ChunkStore> {
  public static final int MAX_GRINDABLE_ITEMS = 6;

  public static final BuilderCodec<MortarAndPestle> CODEC =
      CodecUtil.modelBuilder(MortarAndPestle.class, EchoesFromBeyond.get().getResolver());

  private static @Nullable ComponentType<ChunkStore, MortarAndPestle> COMPONENT_TYPE;

  @SuppressWarnings("FieldMayBeFinal")
  public AlchemyStackContainer ItemsToGrind;

  @SuppressWarnings("FieldMayBeFinal")
  public AlchemyIntermediateStackContainer Overflow;

  public int CurrentInteractions;

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
    this.ItemsToGrind = new AlchemyStackContainer(MAX_GRINDABLE_ITEMS);
    this.Overflow = new AlchemyIntermediateStackContainer();
  }

  private MortarAndPestle(MortarAndPestle that) {
    this.ItemsToGrind = new AlchemyStackContainer(that.ItemsToGrind);
    this.Overflow = new AlchemyIntermediateStackContainer(that.Overflow);
    this.CurrentInteractions = that.CurrentInteractions;
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public boolean hasItems() {
    return ItemsToGrind.getSlotCount() > 0;
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public boolean isInProgress() {
    return CurrentInteractions > 0;
  }

  public double getProgress(int baselineInteractions, int interactionsPerItem) {
    var neededInteractions =
        baselineInteractions + (interactionsPerItem * ItemsToGrind.getSlotCount());

    if (neededInteractions <= 0) return 0.0;
    return (double) CurrentInteractions / (double) neededInteractions;
  }

  public boolean tryAddItemToInput(ItemStack newItem, Object2BooleanFunction<ItemStack> transact) {
    if (transact.test(newItem)) {
      var result = ItemsToGrind.addItem(newItem);

      return result.operationState().isSuccess();
    }

    return false;
  }

  @Override
  @SuppressWarnings("MethodDoesntCallSuperMethod")
  public Component<ChunkStore> clone() {
    return new MortarAndPestle(this);
  }
}
