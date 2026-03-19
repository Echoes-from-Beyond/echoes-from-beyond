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

package org.echoesfrombeyond.dialoguelib.dialogue;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.Doc;
import org.echoesfrombeyond.codechelper.annotation.Opt;
import org.echoesfrombeyond.dialoguelib.DialoguePlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class UIDialogueBase {
  public static final BuilderCodec<UIDialogueBase> CODEC =
      CodecUtil.modelBuilder(
          UIDialogueBase.class, DialoguePlugin.getResolver(), Plugin.getSharedCache());

  @Doc(
      """
      The UI to render. It must contain a group with the tag
      #DialogueChoices. All dialogue choices will be dynamically
      appended to this group. It must also contain a group with the
      tag #DialogueLine. If left absent, defaults to
      Cinematic_Dialogue.ui.
      """)
  @Opt
  public String UiPage;

  @Doc(
      """
      UI file path specifying the formatting of the dialogue choices.
      The UI file must contain only a single top-level Button and a
      Label with the tag #DialogueLabel (the label may or may not be
      contained in the button). If unspecified, defaults to
      Cinematic_Dialogue_Fragment.ui.
      """)
  @Opt
  public String UiFragment;

  @Doc(
      """
      The lifetime of the UI. If left unspecified, defaults to
      CantClose.
      """)
  @Opt
  public CustomPageLifetime Lifetime;

  public UIDialogueBase() {
    this.UiPage = "Cinematic_Dialogue.ui";
    this.UiFragment = "Cinematic_Dialogue_Fragment.ui";
    this.Lifetime = CustomPageLifetime.CantClose;
  }
}
