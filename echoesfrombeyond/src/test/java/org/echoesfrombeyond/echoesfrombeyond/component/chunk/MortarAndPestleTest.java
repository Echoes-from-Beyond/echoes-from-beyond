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

package org.echoesfrombeyond.echoesfrombeyond.component.chunk;

import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import java.lang.reflect.InvocationTargetException;
import org.echoesfrombeyond.test.HytaleIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(HytaleIntegrationTest.class)
class MortarAndPestleTest {
  @Test
  void roundTrip() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    var mortarAndPestle = new MortarAndPestle();

    // mortarAndPestle.ItemsToGrind = new AlchemyStackContainer(10);
    // mortarAndPestle.ItemsToGrind.addItem(new ItemStack("Soil_Dirt", 1));

    mortarAndPestle
        .ItemsToGrind
        .getClass()
        .getMethod("addItem", ItemStack.class)
        .invoke(mortarAndPestle.ItemsToGrind, new ItemStack("Soil_Dirt", 1));

    var encoded = MortarAndPestle.CODEC.encode(mortarAndPestle, new ExtraInfo());

    System.out.println(encoded);
  }
}
