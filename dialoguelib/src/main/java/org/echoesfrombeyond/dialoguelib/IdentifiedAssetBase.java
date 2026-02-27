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

package org.echoesfrombeyond.dialoguelib;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public abstract class IdentifiedAssetBase<K> implements IdentifiedAsset<K> {
  private K id;
  private AssetExtraInfo.@Nullable Data data;

  @Override
  public final void setId(K id) {
    this.id = id;
  }

  @Override
  public final void setData(AssetExtraInfo.@Nullable Data data) {
    this.data = data;
  }

  @Override
  public final AssetExtraInfo.@Nullable Data getData() {
    return data;
  }

  @Override
  public final K getId() {
    return id;
  }
}
