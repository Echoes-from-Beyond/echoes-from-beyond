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

package org.echoesfrombeyond.example;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import java.util.List;
import java.util.Map;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.jspecify.annotations.NullMarked;

/** Basic integration test for codec-helper. */
@SuppressWarnings({"FieldCanBeLocal", "unused", "FieldMayBeFinal"})
@NullMarked
@ModelBuilder
public class CursedData {
  /** The codec. */
  public static final BuilderCodec<CursedData> CODEC =
      CodecUtil.modelBuilder(CursedData.class, Plugin.getSharedResolver());

  /** Creates a new instance of this class. */
  public CursedData() {
    this.Value = 10;
    this.Cursed = Map.of(List.of("Hello"), Map.of("World", 10));
  }

  private int Value;
  private Map<List<String>, Map<String, Integer>> Cursed;
}
