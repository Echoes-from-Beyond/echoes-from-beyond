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

package org.echoesfrombeyond.plantingyourroots;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;


@SuppressWarnings("unused")
@NullMarked
public class PlantingYourRoots extends JavaPlugin {

  public PlantingYourRoots(JavaPluginInit init) {
    super(init);
  }

  /**
   * Generic configuration happens here. May be done in parallel using {@link CompletableFuture}s.
   *
   * @return a future representing our plugin's pre-load phase
   */
  @Override
  public @Nullable CompletableFuture<Void> preLoad() {
    return super.preLoad();
  }

  /** Setup. Most asset registration happens here. */
  @Override
  protected void setup() {
    super.setup();
  }

  @Override
  protected void start() {
    super.start();
  }
}
