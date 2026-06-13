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

package org.echoesfrombeyond.modutil.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.Optional;
import org.joml.Vector3i;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ComponentUtils {
  private ComponentUtils() {
    throw new RuntimeException();
  }

  public static <T extends Component<ChunkStore>> Optional<T> getBlockComponent(
      World world, Vector3i position, ComponentType<ChunkStore, T> type) {
    var worldChunkComponent =
        world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(position.x, position.z));
    if (worldChunkComponent == null) return Optional.empty();

    var componentChunk = worldChunkComponent.getBlockComponentChunk();
    if (componentChunk == null) return Optional.empty();

    return Optional.ofNullable(
        componentChunk.getComponent(
            worldChunkComponent.getBlock(
                ChunkUtil.localCoordinate(position.x),
                position.y,
                ChunkUtil.localCoordinate(position.z)),
            type));
  }
}
