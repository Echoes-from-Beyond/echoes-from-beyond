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
import java.util.Collections;
import java.util.List;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.echoesfrombeyond.EchoesFromBeyond;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.Nullable;

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
  }

  private AlchemyStorage(AlchemyStorage that) {
    this.MaxStorage = that.MaxStorage;
    this.ItemsStored = new ObjectArrayList<>(that.ItemsStored);
  }

  @UnmodifiableView
  public List<ItemStack> getStorage() {
    // instead of directly modifying the list, should always fall back on other methods to ensure
    // validation
    return Collections.unmodifiableList(this.ItemsStored);
  }

  public boolean addToStorage(ItemStack item) {
    // only one item per slot
    if (item.getQuantity() > 1) return false;

    String[] typeTags = item.getItem().getData().getRawTags().get("Type");

    boolean found = false;
    for (String tag : typeTags) {
      if (tag.equals(ALCHEMY_INTERMEDIATE_TAG)) {
        found = true;
        break;
      }
    }

    // don't add items that don't have the correct tag
    if (!found) return false;

    this.ItemsStored.add(item);
    return true;
  }

  public boolean removeFromStorage(int index) {
    // bounds check
    if (index < 0 || index >= this.ItemsStored.size()) return false;

    this.ItemsStored.remove(index);

    return true;
  }

  @Override
  @SuppressWarnings("MethodDoesntCallSuperMethod")
  public Component<ChunkStore> clone() {
    return new AlchemyStorage(this);
  }
}
