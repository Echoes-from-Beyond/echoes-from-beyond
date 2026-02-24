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

package org.echoesfrombeyond.echoesfrombeyond.component.sigil;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.List;
import org.echoesfrombeyond.echoesfrombeyond.codec.SigilPoint;
import org.echoesfrombeyond.echoesfrombeyond.hud.SigilHud;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Tracks progress of a Sigil as it is being drawn. */
@NullMarked
public class SigilDrawComponent implements Component<EntityStore> {
  /** Default value for {@link SigilDrawComponent#gridDistance}. */
  public static final float DEFAULT_GRID_DISTANCE = 4;

  /** Initial x and y coordinate for the Sigil draw cursor. */
  public static final float DEFAULT_CURSOR_XY = SigilHud.gridToPixelCoordinates(0.5F);

  private static @Nullable ComponentType<EntityStore, SigilDrawComponent> COMPONENT_TYPE;

  /**
   * Called internally during plugin initialization.
   *
   * @param proxy the registry proxy
   */
  @ApiStatus.Internal
  public static void registerComponentType(ComponentRegistryProxy<EntityStore> proxy) {
    COMPONENT_TYPE = proxy.registerComponent(SigilDrawComponent.class, SigilDrawComponent::new);
  }

  /**
   * @return the component type
   */
  public static ComponentType<EntityStore, SigilDrawComponent> getComponentType() {
    assert COMPONENT_TYPE != null;
    return COMPONENT_TYPE;
  }

  /** Whether the Sigil HUD is open. */
  public boolean open;

  /** Whether the player has started drawing the sigil. */
  public boolean drawing;

  /** The starting rotation of the player. */
  public Vector3f initialRotation;

  /** Distance the grid is from the player. */
  public float gridDistance;

  /** Last X position of the cursor. */
  public float lastCursorX;

  /** Last Y position of the cursor. */
  public float lastCursorY;

  /** Which square is currently highlighted. */
  public SigilPoint highlighted;

  /** Points in the sigil that have been drawn thus far. */
  public List<SigilPoint> points;

  public SigilDrawComponent() {
    this.open = false;
    this.drawing = false;
    this.initialRotation = new Vector3f(0, 0, 0);
    this.gridDistance = DEFAULT_GRID_DISTANCE;
    this.lastCursorX = DEFAULT_CURSOR_XY;
    this.lastCursorY = DEFAULT_CURSOR_XY;
    this.highlighted = SigilPoint.ZERO;
    this.points = new ArrayList<>();
  }

  private SigilDrawComponent(SigilDrawComponent other) {
    this.open = other.open;
    this.drawing = other.drawing;
    this.initialRotation = other.initialRotation.clone();
    this.gridDistance = other.gridDistance;
    this.lastCursorX = other.lastCursorX;
    this.lastCursorY = other.lastCursorY;
    this.highlighted = other.highlighted;
    this.points = new ArrayList<>(other.points);
  }

  public void reset() {
    this.open = false;
    this.drawing = false;
    this.initialRotation.assign(0, 0, 0);
    this.gridDistance = DEFAULT_GRID_DISTANCE;
    this.lastCursorX = DEFAULT_CURSOR_XY;
    this.lastCursorY = DEFAULT_CURSOR_XY;
    this.highlighted = SigilPoint.ZERO;
    this.points.clear();
  }

  @Override
  @SuppressWarnings("MethodDoesntCallSuperMethod")
  public SigilDrawComponent clone() {
    return new SigilDrawComponent(this);
  }
}
