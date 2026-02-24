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

package org.echoesfrombeyond.echoesfrombeyond.interaction;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.function.consumer.QuadConsumer;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NullMarked;

/** Utilities for dealing with {@link Interaction}s. */
@NullMarked
public final class InteractionUtils {
  private InteractionUtils() {}

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  /**
   * Execute a consumer with the {@link CommandBuffer}, {@link Ref}, {@link Player}, and {@link
   * PlayerRef} if the interaction represents a player. Otherwise, {@code consumer} will not be
   * executed at all.
   *
   * @param context the context
   * @param callback the callback consumer
   */
  public static void forPlayerInStore(
      InteractionContext context,
      QuadConsumer<
              ? super CommandBuffer<EntityStore>,
              ? super Ref<EntityStore>,
              ? super Player,
              ? super PlayerRef>
          callback) {
    // Sanity check: if this getter returns `null`, getCommandBuffer would throw an NPE.
    if (context.getInteractionManager() == null) {
      context.getState().state = InteractionState.Failed;
      return;
    }

    var buffer = context.getCommandBuffer();
    if (buffer == null) {
      LOGGER.atFine().log("CommandBuffer#getCommandBuffer returned null");
      context.getState().state = InteractionState.Failed;
      return;
    }

    var ref = context.getEntity();

    var player = buffer.getComponent(ref, Player.getComponentType());
    var playerRef = buffer.getComponent(ref, PlayerRef.getComponentType());

    if (player == null || playerRef == null) {
      if (player == null) LOGGER.atFine().log(ref + " missing Player component");
      if (playerRef == null) LOGGER.atFine().log(ref + " missing PlayerRef component");

      context.getState().state = InteractionState.Failed;
      return;
    }

    callback.accept(buffer, ref, player, playerRef);
  }
}
