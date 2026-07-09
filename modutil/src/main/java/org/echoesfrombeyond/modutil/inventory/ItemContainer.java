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
import java.util.List;
import java.util.function.Consumer;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * An interface representing a container of {@link ItemStack}. Items may be added to or removed from
 * the container.
 *
 * <p>Can represent a traditional fixed grid of item slots or a growable list.
 */
@NullMarked
public interface ItemContainer {
  /** A {@link Consumer} that accepts an {@code int} and an {@link ItemStack}. */
  @FunctionalInterface
  interface StackConsumer {
    /**
     * Accepts the slot and item stack.
     *
     * @param slot the slot index
     * @param stack the item stack
     */
    void accept(int slot, ItemStack stack);
  }

  /** The result of adding an item to an inventory. */
  enum AddResult {
    /**
     * The item was successfully added. Prefer to call {@link AddResult#isSuccess()} rather than
     * comparing against this variant.
     */
    SUCCESS,

    /** The item wasn't added because the inventory isn't accepting new items at this time. */
    INVENTORY_FROZEN,

    /** The item wasn't added because the inventory is full. */
    TARGET_FULL,

    /** The item wasn't added because it is somehow invalid for the container. */
    INVALID_ITEM;

    /**
     * Whether this was a success result. Prefer to call this method instead of comparing against
     * {@link AddResult#SUCCESS}.
     */
    public boolean isSuccess() {
      return this == SUCCESS;
    }
  }

  /**
   * The result of setting a slot.
   *
   * @param result the added item
   * @param oldItem the item that was in the slot previously; {@code null} if there was no item
   */
  record SetResult(AddResult result, @Nullable ItemStack oldItem) {
    public SetResult {
      if (!result.isSuccess() && oldItem != null) throw new IllegalArgumentException();
    }

    /**
     * Whether the item was successfully set.
     *
     * @return {@code true} if successful; {@code false} if not
     */
    public boolean isSuccess() {
      return result.isSuccess();
    }
  }

  /**
   * Gets the number of items in this container. For fixed-size inventories, this only counts slots
   * that currently have an item. To get the number of slots, call {@link
   * ItemContainer#getItemCapacity()}.
   *
   * @return the number of items in this container
   */
  int getItemCount();

  /**
   * Gets the number of items this container can hold, i.e. the largest valid index + 1. For fixed
   * size inventories, this would be the number of slots. For growable inventories, this is equal to
   * {@link ItemContainer#getItemCount()}.
   *
   * @return the capacity of items
   */
  int getItemCapacity();

  /**
   * Adds an item to the container. This may cause the item to occupy a new slot, or it may be
   * stacked with an existing item, depending on the implementation.
   *
   * @param stack the item to add
   * @return the result of adding the item
   */
  AddResult addItem(ItemStack stack);

  /**
   * Operates similarly to {@link ItemContainer#addItem(ItemStack)}, but inserts the item at the
   * specified index.
   *
   * @param stack the item to add
   * @param index the index to add the item at
   * @return the result of adding the item
   * @throws IndexOutOfBoundsException if {@code index > getItemCapacity()}
   */
  AddResult addItem(ItemStack stack, int index);

  /**
   * Sets the item at a certain index. If there is already an item in the slot, it is replaced.
   *
   * @param stack the item to set
   * @param index the index
   * @return the result of adding the item
   * @throws IndexOutOfBoundsException if {@code index >= getItemCapacity()}
   */
  SetResult setItem(ItemStack stack, int index);

  /**
   * Removes the entire stack at a certain index.
   *
   * @param index the index
   * @return the removed item, or {@code null} if not present
   * @throws IndexOutOfBoundsException if {@code index >= getItemCapacity()}
   */
  @Nullable ItemStack removeItem(int index);

  /**
   * Gets the entire stack at a certain index.
   *
   * @param index the index
   * @return the removed item, or {@code null} if not present
   * @throws IndexOutOfBoundsException if {@code index >= getItemCapacity()}
   */
  @Nullable ItemStack getItem(int index);

  /**
   * Gets all items in the inventory as a list.
   *
   * @return all items contained in a new mutable list
   */
  @Contract("->new")
  List<ItemStack> getAllItems();

  /**
   * Calls {@link StackConsumer#accept(int, ItemStack)} for each item stack in the container.
   *
   * @param consumer the consumer that accepts the items
   */
  void forEachItem(StackConsumer consumer);
}
