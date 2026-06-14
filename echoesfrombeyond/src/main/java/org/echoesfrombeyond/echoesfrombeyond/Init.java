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

import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.system.System;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.PluginState;
import org.echoesfrombeyond.echoesfrombeyond.asset.AlchemyComponent;
import org.echoesfrombeyond.echoesfrombeyond.asset.AlchemyItem;
import org.echoesfrombeyond.echoesfrombeyond.asset.AlchemyReactionList;
import org.echoesfrombeyond.echoesfrombeyond.asset.SigilPattern;
import org.echoesfrombeyond.echoesfrombeyond.command.IntegrationTestCommand;
import org.echoesfrombeyond.echoesfrombeyond.component.chunk.AlchemyBench;
import org.echoesfrombeyond.echoesfrombeyond.component.chunk.MortarAndPestle;
import org.echoesfrombeyond.echoesfrombeyond.component.entity.SigilDrawComponent;
import org.echoesfrombeyond.echoesfrombeyond.interaction.HeldItemConditionInteraction;
import org.echoesfrombeyond.echoesfrombeyond.interaction.sigil.BeginSigilDraw;
import org.echoesfrombeyond.echoesfrombeyond.interaction.sigil.CloseSigilHud;
import org.echoesfrombeyond.echoesfrombeyond.interaction.sigil.EndSigilDraw;
import org.echoesfrombeyond.echoesfrombeyond.interaction.sigil.OpenSigilHud;
import org.echoesfrombeyond.echoesfrombeyond.system.chunk.AlchemyBenchLifecycleSystem;
import org.echoesfrombeyond.echoesfrombeyond.system.chunk.RemoveAlchemyNetworkSystem;
import org.echoesfrombeyond.echoesfrombeyond.system.entity.SigilDrawSystem;
import org.echoesfrombeyond.echoesfrombeyond.ui.BenchMortarSupplier;
import org.echoesfrombeyond.util.Check;
import org.jspecify.annotations.NullMarked;

/**
 * Initialization/registration actions. Only used internally to avoid cluttering {@link
 * EchoesFromBeyond}.
 */
@NullMarked
public class Init {
  private Init() {}

  /**
   * Registers custom {@link AssetStore}s.
   *
   * @param plugin the plugin
   * @throws IllegalArgumentException if {@code plugin} is not in the {@link PluginState#SETUP}
   *     state
   */
  static void registerAssetRegistries(JavaPlugin plugin) {
    Check.equals(plugin.getState(), PluginState.SETUP);

    // Order registrations alphabetically by value passed to `setPath`.
    plugin
        .getAssetRegistry()
        .register(
            HytaleAssetStore.builder(AlchemyComponent.class, new DefaultAssetMap<>())
                .setCodec(AlchemyComponent.CODEC)
                .setPath("Alchemy/Components")
                .setKeyFunction(AlchemyComponent::getId)
                .build());

    plugin
        .getAssetRegistry()
        .register(
            HytaleAssetStore.builder(AlchemyItem.class, new DefaultAssetMap<>())
                .setCodec(AlchemyItem.CODEC)
                .setPath("Alchemy/Items")
                .setKeyFunction(AlchemyItem::getId)
                .build());

    plugin
        .getAssetRegistry()
        .register(
            HytaleAssetStore.builder(AlchemyReactionList.class, new DefaultAssetMap<>())
                .setCodec(AlchemyReactionList.CODEC)
                .setPath("Alchemy/Reactions")
                .setKeyFunction(AlchemyReactionList::getId)
                .build());

    plugin
        .getAssetRegistry()
        .register(
            HytaleAssetStore.builder(SigilPattern.class, new DefaultAssetMap<>())
                .setCodec(SigilPattern.CODEC)
                .setPath("SigilPatterns")
                .setKeyFunction(SigilPattern::getId)
                .build());
  }

  /**
   * Registers custom {@link Codec}s.
   *
   * @param plugin the plugin
   * @throws IllegalArgumentException if {@code plugin} is not in the {@link PluginState#SETUP}
   *     state
   */
  static void registerCodecs(JavaPlugin plugin) {
    Check.equals(plugin.getState(), PluginState.SETUP);

    // Order registrations alphabetically by `id` parameter.
    plugin
        .getCodecRegistry(Interaction.CODEC)
        .register("Begin_Sigil_Draw", BeginSigilDraw.class, BeginSigilDraw.CODEC)
        .register("Close_Sigil_Hud", CloseSigilHud.class, CloseSigilHud.CODEC)
        .register("End_Sigil_Draw", EndSigilDraw.class, EndSigilDraw.CODEC)
        .register(
            "HeldItemCondition",
            HeldItemConditionInteraction.class,
            HeldItemConditionInteraction.CODEC)
        .register("Open_Sigil_Hud", OpenSigilHud.class, OpenSigilHud.CODEC);

    plugin
        .getCodecRegistry(OpenCustomUIInteraction.PAGE_CODEC)
        .register("BenchMortar", BenchMortarSupplier.class, BenchMortarSupplier.CODEC);
  }

  /**
   * Registers custom {@link AbstractCommand}s.
   *
   * @param plugin the plugin
   * @throws IllegalArgumentException if {@code plugin} is not in the {@link PluginState#SETUP}
   *     state
   */
  static void registerCommands(JavaPlugin plugin) {
    Check.equals(plugin.getState(), PluginState.SETUP);

    // Order registrations alphabetically by class name.
    IntegrationTestCommand.register(plugin);
  }

  /**
   * Registers custom {@link Component}s.
   *
   * @param plugin the plugin
   * @throws IllegalArgumentException if {@code plugin} is not in the {@link PluginState#SETUP}
   *     state
   */
  static void registerComponents(JavaPlugin plugin) {
    Check.equals(plugin.getState(), PluginState.SETUP);

    registerChunkStoreComponents(plugin);
    registerEntityStoreComponents(plugin);
  }

  private static void registerChunkStoreComponents(JavaPlugin plugin) {
    var chunkProxy = plugin.getChunkStoreRegistry();

    // Order alphabetically by class name
    AlchemyBench.registerComponentType(chunkProxy);
    MortarAndPestle.registerComponentType(chunkProxy);
  }

  private static void registerEntityStoreComponents(JavaPlugin plugin) {
    var entityProxy = plugin.getEntityStoreRegistry();

    // Order alphabetically by class name
    SigilDrawComponent.registerComponentType(entityProxy);
  }

  /**
   * Registers custom {@link System}s.
   *
   * @param plugin the plugin
   * @throws IllegalArgumentException if {@code plugin} is not in the {@link PluginState#SETUP}
   *     state
   */
  static void registerSystems(JavaPlugin plugin) {
    Check.equals(plugin.getState(), PluginState.SETUP);

    registerChunkSystems(plugin);
    registerEntitySystems(plugin);
  }

  private static void registerChunkSystems(JavaPlugin plugin) {
    var chunkStoreRegistry = plugin.getChunkStoreRegistry();

    // Order alphabetically by class name
    chunkStoreRegistry.registerSystem(new AlchemyBenchLifecycleSystem());
    chunkStoreRegistry.registerSystem(new RemoveAlchemyNetworkSystem());
  }

  private static void registerEntitySystems(JavaPlugin plugin) {
    var entityStoreRegistry = plugin.getEntityStoreRegistry();

    // Order alphabetically by class name
    entityStoreRegistry.registerSystem(new SigilDrawSystem());
  }
}
