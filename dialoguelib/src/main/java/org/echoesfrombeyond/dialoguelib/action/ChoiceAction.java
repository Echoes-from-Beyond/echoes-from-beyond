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

import com.hypixel.hytale.codec.lookup.BuilderCodecMapCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.echoesfrombeyond.annotation.RunOnWorldThread;
import org.echoesfrombeyond.dialoguelib.choice.DialogueChoice;
import org.echoesfrombeyond.dialoguelib.dialogue.Dialogue;
import org.jspecify.annotations.NullMarked;

/**
 * Actions to perform once a choice is selected. These can modify the UI, edit a player's save state, etc.
 */
@NullMarked
public interface ChoiceAction {
  BuilderCodecMapCodec<ChoiceAction> CODEC = new BuilderCodecMapCodec<>();

  /**
   * The logic to perform once the choice is selected.
   *
   * @param activator Reference to the entity that is interacting with this dialogue.
   * @param parent The dialogue containing the asset that called this function.
   * @param choice The choice on behalf of which this action is being made.
   */
  @RunOnWorldThread
  void onChosen(Ref<EntityStore> activator, Dialogue parent, DialogueChoice choice);
}
