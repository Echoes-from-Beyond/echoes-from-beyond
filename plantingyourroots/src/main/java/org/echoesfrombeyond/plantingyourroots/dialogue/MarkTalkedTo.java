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

package org.echoesfrombeyond.plantingyourroots.dialogue;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.echoesfrombeyond.annotation.RunOnWorldThread;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.dialoguelib.action.ChoiceAction;
import org.echoesfrombeyond.dialoguelib.choice.DialogueChoice;
import org.echoesfrombeyond.dialoguelib.dialogue.Dialogue;
import org.echoesfrombeyond.plantingyourroots.component.RootsComponent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@ModelBuilder
public class MarkTalkedTo implements ChoiceAction {
  public static final BuilderCodec<MarkTalkedTo> CODEC =
      CodecUtil.modelBuilder(
          MarkTalkedTo.class, Plugin.getSharedResolver(), Plugin.getSharedCache());

  public @Nullable String Kind;

  @Override
  @RunOnWorldThread
  public void onChosen(Ref<EntityStore> activator, Dialogue parent, DialogueChoice choice) {
    var kind = Kind;
    if (kind == null) return;

    activator
            .getStore()
            .ensureAndGetComponent(activator, RootsComponent.getComponentType())
            .Dateables
            .computeIfAbsent(kind, _ -> new RootsComponent.Dateable())
            .TalkedTo =
        true;
  }
}
