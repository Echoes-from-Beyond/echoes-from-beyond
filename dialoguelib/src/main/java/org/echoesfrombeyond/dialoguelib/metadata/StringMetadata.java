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
import java.util.Objects;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.dialoguelib.DialoguePlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
@ModelBuilder
@SuppressWarnings("FieldMayBeFinal")
public final class StringMetadata implements DialogueMetadata {
  public static final BuilderCodec<StringMetadata> CODEC =
      CodecUtil.modelBuilder(
          StringMetadata.class, DialoguePlugin.getResolver(), Plugin.getSharedCache());

  public String Value;

  @SuppressWarnings("unused")
  public StringMetadata() {
    this.Value = "";
  }

  public StringMetadata(String initial) {
    this.Value = initial;
  }

  @Override
  @SuppressWarnings("MethodDoesntCallSuperMethod")
  public DialogueMetadata clone() {
    return new StringMetadata(Value);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(Value);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof StringMetadata other && Objects.equals(Value, other.Value);
  }
}
