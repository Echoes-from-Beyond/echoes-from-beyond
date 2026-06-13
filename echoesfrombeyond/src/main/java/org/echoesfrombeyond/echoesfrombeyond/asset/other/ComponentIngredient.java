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

package org.echoesfrombeyond.echoesfrombeyond.asset.other;

import org.echoesfrombeyond.codechelper.annotation.Doc;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.jspecify.annotations.NullMarked;

@Doc(
    """
    Associates an alchemical component with a quantity of units to use/produce as part of a reaction.
    """)
@NullMarked
@ModelBuilder
public class ComponentIngredient {
  // note for developers: this one is not meant to be used as a standalone asset
  // reference this class as a field in other assets
  @Doc(
      """
      The identifier of the alchemical component.
      """)
  public String ComponentId;

  @Doc(
      """
      How many units of the above component is used for/produced in this step.
      """)
  public int Quantity;

  public ComponentIngredient() {
    ComponentId = "";
    Quantity = 1;
  }
}
