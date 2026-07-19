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
import com.hypixel.hytale.server.core.inventory.transaction.ActionType;
import java.util.ArrayList;
import java.util.List;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.echoesfrombeyond.EchoesFromBeyond;
import org.echoesfrombeyond.modutil.inventory.InventoryOperationResult;
import org.echoesfrombeyond.modutil.inventory.StackContainer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Represents a StackContainer that only takes in one item at a time and does not have null slots.
 * Implementations differ in the validation that they do.
 */
@ModelBuilder
@NullMarked
public abstract class SingleItemStackContainer implements StackContainer {
  public static final BuilderCodec<SingleItemStackContainer> ABSTRACT_CODEC =
      CodecUtil.modelBuilder(SingleItemStackContainer.class, EchoesFromBeyond.get().getResolver());

  protected final List<ItemStack> StoredItems;
  protected final int MaxSize;

  public SingleItemStackContainer() {
    this.StoredItems = new ArrayList<>();
    this.MaxSize = Integer.MAX_VALUE;
  }

  public SingleItemStackContainer(int maxSize) {
    this.StoredItems = new ArrayList<>();
    this.MaxSize = maxSize;
  }

  public SingleItemStackContainer(List<ItemStack> storedItems, int maxSize) {
    this.StoredItems = new ArrayList<>(storedItems);
    this.MaxSize = maxSize;
  }

  public int getMaxSize() {
    return this.MaxSize;
  }

  @Override
  public int getSlotCount() {
    return StoredItems.size();
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  protected abstract boolean canAddItem(ItemStack stack);

  @Override
  public InventoryOperationResult addItem(ItemStack stack) {
    if (getSlotCount() > this.MaxSize) return InventoryOperationResult.FAILED_ADD_FULL;

    // don't add items that don't have the correct tag, or have zero quantity
    if (!canAddItem(stack))
      return new InventoryOperationResult(
          ActionType.ADD, OperationState.INVALID_ITEM, -1, null, null, stack);

    // will never return null because 1 != 0
    var single = stack.withQuantity(1);
    assert single != null;
    StoredItems.add(single);

    var rest = stack.withQuantity(stack.getQuantity() - 1);
    return new InventoryOperationResult(
        ActionType.ADD, OperationState.SUCCESS, getSlotCount() - 1, null, stack, rest);
  }

  @Override
  public InventoryOperationResult addItem(ItemStack stack, int index) {
    var size = getSlotCount();

    // mimic behavior of List#add(T, int)
    if (index == size) return addItem(stack);

    if (index > size) throw new IndexOutOfBoundsException(index);
    if (size > this.MaxSize) return InventoryOperationResult.FAILED_ADD_FULL;

    // capture the previous item
    var previous = StoredItems.get(index);

    if (!canAddItem(stack))
      return new InventoryOperationResult(
          ActionType.ADD, OperationState.INVALID_ITEM, -1, null, null, stack);

    var single = stack.withQuantity(1);
    assert single != null;
    StoredItems.add(index, single);

    var rest = stack.withQuantity(stack.getQuantity() - 1);
    return new InventoryOperationResult(
        ActionType.ADD, OperationState.SUCCESS, index, previous, stack, rest);
  }

  @Override
  public InventoryOperationResult setItem(ItemStack stack, int index) {
    if (index >= getSlotCount()) throw new IndexOutOfBoundsException(index);

    var previous = StoredItems.get(index);

    if (!canAddItem(stack))
      return new InventoryOperationResult(
          ActionType.REPLACE, OperationState.INVALID_ITEM, -1, null, null, stack);

    var single = stack.withQuantity(1);
    assert single != null;

    StoredItems.set(index, single);

    var rest = stack.withQuantity(stack.getQuantity() - 1);
    return new InventoryOperationResult(
        ActionType.REPLACE, OperationState.SUCCESS, index, previous, stack, rest);
  }

  @Override
  public InventoryOperationResult removeItem(int index) {
    if (index >= getSlotCount()) throw new IndexOutOfBoundsException(index);

    var previous = StoredItems.get(index);
    var rest = previous.withQuantity(previous.getQuantity() + 1);
    return new InventoryOperationResult(
        ActionType.REMOVE, OperationState.SUCCESS, index, previous, null, rest);
  }

  @Override
  public @Nullable ItemStack getItem(int index) {
    if (index >= getSlotCount()) throw new IndexOutOfBoundsException(index);
    return StoredItems.get(index);
  }

  @Override
  public List<ItemStack> getAllItems() {
    return new ArrayList<>(StoredItems);
  }

  @Override
  public void forEachItem(StackConsumer consumer) {
    for (var slot = 0; slot < StoredItems.size(); slot++)
      consumer.accept(slot, StoredItems.get(slot));
  }
}
