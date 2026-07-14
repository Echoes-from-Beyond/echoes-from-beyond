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
import com.hypixel.hytale.server.core.inventory.transaction.ActionType;
import com.hypixel.hytale.server.core.inventory.transaction.SlotTransaction;
import org.echoesfrombeyond.modutil.inventory.InventoryContainer.OperationState;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A data class used to communicate results of an inventory operation.
 *
 * <p>Deliberately distinct from Hytale's {@link SlotTransaction} (and various children) so that the
 * error reasons can be communicated, and irrelevant fields are omitted.
 */
@NullMarked
public class InventoryOperationResult {
  private final ActionType operation;

  private final OperationState operationState;

  private final int slotIndex;

  // item that was replaced
  // can be null in case of addition
  @Nullable private final ItemStack previousItem;

  // what you can find now at that slot
  // can be null in case of removal, but also can be an item that can now be found at that index
  // if the inventory does not permit empty slots, e.g. the overflow inventory in Echoes from
  // Beyond's alchemy stations
  @Nullable private final ItemStack newItem;

  public InventoryOperationResult(
      ActionType operation,
      OperationState operationState,
      int slotIndex,
      @Nullable ItemStack previousItem,
      @Nullable ItemStack newItem) {
    this.operation = operation;
    this.operationState = operationState;
    this.slotIndex = slotIndex;
    this.previousItem = previousItem;
    this.newItem = newItem;
  }

  public ActionType getOperation() {
    return operation;
  }

  public OperationState getOperationState() {
    return operationState;
  }

  public int getSlotIndex() {
    return slotIndex;
  }

  public @Nullable ItemStack getPreviousItem() {
    return previousItem;
  }

  public @Nullable ItemStack getNewItem() {
    return newItem;
  }
}
