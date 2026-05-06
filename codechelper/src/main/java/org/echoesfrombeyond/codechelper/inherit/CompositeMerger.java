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

package org.echoesfrombeyond.codechelper.inherit;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Special implementation of {@link InheritMerger} that combines multiple sub-mergers.
 *
 * <p>This API is internal and is not meant to be used by classes outside this library.
 *
 * @param <T> the field type
 */
@NullMarked
@ApiStatus.Internal
public class CompositeMerger<T> implements InheritMerger<T> {
  private final List<InheritMerger<T>> mergers;

  /** Creates a new instance of this class. */
  public CompositeMerger() {
    this.mergers = new ArrayList<>();
  }

  @Override
  public @Nullable T merge(@Nullable T defaultValue, @Nullable T parentValue) {
    T running = defaultValue;
    for (var merger : mergers) running = merger.merge(running, parentValue);
    return running;
  }

  /**
   * Adds a new merger. Mergers added later will be executed later during the composite merge.
   *
   * @param merger the merger to add
   */
  public void addMerger(InheritMerger<T> merger) {
    mergers.add(merger);
  }
}
