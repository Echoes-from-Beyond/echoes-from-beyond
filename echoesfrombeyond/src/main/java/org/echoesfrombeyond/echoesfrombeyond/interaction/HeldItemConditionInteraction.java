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

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.Doc;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.codechelper.annotation.Opt;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@Doc("Fails the interaction if the held item isn't the correct ID.")
@NullMarked
@ModelBuilder
public class HeldItemConditionInteraction extends SimpleInstantInteraction {
  public static final BuilderCodec<HeldItemConditionInteraction> CODEC =
      CodecUtil.modelBuilder(
          HeldItemConditionInteraction.class,
          SimpleInstantInteraction.CODEC,
          Plugin.getSharedResolver());

  @Opt
  @Doc("The item ID to check against. If left unspecified, checks if there is no held item.")
  public @Nullable String ItemId;

  @Override
  protected void firstRun(
      InteractionType type, InteractionContext context, CooldownHandler ignored) {
    if (!this.matches(context)) context.getState().state = InteractionState.Failed;
  }

  protected boolean matches(InteractionContext context) {
    var item = context.getHeldItem();
    var id = ItemId;

    if (item == null) return id == null;
    return item.getItemId().equalsIgnoreCase(id);
  }
}
