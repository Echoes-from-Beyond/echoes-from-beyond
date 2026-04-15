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

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.plantingyourroots.PlantingYourRoots;
import org.echoesfrombeyond.plantingyourroots.component.RootsComponent;
import org.jspecify.annotations.NullMarked;

@ModelBuilder
@NullMarked
public class AdvanceDayInteraction extends SimpleInstantInteraction {
  public static final BuilderCodec<AdvanceDayInteraction> CODEC =
      CodecUtil.modelBuilder(
          AdvanceDayInteraction.class, Plugin.getSharedResolver(), Plugin.getSharedCache());

  @Override
  protected void firstRun(
      InteractionType interactionType,
      InteractionContext interactionContext,
      CooldownHandler cooldownHandler) {
    var buffer = interactionContext.getCommandBuffer();
    if (buffer == null) return;

    var roots = PlantingYourRoots.get();
    if (!roots.isKweebdrasilInstance(buffer.getStore().getExternalData().getWorld())) return;

    roots.advanceDay(
        buffer,
        buffer.ensureAndGetComponent(
            interactionContext.getEntity(), RootsComponent.getComponentType()));
  }
}
