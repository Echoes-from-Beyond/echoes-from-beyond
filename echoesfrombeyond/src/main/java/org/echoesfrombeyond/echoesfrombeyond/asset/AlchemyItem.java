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

package org.echoesfrombeyond.echoesfrombeyond.asset;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import java.util.List;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.Data;
import org.echoesfrombeyond.codechelper.annotation.Doc;
import org.echoesfrombeyond.codechelper.annotation.Id;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.codechelper.internaldep.org.echoesfrombeyond.util.Check;
import org.echoesfrombeyond.echoesfrombeyond.asset.other.ComponentIngredient;
import org.echoesfrombeyond.modutil.asset.IdentifiedAsset;
import org.echoesfrombeyond.util.thread.Once;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@Doc(
    """
    Associates items with alchemical components. The name of the file must correspond to an existing
    Item.
    """)
@NullMarked
@ModelBuilder
public class AlchemyItem
    implements IdentifiedAsset<String>,
        JsonAssetWithMap<String, DefaultAssetMap<String, AlchemyItem>> {
  // TODO: validation for asset names?

  public static final AssetBuilderCodec<String, AlchemyItem> CODEC =
      CodecUtil.modelAssetBuilder(
          AlchemyItem.class, Plugin.getSharedResolver(), Plugin.getSharedCache());

  @Id private @Nullable String Id;

  @Data private AssetExtraInfo.@Nullable Data Data;

  @Doc(
      """
      A list of component-quantity pairs. All instances of this item have the same components.
      """)
  public List<ComponentIngredient> Components;

  @ApiStatus.Internal
  static class Internal {
    private static final Once<AssetStore<String, AlchemyItem, DefaultAssetMap<String, AlchemyItem>>>
        ASSET_STORE = Once.of(() -> Check.nonNull(AssetRegistry.getAssetStore(AlchemyItem.class)));
  }

  public static @Nullable AlchemyItem getAlchemyItem(String asset) {
    return AlchemyItem.Internal.ASSET_STORE.get().getAssetMap().getAsset(asset);
  }

  public static AssetStore<String, AlchemyItem, DefaultAssetMap<String, AlchemyItem>>
      getAssetStore() {
    return AlchemyItem.Internal.ASSET_STORE.get();
  }

  @Override
  public void setId(String id) {
    Id = id;
  }

  @Override
  public void setData(AssetExtraInfo.@Nullable Data data) {
    Data = data;
  }

  @Override
  public AssetExtraInfo.@Nullable Data getData() {
    return Data;
  }

  @Override
  public @Nullable String getId() {
    return Id;
  }
}
