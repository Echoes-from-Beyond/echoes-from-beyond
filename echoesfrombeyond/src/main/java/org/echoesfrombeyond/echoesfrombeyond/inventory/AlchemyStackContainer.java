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

package org.echoesfrombeyond.echoesfrombeyond.inventory;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import java.util.Arrays;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.echoesfrombeyond.EchoesFromBeyond;
import org.echoesfrombeyond.echoesfrombeyond.asset.AlchemyItem;
import org.jspecify.annotations.NullMarked;

/**
 * An item container that represents a common type of inventory in the alchemy system. Enforces that
 * all added items are referenced by the {@link AlchemyItem} asset, and are singular in quantity.
 *
 * <p>This one does not allow empty (null) slots.
 */
@ModelBuilder
@NullMarked
public class AlchemyStackContainer extends SingleItemStackContainer {
  public static final String ALCHEMY_INGREDIENT_TAG = "AlchemyIngredient";

  public static final BuilderCodec<AlchemyStackContainer> CODEC =
      CodecUtil.modelBuilder(
          AlchemyStackContainer.class, ABSTRACT_CODEC, EchoesFromBeyond.EARLY_RESOLVER);

  public AlchemyStackContainer() {
    super();
  }

  public AlchemyStackContainer(int maxSize) {
    super(maxSize);
  }

  public AlchemyStackContainer(AlchemyStackContainer that) {
    super(that.StoredItems, that.MaxSize);
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  protected boolean canAddItem(ItemStack stack) {
    // check immediately for our own items
    // won't work for vanilla items as we can't currently override an item without potentially
    // introducing mod conflicts?
    if (Arrays.asList(stack.getItem().getData().getRawTags().get("Type"))
        .contains(ALCHEMY_INGREDIENT_TAG)) return true;

    var assetMap = AlchemyItem.getAssetStore().getAssetMap().getAssetMap();
    String stackId = stack.getItemId();

    for (var key : assetMap.keySet()) {
      if (key.equals(stackId)) return true;
    }

    return false;
  }
}
