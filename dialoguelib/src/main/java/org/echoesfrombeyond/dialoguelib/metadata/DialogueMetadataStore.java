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

import java.util.HashMap;
import java.util.Map;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@ModelBuilder
@SuppressWarnings("FieldMayBeFinal")
@NullMarked
public class DialogueMetadataStore implements Cloneable {
  private Map<String, DialogueMetadata> Metadata;

  public DialogueMetadataStore() {
    this.Metadata = new HashMap<>();
  }

  public @Nullable DialogueMetadata get(String key) {
    return Metadata.get(key);
  }

  public @Nullable DialogueMetadata put(String key, DialogueMetadata value) {
    return Metadata.put(key, value);
  }

  public @Nullable DialogueMetadata remove(String key) {
    return Metadata.remove(key);
  }

  public @Nullable DialogueMetadata putString(String key, String value) {
    return Metadata.put(key, new StringMetadata(value));
  }

  public @Nullable DialogueMetadata putInteger(String key, int value) {
    return Metadata.put(key, new IntegerMetadata(value));
  }

  public @Nullable DialogueMetadata putBoolean(String key, boolean value) {
    return Metadata.put(key, new BooleanMetadata(value));
  }

  @Override
  @SuppressWarnings("MethodDoesntCallSuperMethod")
  public DialogueMetadataStore clone() {
    var newStore = new DialogueMetadataStore();
    for (var entry : Metadata.entrySet()) newStore.put(entry.getKey(), entry.getValue().clone());

    return newStore;
  }
}
