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

package org.echoesfrombeyond.ui.hud;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.Anchor;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.echoesfrombeyond.codec.SigilPoint;
import org.echoesfrombeyond.sigil.SigilValidation;
import org.echoesfrombeyond.system.sigil.SigilDrawSystem;
import org.jspecify.annotations.NullMarked;

/**
 * Used to render Sigils as they are being drawn. This class is driven by {@link SigilDrawSystem}
 * and does not contain much logic on its own.
 */
@NullMarked
public class SigilHud extends CustomUIHud {
  private static final String[] HIGHLIGHT_SELECTOR_LOOKUP;

  // RGBA: Fully transparent
  private static final String BASE_COLOR = "#00000000";

  // RGBA: Half-transparent white
  private static final String HIGHLIGHT_COLOR = "#FFFFFF80";

  private static final int SHIFT = Integer.numberOfTrailingZeros(SigilValidation.GRID_SIZE);
  private static final int MASK = (SigilValidation.GRID_SIZE * SigilValidation.GRID_SIZE) - 1;

  static {
    HIGHLIGHT_SELECTOR_LOOKUP = new String[SigilValidation.GRID_SIZE * SigilValidation.GRID_SIZE];

    for (int x = 0; x < SigilValidation.GRID_SIZE; x++) {
      for (int y = 0; y < SigilValidation.GRID_SIZE; y++) {
        HIGHLIGHT_SELECTOR_LOOKUP[index(x, y)] = "#P" + x + y + ".Background";
      }
    }
  }

  private static int index(int x, int y) {
    return x << SHIFT | y;
  }

  /**
   * Converts Sigil grid coordinates to pixel coordinates. Grid coordinates start at (0, 0) (at the
   * very top left of the grid). (0.5, 0.5) is the center of the top left cell.
   *
   * @param c the grid coordinate
   * @return the pixel coordinate equivalent
   */
  public static float gridToPixelCoordinates(float c) {
    // Update this value if changing the cell width in Sigil_Hud.ui.
    return c * 150;
  }

  private final Anchor cursor;

  /**
   * Creates a new instance of this HUD for the specified player.
   *
   * @param playerRef the player reference component
   */
  public SigilHud(PlayerRef playerRef) {
    super(playerRef);

    this.cursor = new Anchor();
    this.cursor.setWidth(Value.of(5));
    this.cursor.setHeight(Value.of(5));
  }

  @Override
  protected void build(UICommandBuilder builder) {
    builder.append("Sigil_Hud.ui");
  }

  /**
   * Sets or clears the highlight of a specific Sigil grid square.
   *
   * <p>This function appends one or more commands to {@code builder}, but does not update the UI
   * for the client. It is necessary to call {@link SigilHud#update(boolean, UICommandBuilder)} with
   * {@code builder} after.
   *
   * @param builder the builder
   * @param point the point to highlight or clear
   * @param set if {@code true}, highlights the grid square; otherwise clears the highlight status
   */
  public void highlight(UICommandBuilder builder, SigilPoint point, boolean set) {
    // Use a bitmask here because it keeps the index in bounds of the array.
    var selector = HIGHLIGHT_SELECTOR_LOOKUP[index(point.x(), point.y()) & MASK];

    if (set) builder.set(selector, HIGHLIGHT_COLOR);
    else builder.set(selector, BASE_COLOR);
  }

  /**
   * Updates the cursor position. {@code x} and {@code y} are given in grid coordinates; for example
   * (0.5, 0.5) is the center of the top-left Sigil grid cell.
   *
   * <p>This function appends one or more commands to {@code builder}, but does not update the UI
   * for the client. It is necessary to call {@link SigilHud#update(boolean, UICommandBuilder)} with
   * {@code builder} after.
   *
   * @param builder the builder
   * @param x the cursor x-coordinate
   * @param y the cursor y-coordinate
   */
  public void cursor(UICommandBuilder builder, float x, float y) {
    cursor.setLeft(Value.of((int) Math.rint(gridToPixelCoordinates(x))));
    cursor.setTop(Value.of((int) Math.rint(gridToPixelCoordinates(y))));

    builder.setObject("#Cursor.Anchor", cursor);
  }

  /**
   * Adds or removes a line between two Sigil grid cells. This function will do nothing if {@code
   * !first.isAdjacentTo(second)}.
   *
   * <p>This function appends one or more commands to {@code builder}, but does not update the UI
   * for the client. It is necessary to call {@link SigilHud#update(boolean, UICommandBuilder)} with
   * {@code builder} after.
   *
   * @param builder the builder
   * @param first the first point
   * @param second the second point
   * @param set if {@code true}, adds a line, otherwise removes an existing one
   */
  public void line(UICommandBuilder builder, SigilPoint first, SigilPoint second, boolean set) {
    if (!first.isAdjacentTo(second)) return;

    int indexA = index(first.x(), first.y()) & MASK;
    int indexB = index(second.x(), second.y()) & MASK;

    int firstIndex = Math.min(indexA, indexB);
    int secondIndex = Math.max(indexA, indexB);

    builder.set("#L" + firstIndex + "t" + secondIndex + ".Visible", set);
  }

  /**
   * Unsets all lines made by {@code points}. Unsetting a line is performed as if by calling {@code
   * line(builder, first, second, false)}.
   *
   * <p>This function appends one or more commands to {@code builder}, but does not update the UI
   * for the client. It is necessary to call {@link SigilHud#update(boolean, UICommandBuilder)} with
   * {@code builder} after.
   *
   * @param builder the builder
   * @param points an {@link Iterable} of all points to remove
   */
  public void unsetLines(UICommandBuilder builder, Iterable<SigilPoint> points) {
    SigilPoint first = null;

    for (SigilPoint second : points) {
      if (first != null) line(builder, first, second, false);
      first = second;
    }
  }
}
