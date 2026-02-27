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

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;

@NullUnmarked
@SuppressWarnings("FieldMayBeFinal")
public class StandardDialogue extends IdentifiedAssetBase<@NonNull String> implements Dialogue {
  public static BuilderCodec<StandardDialogue> CODEC =
      CodecUtil.modelBuilder(
          StandardDialogue.class, DialoguePlugin.getResolver(), Plugin.getSharedCache());

  private List<DialogueChoice> Choices;

  public StandardDialogue() {}

  @Override
  public void display(@NonNull Ref<EntityStore> activator) {
    // TODO: render the choices
  }
}
