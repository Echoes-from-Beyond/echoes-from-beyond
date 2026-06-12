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
import java.util.ArrayList;
import java.util.List;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.*;
import org.echoesfrombeyond.codechelper.annotation.validator.ValidateLengthRange;
import org.echoesfrombeyond.codechelper.internaldep.org.echoesfrombeyond.util.Check;
import org.echoesfrombeyond.echoesfrombeyond.enums.MathFormulaType;
import org.echoesfrombeyond.modutil.asset.IdentifiedAsset;
import org.echoesfrombeyond.util.thread.Once;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@ModelBuilder
public class AlchemyReactionList
    implements IdentifiedAsset<String>,
        JsonAssetWithMap<String, DefaultAssetMap<String, AlchemyReactionList>> {

  @Doc(
      """
      Represents an alchemical reaction.
      """)
  @NullMarked
  @ModelBuilder
  public static class Reaction {

    @Doc(
        """
        Associates an alchemical component with a quantity of units to use/produce as part of a reaction.
        """)
    @NullMarked
    @ModelBuilder
    public static class ComponentIngredient {
      @Doc(
          """
          The identifier of the alchemical component.
          """)
      public String ComponentId;

      @Doc(
          """
          How many units of the above component is used for/produced in this step.
          """)
      public int Quantity;

      public ComponentIngredient() {
        ComponentId = "";
        Quantity = 1;
      }
    }

    @Doc(
        """
        The ingredients *used* (i.e. the left-hand side) as part of the reaction. There should always be at least one reactant.
        """)
    @ValidateLengthRange(min = 1, max = Integer.MAX_VALUE)
    public List<ComponentIngredient> Reactants;

    @Doc(
        """
        The ingredients *produced* (i.e. the right-hand side) as part of the reaction.
        Leave blank if you want the reaction to be entirely wasteful.
        """)
    @Opt
    public List<ComponentIngredient> Products;

    @Doc(
        """
        How to treat reaction rates with respect to temperature excesses and deficiencies.
        CONSTANT means that the rate is the same no matter the temperature; LINEAR multiplies the rate by the difference between
        the current temperature and the boundary; EXPONENTIAL raises the rate to the power of the difference. In all cases,
        reactions only care about the *absolute* value of the difference.
        Defaults to LINEAR.
        """)
    @Opt
    public MathFormulaType ReactionRateMath;

    @Doc(
        """
        At what temperature the reaction starts taking place. Measured in degrees Celsius.
        Defaults to 100.0.
        """)
    @Opt
    public double RequisiteTemperature;

    @Doc(
        """
        Determines whether the reaction cares about addition of heat, or lack thereof.
        In other words, if this is set to true, the reaction checks that the current temperature is *higher* than RequisiteTemperature;
        if set to false, the reaction checks that it's *below* RequisiteTemperature.
        Defaults to true.
        """)
    @Opt
    public boolean PreferHigherTemperatures;

    public Reaction() {
      Reactants = new ArrayList<>();
      Products = new ArrayList<>();
      ReactionRateMath = MathFormulaType.Linear;
      RequisiteTemperature = 100.0;
      PreferHigherTemperatures = true;
    }
  }

  @Doc(
      """
      A list of lists of alchemical reactions.
      The index of the outermost list determines the priority of inner lists of reactions; lower index = higher priority.
      Reactions within the same inner list have the same priority, and are chosen randomly during each calculation.
      """)
  @ValidateLengthRange(min = 1, max = Integer.MAX_VALUE)
  public List<List<Reaction>> Reactions;

  public AlchemyReactionList() {
    Reactions = new ArrayList<>();
  }

  public static final AssetBuilderCodec<String, AlchemyReactionList> CODEC =
      CodecUtil.modelAssetBuilder(
          AlchemyReactionList.class, Plugin.getSharedResolver(), Plugin.getSharedCache());

  @Id private @Nullable String Id;

  @Data private AssetExtraInfo.@Nullable Data Data;

  @ApiStatus.Internal
  static class Internal {
    private static final Once<
            AssetStore<String, AlchemyReactionList, DefaultAssetMap<String, AlchemyReactionList>>>
        ASSET_STORE =
            Once.of(() -> Check.nonNull(AssetRegistry.getAssetStore(AlchemyReactionList.class)));
  }

  public static @Nullable AlchemyReactionList getAlchemyReactionList(String asset) {
    return AlchemyReactionList.Internal.ASSET_STORE.get().getAssetMap().getAsset(asset);
  }

  public static AssetStore<
          String, AlchemyReactionList, DefaultAssetMap<String, AlchemyReactionList>>
      getAssetStore() {
    return AlchemyReactionList.Internal.ASSET_STORE.get();
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
