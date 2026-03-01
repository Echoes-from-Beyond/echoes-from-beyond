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

package org.echoesfrombeyond.dialoguelib;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import java.util.*;
import org.echoesfrombeyond.codechelper.CodecResolver;
import org.echoesfrombeyond.codechelper.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@SuppressWarnings("unused")
public class DialoguePlugin extends JavaPlugin {
  private static @Nullable CodecResolver RESOLVER;

  public DialoguePlugin(JavaPluginInit init) {
    super(init);
  }

  @Override
  protected void setup() {
    RESOLVER =
        CodecResolver.builder()
            .withCollectionSupport()
            .withRecursiveResolution(Plugin.getSharedCache())
            .withSubtypeMapping(List.class, ArrayList.class)
            .withDirectMapping(Dialogue.class, Dialogue.CODEC)
            .withDirectMapping(DialogueChoice.class, DialogueChoice.CODEC)
            .withDirectMapping(Message.class, Message.CODEC)
            .withDirectMapping(Trigger.class, Trigger.CODEC)
            .build();

    Dialogue.CODEC.register("Standard", StandardDialogue.class, StandardDialogue.CODEC);
  }

  @Override
  protected void shutdown() {
    RESOLVER = null;
  }

  @ApiStatus.Internal
  public static CodecResolver getResolver() {
    var resolver = RESOLVER;
    if (resolver == null) throw new IllegalStateException("Plugin must be initialized");

    return resolver;
  }
}
