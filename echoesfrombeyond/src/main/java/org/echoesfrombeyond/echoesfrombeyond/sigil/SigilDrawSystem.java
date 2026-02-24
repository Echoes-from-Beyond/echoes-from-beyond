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

package org.echoesfrombeyond.echoesfrombeyond.sigil;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.echoesfrombeyond.codec.SigilPoint;
import org.echoesfrombeyond.component.ComponentUtils;
import org.echoesfrombeyond.component.sigil.SigilDrawComponent;
import org.echoesfrombeyond.sigil.SigilValidation;
import org.echoesfrombeyond.ui.hud.HudUtils;
import org.echoesfrombeyond.ui.hud.SigilHud;
import org.echoesfrombeyond.util.thread.Once;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Handles Sigil rendering and initial casting. */
@NullMarked
public class SigilDrawSystem extends EntityTickingSystem<EntityStore> {
  private final Once<Archetype<EntityStore>> archetype;

  /**
   * Creates a new instance of this class with the default {@link Archetype}, that consists of
   * {@link SigilDrawComponent}, {@link HeadRotation} and {@link Player}.
   */
  public SigilDrawSystem() {
    this.archetype =
        Once.of(
            () ->
                Archetype.of(
                    SigilDrawComponent.getComponentType(),
                    HeadRotation.getComponentType(),
                    Player.getComponentType()));
  }

  @Override
  public void tick(
      float v,
      int i,
      ArchetypeChunk<EntityStore> chunk,
      Store<EntityStore> store,
      CommandBuffer<EntityStore> commandBuffer) {
    var draw = ComponentUtils.assume(chunk, i, SigilDrawComponent.getComponentType());
    if (!draw.open) return;

    var player = ComponentUtils.assume(chunk, i, Player.getComponentType());
    var hud = HudUtils.getHud(SigilHud.class, player.getHudManager());
    if (hud == null) return;

    var head = ComponentUtils.assume(chunk, i, HeadRotation.getComponentType());
    var rot = head.getRotation().clone().subtract(draw.initialRotation);

    var pitch = rot.getPitch() * -1;
    var yaw = rot.getYaw() * -1;

    var x = Math.sin(yaw) * draw.gridDistance + 0.5;
    var y = Math.sin(pitch) * draw.gridDistance + 0.5;

    var xFloor = Math.floor(x);
    var yFloor = Math.floor(y);

    var toCenterX = x - (xFloor + 0.5);
    var toCenterY = y - (yFloor + 0.5);

    var distanceToCenterSquared = toCenterX * toCenterX + toCenterY * toCenterY;

    var gridX = Math.clamp((int) xFloor, 0, SigilValidation.GRID_SIZE);
    var gridY = Math.clamp((int) yFloor, 0, SigilValidation.GRID_SIZE);

    UICommandBuilder builder = null;

    var point = new SigilPoint(gridX, gridY);

    if (!point.equals(draw.highlighted) && distanceToCenterSquared < 0.4 * 0.4) {
      builder = new UICommandBuilder();

      hud.highlight(builder, draw.highlighted, false);
      hud.highlight(builder, point, true);

      draw.highlighted = point;
    }

    var cursorX = (float) Math.clamp(x, 0, SigilValidation.GRID_SIZE);
    var cursorY = (float) Math.clamp(y, 0, SigilValidation.GRID_SIZE);

    var toLastCursorX = cursorX - draw.lastCursorX;
    var toLastCursorY = cursorY - draw.lastCursorY;

    var distanceToLastCursorSquared = toLastCursorX * toLastCursorX + toLastCursorY * toLastCursorY;
    if (distanceToLastCursorSquared > 0.01) {
      if (builder == null) builder = new UICommandBuilder();

      hud.cursor(builder, cursorX, cursorY);

      draw.lastCursorX = cursorX;
      draw.lastCursorY = cursorY;
    }

    builder = maybeDrawLine(builder, draw, hud);

    if (builder != null) hud.update(false, builder);
  }

  private @Nullable UICommandBuilder maybeDrawLine(
      @Nullable UICommandBuilder builder, SigilDrawComponent draw, SigilHud hud) {
    if (!draw.drawing
        || draw.points.isEmpty()
        || draw.points.size() >= SigilValidation.MAX_SIGIL_LENGTH) return builder;

    var mostRecentPoint = draw.points.getLast();
    if (!mostRecentPoint.isAdjacentTo(draw.highlighted)) return builder;

    int length = draw.points.size();
    boolean set = length == 1 || !draw.points.get(length - 2).equals(draw.highlighted);

    if (set) {
      for (int i = 0; i < length - 1; i++) {
        if (!draw.points.get(i).equals(mostRecentPoint)) continue;

        if ((i > 0 && draw.points.get(i - 1).equals(draw.highlighted))
            || draw.points.get(i + 1).equals(draw.highlighted)) return builder;
      }
    }

    if (builder == null) builder = new UICommandBuilder();
    hud.line(builder, mostRecentPoint, draw.highlighted, set);

    if (set) draw.points.add(draw.highlighted);
    else draw.points.removeLast();

    return builder;
  }

  @Override
  public Query<EntityStore> getQuery() {
    return archetype.get();
  }
}
