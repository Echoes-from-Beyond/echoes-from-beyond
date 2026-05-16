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

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class BuilderOpenDialogue extends BuilderActionBase {
  protected @Nullable String dialogueKey;

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
    return new OpenDialogue(this);
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

    return super.readConfig(data);
  }

  @Override
  public BuilderDescriptorState getBuilderDescriptorState() {
    return BuilderDescriptorState.Stable;
  }
}
