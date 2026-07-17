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
    private static final ItemStack TEST_STACK_1 = new ItemStack("Test", 1);
  }

  @Test
  void simpleAddItem() {
    var container = new SimpleItemContainer((short) 16);
    var proxy = new ProxyContainer(container);

    proxy.addItem(Statics.TEST_STACK_1);
    assertSame(Statics.TEST_STACK_1, container.getItemStack((short) 0));
  }
}
