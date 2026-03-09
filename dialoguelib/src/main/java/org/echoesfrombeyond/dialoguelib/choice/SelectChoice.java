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

package org.echoesfrombeyond.dialoguelib.choice;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.HashMap;
import java.util.Map;
import org.echoesfrombeyond.annotation.RunOnWorldThread;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.Doc;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.dialoguelib.DialoguePlugin;
import org.echoesfrombeyond.dialoguelib.dialogue.Dialogue;
import org.echoesfrombeyond.dialoguelib.metadata.MetadataAccessor;
import org.echoesfrombeyond.dialoguelib.metadata.StringMetadata;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@Doc(
    """
    A DialogueChoice that delegates to one of a number of child
    choices based on a dialogue metadata value.
    """)
@NullMarked
@ModelBuilder
public class SelectChoice extends MetadataAccessor implements DialogueChoice {
  public static final BuilderCodec<SelectChoice> CODEC =
      CodecUtil.modelBuilder(
          SelectChoice.class,
          MetadataAccessor.CODEC,
          DialoguePlugin.getResolver(),
          Plugin.getSharedCache());

  @Doc(
      """
      The choice that is shown when no choice from Options is
      selected. If unspecified, no choice will be displayed unless
      a key in Options matches the metadata value.
      """)
  public @Nullable DialogueChoice Default;

  @Doc(
      """
      A mapping of possible metadata values to the choices that should
      be displayed when the actual value is equal to them.
      """)
  public Map<String, DialogueChoice> Options;

  public SelectChoice() {
    this.Options = new HashMap<>();
  }

  @RunOnWorldThread
  private @Nullable DialogueChoice findDelegate(Ref<EntityStore> activator, Dialogue parent) {
    if (!(getMetadata(activator, parent) instanceof StringMetadata stringMetadata)) return Default;

    var result = Options.get(stringMetadata.Value);
    return result == null ? Default : result;
  }

  @Override
  @RunOnWorldThread
  public String getMessage(Ref<EntityStore> activator, Dialogue parent) {
    var delegate = findDelegate(activator, parent);
    return delegate == null ? "" : delegate.getMessage(activator, parent);
  }

  @Override
  @RunOnWorldThread
  public void onChosen(Ref<EntityStore> activator, Dialogue parent) {
    var delegate = findDelegate(activator, parent);
    if (delegate != null) delegate.onChosen(activator, parent);
  }

  @Override
  @RunOnWorldThread
  public boolean shouldDisplay(Ref<EntityStore> activator, Dialogue parent) {
    var delegate = findDelegate(activator, parent);
    return delegate != null && delegate.shouldDisplay(activator, parent);
  }
}
