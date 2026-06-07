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

package org.echoesfrombeyond.echoesfrombeyond;

import com.hypixel.hytale.math.util.ChunkUtil;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3i;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class AlchemyNetworks {
  public static class Network {
    public final Vector3i origin;
    public final List<Vector3i> children;

    public Network(int originX, int originY, int originZ) {
      this.origin = new Vector3i(originX, originY, originZ);
      this.children = new ArrayList<>();
    }

    public boolean isInside(int x, int y, int z, int networkRadius) {
      return Math.abs(x - origin.x) + Math.abs(y - origin.y) + Math.abs(z - origin.z)
          <= networkRadius;
    }
  }

  private final int networkRadius;
  private final int chunkExplore;
  private final Long2ObjectMap<List<Network>> networks;

  public AlchemyNetworks(int networkRadius) {
    if (networkRadius <= 0) throw new IllegalArgumentException("networkRadius must be > 0");

    this.networkRadius = networkRadius;
    this.chunkExplore = 1 + (networkRadius / ChunkUtil.SIZE);
    this.networks = new Long2ObjectOpenHashMap<>();
  }

  public @Nullable Network addNetwork(int originX, int originY, int originZ) {
    if (getEnclosingNetwork(originX, originY, originZ) != null) return null;

    var network = new Network(originX, originY, originZ);
    networks
        .computeIfAbsent(ChunkUtil.indexChunkFromBlock(originX, originZ), _ -> new ArrayList<>())
        .add(network);
    return network;
  }

  public boolean addToEnclosingNetwork(int x, int y, int z) {
    var currentNetwork = getEnclosingNetwork(x, y, z);
    if (currentNetwork == null) return false;

    currentNetwork.children.add(new Vector3i(x, y, z));
    return true;
  }

  @SuppressWarnings("ConstantValue")
  public void removeNetwork(int originX, int originY, int originZ) {
    var list = networks.get(ChunkUtil.indexChunkFromBlock(originX, originZ));
    if (list == null) return;

    list.removeIf(network -> network.origin.equals(originX, originY, originZ));
  }

  public void removeFromEnclosingNetwork(int x, int y, int z) {
    var enclosing = getEnclosingNetwork(x, y, z);
    if (enclosing == null) return;

    if (enclosing.origin.equals(x, y, z)) removeNetwork(x, y, z);
    else enclosing.children.remove(new Vector3i(x, y, z));
  }

  @SuppressWarnings("ConstantValue")
  public @Nullable Network getEnclosingNetwork(int x, int y, int z) {
    var chunkX = ChunkUtil.chunkCoordinate(x);
    var chunkZ = ChunkUtil.chunkCoordinate(z);

    Network foundNetwork = null;
    for (int cx = chunkX - chunkExplore; cx <= chunkX + chunkExplore; cx++) {
      for (int cz = chunkZ - chunkExplore; cz <= chunkZ + chunkExplore; cz++) {
        var networksInChunk = networks.get(ChunkUtil.indexChunk(cx, cz));
        if (networksInChunk == null) continue;

        for (var network : networksInChunk)
          if (network.isInside(x, y, z, networkRadius)) {
            if (foundNetwork != null) return null;
            foundNetwork = network;
          }
      }
    }

    return foundNetwork;
  }
}
