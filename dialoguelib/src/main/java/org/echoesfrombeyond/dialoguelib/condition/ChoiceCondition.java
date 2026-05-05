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

package org.echoesfrombeyond.dialoguelib.condition;

import com.hypixel.hytale.codec.lookup.BuilderCodecMapCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.echoesfrombeyond.annotation.RunOnWorldThread;
import org.echoesfrombeyond.dialoguelib.choice.DialogueChoice;
import org.echoesfrombeyond.dialoguelib.dialogue.Dialogue;
import org.jspecify.annotations.NullMarked;

/**
 * A ChoiceCondition asset is responsible for determining whether a choice can appear to a player.
 */
@NullMarked
public interface ChoiceCondition {
  BuilderCodecMapCodec<ChoiceCondition> CODEC = new BuilderCodecMapCodec<>();

  /**
   * Evaluates whether a choice is able to be displayed.
   *
   * @param activator Reference to the entity that is interacting with this dialogue.
   * @param parent The dialogue containing the asset that called this function.
   * @param choice The choice on behalf of which this check is being made.
   * @return {@code true} if the condition is met, {@code false} otherwise.
   */
  @RunOnWorldThread
  boolean shouldDisplay(Ref<EntityStore> activator, Dialogue parent, DialogueChoice choice);
}
