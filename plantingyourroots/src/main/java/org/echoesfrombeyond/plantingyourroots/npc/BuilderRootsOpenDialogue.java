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

package org.echoesfrombeyond.plantingyourroots.npc;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import org.echoesfrombeyond.dialoguelib.npc.DialogueExistsAssetValidator;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class BuilderRootsOpenDialogue extends BuilderActionBase {
  protected @Nullable String dialogueKey;
  protected @Nullable String talkedToDialogueKey;

  @Override
  public String getShortDescription() {
    return "Open dialogue";
  }

  @Override
  public String getLongDescription() {
    return "Opens a dialogue menu.";
  }

  @Override
  public Action build(BuilderSupport builderSupport) {
    return new RootsOpenDialogue(this);
  }

  @Override
  public Builder<Action> readConfig(JsonElement data) {
    this.requireAsset(
        data,
        "Dialogue",
        (key) -> dialogueKey = key,
        DialogueExistsAssetValidator.INSTANCE,
        BuilderDescriptorState.Stable,
        "The dialogue.",
        "The dialogue to open when this action is triggered");

    this.requireAsset(
        data,
        "TalkedToDialogue",
        (key) -> talkedToDialogueKey = key,
        DialogueExistsAssetValidator.INSTANCE,
        BuilderDescriptorState.Stable,
        "Talked-to dialogue",
        "The dialogue to open when this NPC has already been talked to");

    return super.readConfig(data);
  }

  @Override
  public BuilderDescriptorState getBuilderDescriptorState() {
    return BuilderDescriptorState.Stable;
  }
}
