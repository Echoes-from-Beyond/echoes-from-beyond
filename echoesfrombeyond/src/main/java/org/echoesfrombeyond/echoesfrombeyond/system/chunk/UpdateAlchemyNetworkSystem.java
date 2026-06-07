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

package org.echoesfrombeyond.echoesfrombeyond.system.chunk;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.echoesfrombeyond.echoesfrombeyond.component.chunk.AlchemyBench;
import org.joml.Vector3i;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class UpdateAlchemyNetworkSystem extends RefSystem<ChunkStore> {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  private final ComponentType<ChunkStore, BlockModule.BlockStateInfo> blockStateInfoComponentType;

  public UpdateAlchemyNetworkSystem() {
    this.blockStateInfoComponentType = BlockModule.BlockStateInfo.getComponentType();
  }

  @Override
  public void onEntityAdded(
      Ref<ChunkStore> ref,
      AddReason reason,
      Store<ChunkStore> store,
      CommandBuffer<ChunkStore> buf) {
    var stateInfo = buf.getComponent(ref, blockStateInfoComponentType);

    if (stateInfo == null) return;

    var chunkRef = stateInfo.getChunkRef();
    if (!chunkRef.isValid()) return;

    var blockChunk = buf.getComponent(chunkRef, BlockChunk.getComponentType());
    if (blockChunk == null) return;

    var alchemyBench = buf.getComponent(ref, AlchemyBench.getComponentType());
    assert alchemyBench != null;

    var blockIndex = stateInfo.getIndex();

    int localX = ChunkUtil.xFromBlockInColumn(blockIndex);
    int localY = ChunkUtil.yFromBlockInColumn(blockIndex);
    int localZ = ChunkUtil.zFromBlockInColumn(blockIndex);

    int blockX = ChunkUtil.worldCoordFromLocalCoord(blockChunk.getX(), localX);
    int blockZ = ChunkUtil.worldCoordFromLocalCoord(blockChunk.getZ(), localZ);

    alchemyBench.setBlockLocation(blockX, localY, blockZ);

    LOGGER.atInfo().log(
        "Added alchemy bench at coordinates " + new Vector3i(blockX, localY, blockZ));
    LOGGER.atInfo().log("Is the bench a network origin bench? " + alchemyBench.isNetworkOrigin());
  }

  @Override
  public void onEntityRemove(
      Ref<ChunkStore> ref,
      RemoveReason reason,
      Store<ChunkStore> store,
      CommandBuffer<ChunkStore> buf) {
    var alchemyBench = buf.getComponent(ref, AlchemyBench.getComponentType());
    assert alchemyBench != null;

    alchemyBench.clearLocation();

    LOGGER.atInfo().log("Removed alchemy bench");
  }

  @Override
  public Query<ChunkStore> getQuery() {
    return AlchemyBench.getComponentType();
  }
}
