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

package org.echoesfrombeyond.component.sigil;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.List;
import org.echoesfrombeyond.asset.SigilPattern;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class SigilQueueComponent implements Component<EntityStore> {
  private static @Nullable ComponentType<EntityStore, SigilQueueComponent> COMPONENT_TYPE;

  /**
   * Called internally during plugin initialization.
   *
   * @param proxy the registry proxy
   */
  @ApiStatus.Internal
  public static void registerComponentType(ComponentRegistryProxy<EntityStore> proxy) {
    COMPONENT_TYPE = proxy.registerComponent(SigilQueueComponent.class, SigilQueueComponent::new);
  }

  /**
   * @return the component type
   */
  public static ComponentType<EntityStore, SigilQueueComponent> getComponentType() {
    assert COMPONENT_TYPE != null;
    return COMPONENT_TYPE;
  }

  /** All sigils in the spell. */
  public final List<SigilPattern> patterns;

  public SigilQueueComponent() {
    this.patterns = new ArrayList<>();
  }

  public SigilQueueComponent(SigilQueueComponent other) {
    this.patterns = new ArrayList<>(other.patterns);
  }

  @Override
  @SuppressWarnings("MethodDoesntCallSuperMethod")
  public Component<EntityStore> clone() {
    return new SigilQueueComponent(this);
  }
}
