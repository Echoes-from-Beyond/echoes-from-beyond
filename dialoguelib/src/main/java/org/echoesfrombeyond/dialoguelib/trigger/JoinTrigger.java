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

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.Data;
import org.echoesfrombeyond.codechelper.annotation.Doc;
import org.echoesfrombeyond.codechelper.annotation.Id;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.dialoguelib.DialoguePlugin;
import org.echoesfrombeyond.dialoguelib.dialogue.Dialogue;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@Doc(
    """
    Trigger to activate dialogue whenever a player first joins the
    server.
    """)
@NullMarked
@ModelBuilder
public class JoinTrigger extends TriggerBase {
  public static final AssetBuilderCodec<String, JoinTrigger> CODEC =
      CodecUtil.modelAssetBuilder(
          JoinTrigger.class,
          TriggerBase.CODEC,
          DialoguePlugin.getResolver(),
          Plugin.getSharedCache());

  @Id private @Nullable String Id;
  @Data private AssetExtraInfo.@Nullable Data Data;

  @Override
  public void link(JavaPlugin linker, Dialogue dialogue) {
    linker
        .getEventRegistry()
        .registerGlobal(PlayerReadyEvent.class, ready -> dialogue.display(ready.getPlayerRef()));
  }

  @Override
  public void setId(String id) {
    this.Id = id;
  }

  @Override
  public void setData(AssetExtraInfo.@Nullable Data data) {
    this.Data = data;
  }

  @Override
  public AssetExtraInfo.@Nullable Data getData() {
    return Data;
  }

  @Override
  public @Nullable String getId() {
    return Id;
  }
}
