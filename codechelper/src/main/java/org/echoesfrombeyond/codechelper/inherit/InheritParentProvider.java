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

import java.lang.reflect.Field;
import org.echoesfrombeyond.codechelper.annotation.inherit.InheritParent;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Provider for {@link InheritParent}. */
@NullMarked
@ApiStatus.Internal
public class InheritParentProvider implements InheritMergerProvider<InheritParent> {
  /** Singleton instance of this class. */
  @SuppressWarnings("unused")
  public static final InheritParentProvider INSTANCE = new InheritParentProvider();

  private static final Provider FORCE = new Provider(true);
  private static final Provider NO_FORCE = new Provider(false);

  private InheritParentProvider() {}

  private record Provider(boolean force) implements InheritMerger<Object> {
    @Override
    public @Nullable Object merge(@Nullable Object defaultValue, @Nullable Object parentValue) {
      if (force) return parentValue;
      return parentValue == null ? defaultValue : parentValue;
    }
  }

  @Override
  public Class<InheritParent> getArgsType() {
    return InheritParent.class;
  }

  @Override
  public InheritMerger<?> getInstance(InheritParent args, Field ignored) {
    return args.value() ? FORCE : NO_FORCE;
  }
}
