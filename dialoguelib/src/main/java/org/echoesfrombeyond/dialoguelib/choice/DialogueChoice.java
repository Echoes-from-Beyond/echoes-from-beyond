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

package org.echoesfrombeyond.dialoguelib.choice;

import com.hypixel.hytale.codec.lookup.BuilderCodecMapCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.echoesfrombeyond.annotation.RunOnWorldThread;
import org.echoesfrombeyond.dialoguelib.dialogue.Dialogue;
import org.jspecify.annotations.NullMarked;

/**
 * A {@code DialogueChoice} can be understood as anything that manages the display of, and navigation between texts that
 * can appear on the UI. Although its name implies that this is confined to what players can interact with, choices also
 * control the displayed line, since it can differ based on choices made prior to its display.
 */
@NullMarked
public interface DialogueChoice {
  BuilderCodecMapCodec<DialogueChoice> CODEC = new BuilderCodecMapCodec<>();

  /**
   * Gets the text associated with this choice.
   *
   * @param activator Reference to the entity that is interacting with this dialogue.
   * @param parent The dialogue containing the asset that called this function.
   * @return The text to display, whether for a button or the line.
   */
  @RunOnWorldThread
  String getMessage(Ref<EntityStore> activator, Dialogue parent);

  /**
   * Executes further actions if this choice is chosen as part of an interactive UI.
   *
   * @param activator Reference to the entity that is interacting with this dialogue.
   * @param parent The dialogue containing the asset that called this function.
   */
  @RunOnWorldThread
  void onChosen(Ref<EntityStore> activator, Dialogue parent);

  /**
   * Whether this choice can appear, based on the associated ChoiceCondition or any arbitrary conditions.
   *
   * @param activator Reference to the entity that is interacting with this dialogue.
   * @param parent The dialogue containing the asset that called this function.
   * @return {@code true} if this choice should disappear, {@code false} if not.
   */
  @RunOnWorldThread
  boolean shouldDisplay(Ref<EntityStore> activator, Dialogue parent);
}
