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

package org.echoesfrombeyond.dialoguelib.trigger;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import java.util.Collections;
import java.util.Set;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.Doc;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.dialoguelib.DialoguePlugin;
import org.echoesfrombeyond.modutil.asset.IdentifiedAssetBase;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

@NullMarked
@ModelBuilder
@SuppressWarnings("FieldMayBeFinal")
public abstract class TriggerBase extends IdentifiedAssetBase<String> implements Trigger {
  public static final BuilderCodec<TriggerBase> CODEC =
      CodecUtil.modelBuilder(
          TriggerBase.class, DialoguePlugin.getResolver(), Plugin.getSharedCache());

  @Doc(
      """
      A set of dialogue IDs that should be activated by this trigger.
      """)
  private Set<String> TargetIds;

  protected TriggerBase() {
    this.TargetIds = Set.of();
  }

  @Override
  public final @Unmodifiable Set<String> getTargetIds() {
    return Collections.unmodifiableSet(TargetIds);
  }
}
