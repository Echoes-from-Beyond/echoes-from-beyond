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

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import org.echoesfrombeyond.dialoguelib.dialogue.Dialogue;
import org.echoesfrombeyond.plantingyourroots.PlantingYourRoots;
import org.echoesfrombeyond.plantingyourroots.component.KindComponent;
import org.echoesfrombeyond.plantingyourroots.component.RootsComponent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class RootsOpenDialogue extends ActionBase {
  private final BuilderRootsOpenDialogue builder;

  public RootsOpenDialogue(BuilderRootsOpenDialogue builderActionBase) {
    super(builderActionBase);
    this.builder = builderActionBase;
  }

  private @Nullable Dialogue getDialogue() {
    var key = builder.dialogueKey;
    return key == null ? null : Dialogue.ASSET_STORE.get().getAssetMap().getAsset(key);
  }

  private @Nullable Dialogue getTalkedToDialogue() {
    var key = builder.talkedToDialogueKey;
    return key == null ? null : Dialogue.ASSET_STORE.get().getAssetMap().getAsset(key);
  }

  @Override
  public boolean execute(
      Ref<EntityStore> ref,
      Role role,
      InfoProvider sensorInfo,
      double dt,
      Store<EntityStore> store) {
    var player = role.getStateSupport().getInteractionIterationTarget();
    if (player == null) return super.execute(ref, role, sensorInfo, dt, store);

    var talkedToDialogue = getTalkedToDialogue();

    var kind = store.getComponent(ref, KindComponent.getComponentType());
    if (kind != null
        && store
            .ensureAndGetComponent(player, RootsComponent.getComponentType())
            .Dateables
            .getOrDefault(kind.Kind, PlantingYourRoots.DEFAULT_DATEABLE)
            .TalkedTo) {
      if (talkedToDialogue != null) talkedToDialogue.display(player);
      return super.execute(ref, role, sensorInfo, dt, store);
    }

    var dialogue = getDialogue();
    if (dialogue != null) dialogue.display(player);

    return super.execute(ref, role, sensorInfo, dt, store);
  }
}
