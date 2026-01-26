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

package org.echoesfrombeyond.command;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Command to run integration tests. Usually this is not registered at all, unless the environment
 * variable {@code ENABLE_INTEGRATION_TESTS} is set to {@code true}.
 */
@NullMarked
public class IntegrationTestCommand extends AbstractCommand {
  private static final IntegrationTestCommand INSTANCE = new IntegrationTestCommand();

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  private IntegrationTestCommand() {
    super("integration_test", "When invoked by the server console, runs integration tests.");
  }

  /**
   * Registers the integration test command, if the appropriate environment variable {@code
   * ENABLE_INTEGRATION_TESTS} is set to {@code true}.
   *
   * @param plugin the launch plugin
   */
  @ApiStatus.Internal
  public static void register(JavaPlugin plugin) {
    var integrationTests = System.getenv("ENABLE_INTEGRATION_TESTS");

    if (integrationTests != null && integrationTests.equalsIgnoreCase("true")) {
      plugin.getCommandRegistry().registerCommand(IntegrationTestCommand.INSTANCE);
    }
  }

  @Override
  protected @Nullable CompletableFuture<Void> execute(CommandContext commandContext) {
    if (!(commandContext.sender() instanceof ConsoleSender)) return null;

    LOGGER.atInfo().log("Running tests.");
    testEntrypoint(commandContext);
    LOGGER.atInfo().log("Tests completed successfully!");

    return null;
  }

  private static void testEntrypoint(CommandContext ignored) {
    // There's nothing to test, yet.
  }
}
