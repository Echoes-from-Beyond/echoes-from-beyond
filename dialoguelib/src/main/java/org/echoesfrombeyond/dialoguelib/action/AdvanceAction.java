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

package org.echoesfrombeyond.dialoguelib.action;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.dialoguelib.DialoguePlugin;
import org.echoesfrombeyond.dialoguelib.choice.DialogueChoice;
import org.echoesfrombeyond.dialoguelib.dialogue.Dialogue;
import org.jspecify.annotations.NullMarked;

@NullMarked
@ModelBuilder
public class AdvanceAction implements ChoiceAction {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  public static final BuilderCodec<AdvanceAction> CODEC =
      CodecUtil.modelBuilder(
          AdvanceAction.class, DialoguePlugin.getResolver(), Plugin.getSharedCache());

  public String Next;

  public AdvanceAction() {
    this.Next = "";
  }

  @Override
  public void onChosen(Ref<EntityStore> activator, Dialogue parent, DialogueChoice choice) {
    var next = Next;
    var nextDialogue = Dialogue.ASSET_STORE.get().getAssetMap().getAsset(next);

    if (nextDialogue == null) {
      LOGGER.atWarning().log("AdvanceAction references non-existent dialogue `%s`", next);
      return;
    }

    nextDialogue.display(activator);
  }
}
