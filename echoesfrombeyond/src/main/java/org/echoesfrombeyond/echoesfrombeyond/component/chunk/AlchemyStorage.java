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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.echoesfrombeyond.EchoesFromBeyond;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@ModelBuilder
@NullMarked
public class AlchemyStorage implements Component<ChunkStore> {
  public static final int DEFAULT_MAX_STORAGE_SIZE = 6;
  public static final String ALCHEMY_INTERMEDIATE_TAG = "AlchemyIntermediate";

  public static final BuilderCodec<AlchemyStorage> CODEC =
      CodecUtil.modelBuilder(AlchemyStorage.class, EchoesFromBeyond.get().getResolver());

  private static @Nullable ComponentType<ChunkStore, AlchemyStorage> COMPONENT_TYPE;

  public int MaxStorage;

  @SuppressWarnings("FieldMayBeFinal")
  private List<ItemStack> ItemsStored;

  /**
   * Called internally during plugin initialization.
   *
   * @param proxy the registry proxy
   */
  @ApiStatus.Internal
  public static void registerComponentType(ComponentRegistryProxy<ChunkStore> proxy) {
    COMPONENT_TYPE = proxy.registerComponent(AlchemyStorage.class, "AlchemyStorage", CODEC);
  }

  /**
   * @return the component type
   */
  public static ComponentType<ChunkStore, AlchemyStorage> getComponentType() {
    assert COMPONENT_TYPE != null;
    return COMPONENT_TYPE;
  }

  @SuppressWarnings("unused")
  public AlchemyStorage() {
    this.MaxStorage = DEFAULT_MAX_STORAGE_SIZE;
    this.ItemsStored = new ArrayList<>();
  }

  private AlchemyStorage(AlchemyStorage that) {
    this.MaxStorage = that.MaxStorage;
    this.ItemsStored = new ObjectArrayList<>(that.ItemsStored);
  }

  @UnmodifiableView
  public List<ItemStack> getStorage() {
    // instead of directly modifying the list, should always fall back on other methods to ensure
    // validation
    return Collections.unmodifiableList(ItemsStored);
  }

  public boolean addToStorage(ItemStack item) {
    // only one item per slot
    if (item.getQuantity() > 1) return false;

    // don't add items that don't have the correct tag
    if (!Arrays.asList(item.getItem().getData().getRawTags().get("Type"))
        .contains(ALCHEMY_INTERMEDIATE_TAG)) return false;

    ItemsStored.add(item);
    return true;
  }

  public boolean removeFromStorage(int index) {
    // bounds check
    if (index < 0 || index >= ItemsStored.size()) return false;

    ItemsStored.remove(index);

    return true;
  }

  /**
   * Performs extra validation by checking that the target item still exists at a given index, before removing it.
   * @param index position in the list
   * @param item target item
   * @return {@code true} if all validation passes <i>and</i> the item was removed, {@code false} otherwise.
   */
  public boolean removeFromStorage(int index, ItemStack item) {
    // bounds check
    if (index < 0 || index >= ItemsStored.size()) return false;

    if(!ItemsStored.get(index).equals(item)) return false;

    ItemsStored.remove(index);

    return true;
  }

  @Override
  @SuppressWarnings("MethodDoesntCallSuperMethod")
  public Component<ChunkStore> clone() {
    return new AlchemyStorage(this);
  }
}
