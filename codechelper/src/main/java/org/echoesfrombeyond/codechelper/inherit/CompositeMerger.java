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

@NullMarked
@ApiStatus.Internal
public class CompositeMerger<T> implements InheritMerger<T> {
  private final List<InheritMerger<T>> mergers;

  public CompositeMerger() {
    this.mergers = new ArrayList<>();
  }

  @Override
  public @Nullable T merge(@Nullable T value, @Nullable T parentValue) {
    T running = value;
    for (var merger : mergers) running = merger.merge(running, parentValue);
    return running;
  }

  public void addMerger(InheritMerger<T> merger) {
    if (merger instanceof CompositeMerger<T> composite) mergers.addAll(composite.mergers);
    else mergers.add(merger);
  }
}
