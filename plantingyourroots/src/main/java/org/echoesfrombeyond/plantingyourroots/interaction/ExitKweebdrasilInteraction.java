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

package org.echoesfrombeyond.plantingyourroots.interaction;

import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.plantingyourroots.PlantingYourRoots;
import org.jspecify.annotations.NullMarked;

@ModelBuilder
@NullMarked
public class ExitKweebdrasilInteraction extends SimpleInstantInteraction {
  public static final BuilderCodec<ExitKweebdrasilInteraction> CODEC =
      CodecUtil.modelBuilder(
          ExitKweebdrasilInteraction.class, Plugin.getSharedResolver(), Plugin.getSharedCache());

  @Override
  protected void firstRun(
      InteractionType interactionType,
      InteractionContext interactionContext,
      CooldownHandler cooldownHandler) {
    var buffer = interactionContext.getCommandBuffer();
    if (buffer == null) return;

    var plugin = PlantingYourRoots.get();
    var world = buffer.getStore().getExternalData().getWorld();
    var ref = interactionContext.getEntity();
    if (!plugin.isKweebdrasilInstance(world)) {
      var player = buffer.getComponent(ref, Player.getComponentType());
      if (player != null)
        player.sendMessage(Message.raw("This item cannot be used outside of Kweebdrasil!"));
      return;
    }

    InstancesPlugin.exitInstance(ref, buffer);
  }
}
