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

package org.echoesfrombeyond.echoesfrombeyond;

import com.hypixel.hytale.server.core.inventory.ItemStack;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.util.Check;
import org.jspecify.annotations.NullMarked;

@NullMarked
@ModelBuilder
public class ItemEntry {
  public static ItemEntry[] EMPTY_ITEM_ENTRY_ARRAY = new ItemEntry[0];

  @SuppressWarnings("FieldMayBeFinal")
  private String Id;

  private int Quantity;

  @SuppressWarnings("unused")
  private ItemEntry() {
    Id = "";
  }

  public ItemEntry(ItemStack stack) {
    this.Id = stack.getItemId();
    this.Quantity = stack.getQuantity();
  }

  public ItemEntry(String id, int quantity) {
    this.Id = Check.nonNull(id);
    this.Quantity = quantity;
  }

  public ItemEntry add(int amount) {
    return new ItemEntry(Id, Quantity + amount);
  }

  public String getId() {
    return Id;
  }

  public int getQuantity() {
    return Quantity;
  }
}
