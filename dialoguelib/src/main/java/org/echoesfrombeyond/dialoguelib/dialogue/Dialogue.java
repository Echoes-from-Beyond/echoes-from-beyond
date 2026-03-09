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

import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.echoesfrombeyond.annotation.RunOnWorldThread;
import org.echoesfrombeyond.codechelper.internaldep.org.echoesfrombeyond.util.Check;
import org.echoesfrombeyond.modutil.asset.IdentifiedAsset;
import org.echoesfrombeyond.util.thread.Once;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface Dialogue
    extends IdentifiedAsset<String>, JsonAssetWithMap<String, AssetMap<String, Dialogue>> {
  AssetCodecMapCodec<String, Dialogue> CODEC = IdentifiedAsset.codec(Codec.STRING);

  Once<AssetStore<String, Dialogue, AssetMap<String, Dialogue>>> ASSET_STORE =
      Once.of(() -> Check.nonNull(AssetRegistry.getAssetStore(Dialogue.class)));

  @RunOnWorldThread
  void display(Ref<EntityStore> activator);
}
