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

package org.echoesfrombeyond.interaction;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class PlayerInstantInteraction extends SimpleInstantInteraction {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  @Override
  protected final void firstRun(
      InteractionType interactionType,
      InteractionContext interactionContext,
      CooldownHandler cooldownHandler) {
    var buffer = InteractionUtils.getBuffer(interactionContext);
    if (buffer == null) {
      LOGGER.atWarning().log("Null command buffer");
      interactionContext.getState().state = InteractionState.Failed;
      return;
    }

    var ref = interactionContext.getEntity();
    var player = buffer.getComponent(ref, Player.getComponentType());
    var playerRef = buffer.getComponent(ref, PlayerRef.getComponentType());

    if (player == null || playerRef == null) {
      interactionContext.getState().state = InteractionState.Failed;
      return;
    }

    firstRunPlayer(interactionType, interactionContext, cooldownHandler, buffer, player, playerRef);
  }

  protected abstract void firstRunPlayer(
      InteractionType interactionType,
      InteractionContext interactionContext,
      CooldownHandler cooldownHandler,
      CommandBuffer<EntityStore> buffer,
      Player player,
      PlayerRef playerRef);
}
