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

import static org.junit.jupiter.api.Assertions.*;

import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import org.echoesfrombeyond.test.HytaleIntegrationTest;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@NullMarked
@ExtendWith(HytaleIntegrationTest.class)
class ProxyContainerTest {
  private static class Statics {
    private static final ItemStack TEST_STACK_1 = new ItemStack("Soil_Dirt", 1);
    private static final ItemStack TEST_STACK_2 = new ItemStack("Soil_Grass", 1);
  }

  private static ProxyContainer getProxy(int size) {
    return new ProxyContainer(new SimpleItemContainer((short) size));
  }

  @Test
  void simpleAddItem() {
    var proxy = getProxy(16);

    proxy.addItem(Statics.TEST_STACK_1);
    assertSame(Statics.TEST_STACK_1, proxy.getItem(0));
  }

  @Test
  void addItemMerge() {
    var proxy = getProxy(16);

    proxy.addItem(Statics.TEST_STACK_1);
    proxy.addItem(Statics.TEST_STACK_1);

    var item = proxy.getItem(0);
    assertNotNull(item);
    assertEquals(2, item.getQuantity());
  }

  @Test
  void fullAdd() {
    var proxy = getProxy(1);

    var result = proxy.addItem(Statics.TEST_STACK_1);
    var result2 = proxy.addItem(Statics.TEST_STACK_2);

    assertEquals(StackContainer.OperationState.SUCCESS, result.operationState());
    assertEquals(StackContainer.OperationState.INVENTORY_FULL, result2.operationState());

    assertNull(result.remainder());
    assertEquals(Statics.TEST_STACK_2, result2.remainder());

    assertEquals(Statics.TEST_STACK_1, result.newStack());
    assertNull(result2.newStack());

    assertNull(result.previousStack());
    assertNull(result2.previousStack());
  }

  @Test
  void addAndRemove() {
    var proxy = getProxy(1);

    proxy.addItem(Statics.TEST_STACK_1);
    assertEquals(Statics.TEST_STACK_1, proxy.getItem(0));

    proxy.removeItem(0);
    assertNull(proxy.getItem(0));
  }
}
