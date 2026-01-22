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

import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.echoesfrombeyond.asset.SigilPattern;
import org.echoesfrombeyond.command.IntegrationTestCommand;
import org.echoesfrombeyond.interaction.DrawSigilInteraction;
import org.echoesfrombeyond.ui.MemeUIPage;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Main entrypoint of the mod. All initialization happens here.
 *
 * <p>Hytale instantiates this via reflection, so there are not necessarily any direct references
 * here.
 */
@SuppressWarnings("unused")
@NullMarked
public class Plugin extends JavaPlugin {
  /**
   * First entrypoint. Actual initialization tasks should probably go in the various load methods.
   *
   * @param init the initialization argument
   */
  public Plugin(JavaPluginInit init) {
    super(init);
  }

  /**
   * Generic configuration happens here. May be done in parallel using {@link CompletableFuture}s.
   *
   * @return a future representing our plugin's pre-load phase
   */
  @Override
  public @Nullable CompletableFuture<Void> preLoad() {
    // This loads all the plugin configs. So plugins must always call this unless they don't need to
    // bother with configuration.
    return super.preLoad();
  }

  /** Setup. Most asset registration happens here. */
  @Override
  protected void setup() {
    getAssetRegistry()
        .register(
            HytaleAssetStore.builder(SigilPattern.class, new DefaultAssetMap<>())
                .setCodec(SigilPattern.CODEC)
                .setExtension(".json")
                .setPath("SigilPatterns")
                .setKeyFunction(SigilPattern::getId)
                .build());

    String integrationTests = System.getenv("ENABLE_INTEGRATION_TESTS");

    if (integrationTests != null && integrationTests.equalsIgnoreCase("true")) {
      getCommandRegistry().registerCommand(IntegrationTestCommand.INSTANCE);
    }

    getCodecRegistry(Interaction.CODEC)
        .register("Draw_Sigil", DrawSigilInteraction.class, DrawSigilInteraction.CODEC);

    getEventRegistry()
        .registerGlobal(
            PlayerChatEvent.class,
            (evt) -> {
              PlayerRef playerRef = evt.getSender();
              UUID worldUUID = playerRef.getWorldUuid();
              if (worldUUID == null) return;

              if (!evt.getContent().toLowerCase(Locale.ROOT).contains("vegetal")) return;

              World world = Universe.get().getWorld(worldUUID);
              if (world == null) return;

              world.execute(
                  () -> {
                    var ref = playerRef.getReference();
                    if (ref == null) return;

                    Store<EntityStore> store = world.getEntityStore().getStore();

                    Player player = store.getComponent(ref, Player.getComponentType());
                    if (player == null) return;

                    player
                        .getPageManager()
                        .openCustomPage(ref, store, new MemeUIPage(evt.getSender()));
                  });
            });

    // This is a no-op currently, but because this has an actual implementation something may be
    // done in the future, so it should always be called.
    super.setup();
  }

  @Override
  protected void start() {
    // Also a no-op.
    super.start();
  }
}
