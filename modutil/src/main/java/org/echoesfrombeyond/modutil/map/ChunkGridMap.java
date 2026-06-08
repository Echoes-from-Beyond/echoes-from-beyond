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

package org.echoesfrombeyond.modutil.map;

import com.hypixel.hytale.math.util.ChunkUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.function.BiConsumer;
import org.joml.Vector3i;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class ChunkGridMap<V extends @Nullable Object> {
  private final Long2ObjectMap<Int2ObjectMap<V>> storage;

  public ChunkGridMap() {
    this.storage = new Long2ObjectOpenHashMap<>();
  }

  @SuppressWarnings("ConstantValue")
  public @Nullable V get(Vector3i at) {
    var chunkX = ChunkUtil.chunkCoordinate(at.x);
    var chunkZ = ChunkUtil.chunkCoordinate(at.z);

    var chunkData = storage.get(ChunkUtil.indexChunk(chunkX, chunkZ));
    if (chunkData == null) return null;

    return chunkData.get(ChunkUtil.indexBlockInColumn(at.x, at.y, at.z));
  }

  public V put(Vector3i at, V value) {
    var chunkX = ChunkUtil.chunkCoordinate(at.x);
    var chunkZ = ChunkUtil.chunkCoordinate(at.z);

    return storage
        .computeIfAbsent(ChunkUtil.indexChunk(chunkX, chunkZ), _ -> new Int2ObjectOpenHashMap<>())
        .put(ChunkUtil.indexBlockInColumn(at.x, at.y, at.z), value);
  }

  @SuppressWarnings("ConstantValue")
  public V remove(Vector3i at) {
    var chunkX = ChunkUtil.chunkCoordinate(at.x);
    var chunkZ = ChunkUtil.chunkCoordinate(at.z);

    var chunkIndex = ChunkUtil.indexChunk(chunkX, chunkZ);
    var chunkData = storage.get(chunkIndex);
    if (chunkData == null) return null;

    var returnValue = chunkData.remove(ChunkUtil.indexBlockInColumn(at.x, at.y, at.z));

    // clean up empty storage
    if (chunkData.isEmpty()) storage.remove(chunkIndex);

    return returnValue;
  }

  @SuppressWarnings("ConstantValue")
  public void forEachInRange(
      Vector3i center, int radius, BiConsumer<? super Vector3i, ? super V> callback) {
    var chunkStartX = ChunkUtil.chunkCoordinate(center.x - radius);
    var chunkEndX = ChunkUtil.chunkCoordinate(center.x + radius);

    var chunkStartZ = ChunkUtil.chunkCoordinate(center.z - radius);
    var chunkEndZ = ChunkUtil.chunkCoordinate(center.z + radius);

    var dataPosition = new Vector3i();

    for (int cx = chunkStartX; cx <= chunkEndX; cx++) {
      for (int cz = chunkStartZ; cz <= chunkEndZ; cz++) {
        var chunkData = storage.get(ChunkUtil.indexChunk(cx, cz));
        if (chunkData == null) continue;

        for (var entry : chunkData.int2ObjectEntrySet()) {
          var index = entry.getIntKey();

          dataPosition.set(
              ChunkUtil.worldCoordFromLocalCoord(cx, ChunkUtil.xFromIndex(index)),
              ChunkUtil.yFromBlockInColumn(index),
              ChunkUtil.worldCoordFromLocalCoord(cz, ChunkUtil.zFromIndex(index)));

          if (dataPosition.gridDistance(center) <= radius)
            callback.accept(dataPosition, entry.getValue());
        }
      }
    }
  }
}
