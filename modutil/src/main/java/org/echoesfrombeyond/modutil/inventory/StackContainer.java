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
 *
 * <p>This is deliberately distinct from Hytale's built-in {@link ItemContainer}, in order to give
 * us more control over the functionality of custom inventories, and present a simplified API.
 */
@NullMarked
public interface StackContainer {
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

  /** The result of modifying the inventory. */
  enum OperationState {
    /**
     * The inventory was successfully modified. Prefer to call {@link OperationState#isSuccess()}
     * rather than comparing against this variant.
     */
    SUCCESS,

    /** The inventory wasn't modified because it doesn't permit such operations at this time. */
    INVENTORY_FROZEN,

    /** The item wasn't added because the inventory is full. */
    INVENTORY_FULL,

    /** The target item wasn't modified because it is somehow invalid for the container. */
    INVALID_ITEM,

    /**
     * The target item couldn't be modified because nothing is stored at a given index. Should only
     * apply to inventories that allow null values.
     */
    EMPTY_SLOT;

    /**
     * Whether this was a success result. Prefer to call this method instead of comparing against
     * {@link OperationState#SUCCESS}.
     *
     * @return {@code true} if successful; {@code false otherwise}
     */
    public boolean isSuccess() {
      return this == SUCCESS;
    }

    /**
     * Whether this operation was a failure result. Prefer to call this method instead of comparing
     * against {@link OperationState#SUCCESS}.
     *
     * @return {@code true} if failed; {@code false otherwise}
     */
    public boolean isFailure() {
      return this != SUCCESS;
    }
  }

  /**
   * Gets the number of filled slots in this container, i.e. slots that currently have an item. To
   * get the number of slots regardless of if they are empty or not, call {@link
   * StackContainer#getSlotCount()}.
   *
   * <p>The default implementation uses {@link StackContainer#forEachItem(StackConsumer)} to count
   * the filled slots.
   *
   * @return the number of slots that are filled
   */
  default int getFilledSlots() {
    var count = new int[1];
    forEachItem((_, _) -> count[0]++);
    return count[0];
  }

  /**
   * Gets the number of slots in the inventory, whether they are empty or not. This should be
   * strictly larger than or equal to {@link StackContainer#getFilledSlots()}.
   *
   * <p>For inventories that dynamically increase in size, this value can change as items are added
   * or removed.
   *
   * @return the number of item slots in the inventory
   */
  int getSlotCount();

  /**
   * Gets the number of items in the inventory. This is the sum of the quantities of every {@link
   * ItemStack} in the inventory.
   *
   * <p>The default implementation calls {@link StackContainer#forEachItem(StackConsumer)} to loop
   * through all stacks, summing their quantities. Implementations may choose to override this
   * method if they can provide a more optimized approach.
   *
   * @return the number of items in the inventory
   */
  default int getItemCount() {
    var count = new int[1];
    forEachItem((_, stack) -> count[0] += stack.getQuantity());
    return count[0];
  }

  /**
   * Adds an item to the container. This may cause the item to occupy a new slot, or it may be
   * stacked with an existing item, depending on the implementation.
   *
   * @param stack the item to add
   * @return the result of adding the item
   */
  InventoryOperationResult addItem(ItemStack stack);

  /**
   * Operates similarly to {@link StackContainer#addItem(ItemStack)}, but inserts the item at the
   * specified index.
   *
   * @param stack the item to add
   * @param index the index to add the item at
   * @return the result of adding the item
   * @throws IndexOutOfBoundsException if {@code index > getSlotCount()}
   */
  InventoryOperationResult addItem(ItemStack stack, int index);

  /**
   * Sets the item at a certain index. If there is already an item in the slot, it is replaced.
   *
   * @param stack the item to set
   * @param index the index
   * @return the result of setting the item
   * @throws IndexOutOfBoundsException if {@code index >= getSlotCount()}
   */
  InventoryOperationResult setItem(ItemStack stack, int index);

  /**
   * Removes the entire stack at a certain index.
   *
   * @param index the index
   * @return the result of removing the item
   * @throws IndexOutOfBoundsException if {@code index >= getSlotCount()}
   */
  InventoryOperationResult removeItem(int index);

  /**
   * Gets the entire stack at a certain index.
   *
   * @param index the index
   * @return the item, or {@code null} if not present
   * @throws IndexOutOfBoundsException if {@code index >= getSlotCount()}
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
