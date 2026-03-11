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

package org.echoesfrombeyond.dialoguelib.npc;

import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import org.echoesfrombeyond.dialoguelib.dialogue.Dialogue;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class DialogueExistsAssetValidator extends AssetValidator {
  public static final DialogueExistsAssetValidator INSTANCE = new DialogueExistsAssetValidator();

  private DialogueExistsAssetValidator() {}

  @Override
  public String getDomain() {
    return "DialogueAsset";
  }

  @Override
  public boolean test(String s) {
    return Dialogue.ASSET_STORE.get().getAssetMap().getAsset(s) != null;
  }

  @Override
  public String errorMessage(String dialogueKey, String attr) {
    return "Dialogue asset with the name \""
        + dialogueKey
        + "\" does not exist for attribute \""
        + attr
        + "\"";
  }

  @Override
  public String getAssetName() {
    return Dialogue.class.getSimpleName();
  }
}
