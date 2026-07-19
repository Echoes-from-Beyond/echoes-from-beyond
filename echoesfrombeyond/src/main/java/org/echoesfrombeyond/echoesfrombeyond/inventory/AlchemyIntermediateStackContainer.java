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
import org.jspecify.annotations.NullMarked;

/**
 * An item container that represents a common type of inventory in the alchemy system. Enforces that
 * all added items contain the appropriate tag, and are singular in quantity.
 *
 * <p>This one does not allow empty (null) slots.
 */
@ModelBuilder
@NullMarked
public class AlchemyIntermediateStackContainer extends SingleItemStackContainer {
  public static final String ALCHEMY_INTERMEDIATE_TAG = "AlchemyIntermediate";

  public static final BuilderCodec<AlchemyIntermediateStackContainer> CODEC =
      CodecUtil.modelBuilder(
          AlchemyIntermediateStackContainer.class,
          ABSTRACT_CODEC,
          EchoesFromBeyond.get().getResolver());

  public AlchemyIntermediateStackContainer() {
    super();
  }

  public AlchemyIntermediateStackContainer(int maxSize) {
    super(maxSize);
  }

  public AlchemyIntermediateStackContainer(AlchemyIntermediateStackContainer that) {
    super(that.StoredItems, that.MaxSize);
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  protected boolean canAddItem(ItemStack stack) {
    return Arrays.asList(stack.getItem().getData().getRawTags().get("Type"))
        .contains(ALCHEMY_INTERMEDIATE_TAG);
  }
}
