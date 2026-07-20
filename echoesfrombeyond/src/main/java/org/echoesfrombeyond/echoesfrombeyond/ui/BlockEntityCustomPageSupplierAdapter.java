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

package org.echoesfrombeyond.echoesfrombeyond.ui;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Abstract class uniting both {@link OpenCustomUIInteraction.CustomPageSupplier} and {@link
 * OpenCustomUIInteraction.BlockEntityCustomPageSupplier}.
 *
 * <p>Necessary to avoid needing the registration API in {@link OpenCustomUIInteraction}, which is
 * incomplete (does not support user-provided codecs).
 */
@NullMarked
public abstract class BlockEntityCustomPageSupplierAdapter
    implements OpenCustomUIInteraction.CustomPageSupplier,
        OpenCustomUIInteraction.BlockEntityCustomPageSupplier {
  @Override
  public final @Nullable CustomUIPage tryCreate(
      Ref<EntityStore> ref,
      ComponentAccessor<EntityStore> componentAccessor,
      PlayerRef playerRef,
      InteractionContext interactionContext) {
    var targetBlock = interactionContext.getTargetBlock();
    if (targetBlock == null) return null;

    var chunkStore = ref.getStore().getExternalData().getWorld().getChunkStore();

    var chunkRef =
        chunkStore.getChunkReference(ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z));
    if (chunkRef == null || !chunkRef.isValid()) return null;

    var worldChunkComponent =
        chunkStore.getStore().getComponent(chunkRef, WorldChunk.getComponentType());
    if (worldChunkComponent == null) return null;

    var cx = ChunkUtil.localCoordinate(targetBlock.x);
    var cz = ChunkUtil.localCoordinate(targetBlock.z);

    var blockEntityRef = worldChunkComponent.getBlockComponentEntity(cx, targetBlock.y, cz);
    if (blockEntityRef == null || !blockEntityRef.isValid()) return null;

    return tryCreate(playerRef, blockEntityRef);
  }

  public abstract @Nullable CustomUIPage tryCreate(PlayerRef ref, Ref<ChunkStore> blockEntityRef);
}
