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

package org.echoesfrombeyond;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Main entrypoint of the mod. All initialization happens here.
 *
 * <p>Hytale instantiates this via reflection, so there are not necessarily any direct references
 * here.
 */
@SuppressWarnings("unused")
public class Plugin extends JavaPlugin {
  /**
   * First entrypoint. Actual initialization tasks should probably go in the various load methods.
   *
   * @param init the initialization argument
   */
  public Plugin(JavaPluginInit init) {
    super(init);
  }

  @Override
  public @Nullable CompletableFuture<Void> preLoad() {
    System.out.println("Hello Orbis (preload)");

    // This loads all the plugin configs. So plugins must always call this unless they don't need to
    // bother with configuration.
    return super.preLoad();
  }

  @Override
  protected void setup() {
    System.out.println("Hello Orbis (setup)");

    getCommandRegistry()
        .registerCommand(
            new AbstractCommand("succ", null) {
              @Override
              protected @Nullable CompletableFuture<Void> execute(
                  @NotNull CommandContext commandContext) {
                if (commandContext.isPlayer()) {
                  Player player = commandContext.senderAs(Player.class);
                  player.sendMessage(Message.raw("how many layers of irony are you on?"));
                }

                return null;
              }

              @Override
              public boolean hasPermission(@NotNull CommandSender sender) {
                return true;
              }
            });

    // This is a no-op currently, but because this has an actual implementation something may be
    // done in the future, so it should always be called.
    super.setup();
  }

  @Override
  protected void start() {
    System.out.println("Hello Orbis (start)");

    // Also a no-op.
    super.start();
  }
}
