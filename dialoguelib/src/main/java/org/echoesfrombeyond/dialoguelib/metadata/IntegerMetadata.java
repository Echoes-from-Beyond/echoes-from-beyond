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

package org.echoesfrombeyond.dialoguelib.metadata;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.dialoguelib.DialoguePlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
@ModelBuilder
@SuppressWarnings("FieldMayBeFinal")
public final class IntegerMetadata implements DialogueMetadata {
  public static final BuilderCodec<IntegerMetadata> CODEC =
      CodecUtil.modelBuilder(
          IntegerMetadata.class, DialoguePlugin.getResolver(), Plugin.getSharedCache());

  public int Value;

  @SuppressWarnings("unused")
  public IntegerMetadata() {}

  public IntegerMetadata(int initial) {
    this.Value = initial;
  }

  @Override
  @SuppressWarnings("MethodDoesntCallSuperMethod")
  public DialogueMetadata clone() {
    return new IntegerMetadata(Value);
  }

  @Override
  public int hashCode() {
    return Integer.hashCode(Value);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof IntegerMetadata other && Value == other.Value;
  }
}
