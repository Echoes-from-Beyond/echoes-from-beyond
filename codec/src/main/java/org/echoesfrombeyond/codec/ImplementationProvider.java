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

package org.echoesfrombeyond.codec;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import org.echoesfrombeyond.util.Check;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public sealed interface ImplementationProvider<V> permits ImplementationProviderImpl {
  record Spec<C>(Supplier<? extends C> creator, @Nullable Immutable<C> immutable) {
    public record Immutable<C>(
        Class<C> type, Function<? super C, ? extends C> makeImmutable, C emptyImmutable) {}
  }

  class Builder<B> {
    private final Map<Class<?>, Class<?>> abstractMappings;
    private final List<Spec.Immutable<?>> immutableMappings;

    private Builder() {
      this.abstractMappings = new HashMap<>();
      this.immutableMappings = new ArrayList<>();
    }

    public <T extends B> Builder<B> withAbstractMapping(
        Class<T> abstractClass, Class<? extends T> concreteClass) {
      abstractMappings.put(Check.nonNull(abstractClass), Check.nonNull(concreteClass));
      return this;
    }

    public <T extends B> Builder<B> withImmutable(Spec.Immutable<T> immutable) {
      immutableMappings.add(immutable);
      return this;
    }

    public ImplementationProvider<B> build() {
      return new ImplementationProviderImpl<>(abstractMappings, immutableMappings);
    }
  }

  static <B> Builder<B> builder() {
    return new Builder<>();
  }

  Spec<? extends V> forType(Class<?> type, Field field);
}
