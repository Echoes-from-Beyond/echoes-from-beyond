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
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.echoesfrombeyond.component.sigil.SigilDrawComponent;
import org.echoesfrombeyond.interaction.PlayerInstantInteraction;
import org.echoesfrombeyond.system.sigil.SigilValidateSystem;
import org.echoesfrombeyond.ui.hud.HudUtils;
import org.echoesfrombeyond.ui.hud.SigilHud;
import org.jspecify.annotations.NullMarked;

/**
 * {@link Interaction} to finish drawing a Sigil. This will have no effect if the Sigil UI is not
 * open yet, or if drawing has not started.
 */
@NullMarked
public class EndSigilDraw extends PlayerInstantInteraction {
  /** The codec. */
  public static final BuilderCodec<EndSigilDraw> CODEC =
      BuilderCodec.builder(EndSigilDraw.class, EndSigilDraw::new, SimpleInstantInteraction.CODEC)
          .build();

  @Override
  protected void firstRunPlayer(
      InteractionType interactionType,
      InteractionContext interactionContext,
      CooldownHandler cooldownHandler,
      CommandBuffer<EntityStore> buffer,
      Player player,
      PlayerRef playerRef) {
    var ref = interactionContext.getEntity();
    var sigilDraw = buffer.getComponent(ref, SigilDrawComponent.getComponentType());

    if (sigilDraw == null || !sigilDraw.drawing || !sigilDraw.open) return;

    var hud = HudUtils.getHud(SigilHud.class, player.getHudManager());
    if (sigilDraw.points.size() > 1 && hud != null) {
      UICommandBuilder builder = new UICommandBuilder();
      hud.unsetLines(builder, sigilDraw.points);
      hud.update(false, builder);
    }

    buffer.invoke(ref, new SigilValidateSystem.Event(sigilDraw.points));

    sigilDraw.points.clear();
    sigilDraw.drawing = false;
  }
}
