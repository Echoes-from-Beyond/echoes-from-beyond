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

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.Doc;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.dialoguelib.DialoguePlugin;
import org.echoesfrombeyond.dialoguelib.dialogue.Dialogue;
import org.jspecify.annotations.NullMarked;

@NullMarked
@ModelBuilder
@Doc(
    """
    A DialogueChoice implementation meant to be used in situations
    where neither dialogue actions nor conditions are relevant. While
    this works the same as a Standard DialogueChoice but with Condition
    and Actions omitted, new users might find it more intuitive.
    """)
public class DisplayChoice implements DialogueChoice {
  /** Codec for DisplayChoice */
  public static final BuilderCodec<DisplayChoice> CODEC =
      CodecUtil.modelBuilder(
          DisplayChoice.class, DialoguePlugin.getResolver(), Plugin.getSharedCache());

  @Doc("The text to display.")
  public String Text;

  public DisplayChoice() {
    Text = "";
  }

  @Override
  public String getMessage(Ref<EntityStore> activator, Dialogue parent) {
    return Text;
  }

  // DisplayChoices are inert; they're not meant to do anything
  @Override
  public void onChosen(Ref<EntityStore> activator, Dialogue parent) {}

  @Override
  public boolean shouldDisplay(Ref<EntityStore> activator, Dialogue parent) {
    // DisplayChoices are always shown
    return true;
  }
}
