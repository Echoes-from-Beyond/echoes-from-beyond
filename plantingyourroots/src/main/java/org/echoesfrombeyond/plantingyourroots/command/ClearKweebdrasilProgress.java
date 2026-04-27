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

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import org.echoesfrombeyond.dialoguelib.component.DialogueComponent;
import org.echoesfrombeyond.plantingyourroots.PlantingYourRoots;
import org.echoesfrombeyond.plantingyourroots.component.RootsComponent;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ClearKweebdrasilProgress extends CommandBase {
  public ClearKweebdrasilProgress() {
    super("clearkweebdrasilprogress", "Resets your adventure in Kweebdrasil.");
  }

  @Override
  protected void executeSync(CommandContext commandContext) {
    if (!commandContext.isPlayer()) return;

    var playerRef = commandContext.senderAsPlayerRef();
    if (playerRef == null) return;

    var store = playerRef.getStore();
    var world = store.getExternalData().getWorld();
    var plugin = PlantingYourRoots.get();

    if (plugin.isKweebdrasilInstance(world)) {
      commandContext.sendMessage(Message.parse("Leave Kweebdrasil before running this command!"));
      return;
    }

    world.execute(
        () -> {
          store.tryRemoveComponent(playerRef, RootsComponent.getComponentType());
          store.tryRemoveComponent(playerRef, DialogueComponent.getComponentType());
        });
  }
}
