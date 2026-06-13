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
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.*;
import org.echoesfrombeyond.codechelper.internaldep.org.echoesfrombeyond.util.Check;
import org.echoesfrombeyond.echoesfrombeyond.enums.MathFormulaType;
import org.echoesfrombeyond.modutil.asset.IdentifiedAsset;
import org.echoesfrombeyond.util.thread.Once;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@ModelBuilder
public class AlchemyComponent
    implements IdentifiedAsset<String>,
        JsonAssetWithMap<String, DefaultAssetMap<String, AlchemyComponent>> {
  public static final AssetBuilderCodec<String, AlchemyComponent> CODEC =
      CodecUtil.modelAssetBuilder(
          AlchemyComponent.class, Plugin.getSharedResolver(), Plugin.getSharedCache());

  @Id private @Nullable String Id;

  @Data private AssetExtraInfo.@Nullable Data Data;

  @Doc(
      """
      A name, instead of the component identifier, that will be shown to the player.
      """)
  public @Nullable String DisplayName;

  @Doc(
      """
      How to treat denaturation rates with respect to temperature excesses (for upper) and deficiencies (for lower).
      CONSTANT means that the rate is the same no matter the temperature; LINEAR multiplies the rate by the difference between
      the current temperature and the boundary; EXPONENTIAL raises the rate to the power of the difference. In all cases,
      denaturation only cares about the *absolute* value of the difference.
      Defaults to LINEAR.
      """)
  @Opt
  public MathFormulaType DenaturationRateMath;

  @Doc(
      """
      The upper temperature at which a component begins to denature. Measured in degrees Celsius.
      Defaults to 5500.0 degrees Celsius, which is unreachable in the current version of the plugin.
      If you set it to a more reasonable value, you should also assign a value to the denaturationRateUpper field.
      """)
  @Opt
  public double DenaturationTempUpper;

  @Doc(
      """
      The rate of denaturation once the temperature of a mixture reaches the upper range. The arithmetic depends on DenaturationRateMath.
      Defaults to 0.0.
      """)
  @Opt
  public double DenaturationRateUpper;

  @Doc(
      """
      Identifier of the component to denature into once the temperature exceeds the upper boundary, according to the configured rate.
      Leave blank if you want the component to "waste away".
      """)
  @Opt
  public @Nullable String DenatureIntoUpper;

  @Doc(
      """
      The lower temperature at which a component begins to denature. Measured in degrees Celsius.
      Defaults to -273.15 degrees Celsius, which is unreachable in the current version of the plugin.
      If you set it to a more reasonable value, you should also assign a value to the denaturationRateLower field.
      """)
  @Opt
  public double DenaturationTempLower;

  @Doc(
      """
      The rate of denaturation once the temperature of a mixture reaches the lower range. The arithmetic depends on DenaturationRateMath.
      Defaults to 0.0.
      """)
  @Opt
  public double DenaturationRateLower;

  @Doc(
      """
      Identifier of the component to denature into once the temperature is below the lower boundary, according to the configured rate.
      Leave blank if you want the component to "waste away".
      """)
  @Opt
  public @Nullable String DenatureIntoLower;

  @Doc(
      """
      Arbitrary weight applied to each unit of this component, only used to split off mixtures into
      different intermediary ingredients. Each intermediary ingredient can only hold a finite amount of weight.
      Defaults to 0.1.
      """)
  @Opt
  public double WeightPerUnit;

  public AlchemyComponent() {
    DenaturationRateMath = MathFormulaType.Linear;
    DenaturationTempUpper = 5500.0;
    DenaturationRateUpper = 0.0;
    DenaturationTempLower = -273.15;
    DenaturationRateLower = 0.0;
    WeightPerUnit = 0.1;
  }

  @ApiStatus.Internal
  static class Internal {
    private static final Once<
            AssetStore<String, AlchemyComponent, DefaultAssetMap<String, AlchemyComponent>>>
        ASSET_STORE =
            Once.of(() -> Check.nonNull(AssetRegistry.getAssetStore(AlchemyComponent.class)));
  }

  public static @Nullable AlchemyComponent getAlchemyComponent(String asset) {
    return Internal.ASSET_STORE.get().getAssetMap().getAsset(asset);
  }

  public static AssetStore<String, AlchemyComponent, DefaultAssetMap<String, AlchemyComponent>>
      getAssetStore() {
    return Internal.ASSET_STORE.get();
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
