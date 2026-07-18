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

package org.echoesfrombeyond.modutil.inventory;

import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ActionType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@SuppressWarnings("ClassCanBeRecord")
public class ProxyContainer implements StackContainer {
  private final ItemContainer itemContainer;

  public ProxyContainer(ItemContainer itemContainer) {
    this.itemContainer = itemContainer;
  }

  @Override
  public int getSlotCount() {
    return itemContainer.getCapacity();
  }

  @Override
  public InventoryOperationResult addItem(ItemStack stack) {
    var result = itemContainer.addItemStack(stack, true, false, false);
    if (!result.succeeded())
      return new InventoryOperationResult(
          ActionType.ADD, OperationState.INVENTORY_FULL, -1, null, null, stack);

    var transactions = result.getSlotTransactions();

    var first = transactions.getFirst();
    var last = transactions.getLast();

    return new InventoryOperationResult(
        ActionType.ADD,
        OperationState.SUCCESS,
        first.getSlot(),
        first.getSlotBefore(),
        last.getSlotAfter(),
        result.getRemainder());
  }

  @Override
  public InventoryOperationResult addItem(ItemStack stack, int index) {
    var result = itemContainer.addItemStackToSlot((short) index, stack, true, false);

    return new InventoryOperationResult(
        ActionType.ADD,
        result.succeeded() ? OperationState.SUCCESS : OperationState.INVENTORY_FULL,
        index,
        result.getSlotBefore(),
        result.getSlotAfter(),
        result.getRemainder());
  }

  @Override
  public InventoryOperationResult setItem(ItemStack stack, int index) {
    var result = itemContainer.setItemStackForSlot((short) index, stack, false);

    return new InventoryOperationResult(
        ActionType.SET,
        OperationState.SUCCESS,
        index,
        result.getSlotBefore(),
        result.getSlotAfter(),
        result.getRemainder());
  }

  @Override
  public InventoryOperationResult removeItem(int index) {
    var result = itemContainer.removeItemStackFromSlot((short) index);
    if (!result.succeeded())
      return new InventoryOperationResult(
          ActionType.REMOVE, OperationState.EMPTY_SLOT, index, null, null, null);

    return new InventoryOperationResult(
        ActionType.REMOVE,
        OperationState.SUCCESS,
        index,
        result.getSlotBefore(),
        result.getSlotAfter(),
        result.getSlotAfter());
  }

  @Override
  public @Nullable ItemStack getItem(int index) {
    return itemContainer.getItemStack((short) index);
  }

  @Override
  public List<ItemStack> getAllItems() {
    var list = new ObjectArrayList<ItemStack>();
    itemContainer.forEach((_, stack) -> list.add(stack));
    return list;
  }

  @Override
  public void forEachItem(StackConsumer consumer) {
    itemContainer.forEach(consumer::accept);
  }

  /**
   * Gets the underlying {@link ItemContainer}.
   *
   * @return the underlying ItemContainer
   */
  public ItemContainer getItemContainer() {
    return itemContainer;
  }
}
