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

package org.echoesfrombeyond.echoesfrombeyond.ui.supplier;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.Doc;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.codechelper.annotation.validator.ValidateIntRange;
import org.echoesfrombeyond.echoesfrombeyond.component.chunk.MortarAndPestle;
import org.echoesfrombeyond.echoesfrombeyond.ui.AlchemyBenchSupplier;
import org.echoesfrombeyond.echoesfrombeyond.ui.page.BenchMortarPage;
import org.echoesfrombeyond.echoesfrombeyond.util.AlchemyBenchUtils;
import org.joml.Vector3i;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@ModelBuilder
public class BenchMortarSupplier extends AlchemyBenchSupplier {
  public static final BuilderCodec<BenchMortarSupplier> CODEC =
      CodecUtil.modelBuilder(BenchMortarSupplier.class, Plugin.getSharedResolver());

  @Doc(
      """
      When the player uses the mortar, this is a baseline number of interactions (including the initiating one)
      for the crafting to complete, no matter the amount of ingredients.
      Cannot be lower than 1.
      """)
  @ValidateIntRange(min = 1, max = Integer.MAX_VALUE)
  protected int BaselineInteractions;

  @Doc(
      """
      When the player uses the mortar, this is a number of interactions added per each ingredient to the crafting requirement.
      Cannot be lower than 0.
      """)
  @ValidateIntRange(min = 0, max = Integer.MAX_VALUE)
  protected int InteractionsPerIngredient;

  @Override
  public @Nullable CustomUIPage tryCreate(PlayerRef playerRef, Ref<ChunkStore> blockEntity, WorldChunk worldChunk) {
    var ref = playerRef.getReference();
    if (ref == null) return null;

    var store = blockEntity.getStore();

    var blockStateInfo =
        store.getComponent(blockEntity, BlockModule.BlockStateInfo.getComponentType());
    var mortarAndPestle = store.getComponent(blockEntity, MortarAndPestle.getComponentType());

    assert blockStateInfo != null;
    assert mortarAndPestle != null;

    var index = blockStateInfo.getIndex();
    var cx = ChunkUtil.xFromBlockInColumn(index);
    var cy = ChunkUtil.yFromBlockInColumn(index);
    var cz = ChunkUtil.zFromBlockInColumn(index);

    var position =
        new Vector3i(
            ChunkUtil.worldCoordFromLocalCoord(worldChunk.getX(), cx),
            cy,
            ChunkUtil.worldCoordFromLocalCoord(worldChunk.getZ(), cz));

    var validIngredients =
        AlchemyBenchUtils.getValidIngredientsForBench(ref, ref.getStore(), "MortarAndPestle");
    var itemsInStorage =
        AlchemyBenchUtils.getItemsInStorageNetwork(store.getExternalData().getWorld(), position);

    return new BenchMortarPage(
        playerRef,
        blockStateInfo,
        mortarAndPestle,
        position,
        validIngredients,
        itemsInStorage,
        BaselineInteractions,
        InteractionsPerIngredient);
  }
}
