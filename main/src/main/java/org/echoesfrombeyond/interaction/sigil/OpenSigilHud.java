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

package org.echoesfrombeyond.interaction.sigil;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.echoesfrombeyond.codec.SigilPoint;
import org.echoesfrombeyond.component.sigil.SigilDrawComponent;
import org.echoesfrombeyond.interaction.InteractionUtils;
import org.echoesfrombeyond.ui.hud.SigilHud;
import org.jspecify.annotations.NullMarked;

/** Opens the Sigil HUD. */
@NullMarked
public class OpenSigilHud extends SimpleInstantInteraction {
  /** The codec. */
  public static final BuilderCodec<OpenSigilHud> CODEC =
      BuilderCodec.builder(OpenSigilHud.class, OpenSigilHud::new, SimpleInstantInteraction.CODEC)
          .build();

  @Override
  protected void firstRun(
      InteractionType interactionType,
      InteractionContext interactionContext,
      CooldownHandler cooldownHandler) {
    InteractionUtils.forPlayerInStore(interactionContext, OpenSigilHud::run);
  }

  private static void run(
      CommandBuffer<EntityStore> buffer, Ref<EntityStore> ref, Player player, PlayerRef playerRef) {
    var head = buffer.getComponent(ref, HeadRotation.getComponentType());
    if (head == null) return;

    var sigilDraw = buffer.ensureAndGetComponent(ref, SigilDrawComponent.getComponentType());
    var wasNotOpen = !sigilDraw.open;

    if (wasNotOpen) {
      sigilDraw.open = true;
      sigilDraw.initialRotation = head.getRotation().clone();
    }

    var hudManager = player.getHudManager();

    SigilHud hud;
    if (hudManager.getCustomHud() instanceof SigilHud sigilHud) hud = sigilHud;
    else {
      hud = new SigilHud(playerRef);
      hudManager.setCustomHud(playerRef, hud);
    }

    if (wasNotOpen) {
      var builder = new UICommandBuilder();
      hud.highlight(builder, SigilPoint.ZERO, true);
      hud.update(false, builder);
    }
  }
}
