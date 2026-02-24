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
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.system.System;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.PluginState;
import org.echoesfrombeyond.asset.SigilAssetMap;
import org.echoesfrombeyond.asset.SigilPattern;
import org.echoesfrombeyond.command.IntegrationTestCommand;
import org.echoesfrombeyond.component.sigil.SigilDrawComponent;
import org.echoesfrombeyond.component.sigil.SigilQueueComponent;
import org.echoesfrombeyond.interaction.sigil.BeginSigilDraw;
import org.echoesfrombeyond.interaction.sigil.CloseSigilHud;
import org.echoesfrombeyond.interaction.sigil.EndSigilDraw;
import org.echoesfrombeyond.interaction.sigil.OpenSigilHud;
import org.echoesfrombeyond.system.sigil.SigilDrawSystem;
import org.echoesfrombeyond.system.sigil.SigilQueueSystem;
import org.echoesfrombeyond.system.sigil.SigilValidateSystem;
import org.echoesfrombeyond.util.Check;
import org.jspecify.annotations.NullMarked;

/** Initialization/registration actions. Only used internally to avoid cluttering {@link Plugin}. */
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
            HytaleAssetStore.builder(SigilPattern.class, new SigilAssetMap())
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
        .register("Begin_Sigil_Draw", BeginSigilDraw.class, BeginSigilDraw.CODEC);

    plugin
        .getCodecRegistry(Interaction.CODEC)
        .register("Close_Sigil_Hud", CloseSigilHud.class, CloseSigilHud.CODEC);

    plugin
        .getCodecRegistry(Interaction.CODEC)
        .register("End_Sigil_Draw", EndSigilDraw.class, EndSigilDraw.CODEC);

    plugin
        .getCodecRegistry(Interaction.CODEC)
        .register("Open_Sigil_Hud", OpenSigilHud.class, OpenSigilHud.CODEC);
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

    var registry = plugin.getEntityStoreRegistry();

    // Order registrations alphabetically by class name.
    SigilDrawComponent.registerComponentType(registry);
    SigilQueueComponent.registerComponentType(registry);
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

    var registry = plugin.getEntityStoreRegistry();

    // Order registrations alphabetically by class name.
    registry.registerSystem(new SigilDrawSystem());
    registry.registerSystem(new SigilQueueSystem());
    registry.registerSystem(new SigilValidateSystem());
  }
}
