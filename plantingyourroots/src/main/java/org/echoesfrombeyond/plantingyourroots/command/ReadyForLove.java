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

package org.echoesfrombeyond.plantingyourroots.command;

import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import org.echoesfrombeyond.plantingyourroots.PlantingYourRoots;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ReadyForLove extends CommandBase {
  public ReadyForLove() {
    super("readyforlove", "Brings you to your new home in Kweebdrasil...");
  }

  @Override
  protected void executeSync(CommandContext commandContext) {
    if (!commandContext.isPlayer()) return;

    var playerRef = commandContext.senderAsPlayerRef();
    if (playerRef == null) return;

    var store = playerRef.getStore();
    var world = store.getExternalData().getWorld();
    var roots = PlantingYourRoots.get();

    if (roots.isKweebdrasilInstance(world)) return;

    world.execute(
        () -> {
          var transform = store.getComponent(playerRef, TransformComponent.getComponentType());
          if (transform == null) return;

          var returnPoint = transform.getTransform().clone();
          InstancesPlugin.teleportPlayerToLoadingInstance(
              playerRef, store, PlantingYourRoots.get().getKweebdrasil(), returnPoint);
        });
  }
}
