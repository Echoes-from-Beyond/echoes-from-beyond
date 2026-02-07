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

import com.hypixel.hytale.codec.Codec;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
class CollectionResolver implements CodecResolver {
  private static class Vars {
    private static final TypeVariable<?> ELEMENT_TYPE = Collection.class.getTypeParameters()[0];
  }

  private final CodecResolver root;
  private final Supplier<? extends List<?>> listSupplier;
  private final boolean unmodifiable;

  CollectionResolver(
      CodecResolver root, Supplier<? extends List<?>> listSupplier, boolean unmodifiable) {
    this.root = root;
    this.listSupplier = listSupplier;
    this.unmodifiable = unmodifiable;
  }

  @Override
  @SuppressWarnings("unchecked")
  public @Nullable Codec<?> resolve(Type type) {
    var raw = GenericUtil.getRawType(type);
    if (raw == null || !Collection.class.isAssignableFrom(raw)) return null;

    var params = GenericUtil.resolveSupertypeParameters(type, Collection.class);
    assert params != null;

    var elementType = params.get(Vars.ELEMENT_TYPE);
    assert elementType != null;

    var elementCodec = root.resolve(elementType);
    if (elementCodec == null) return null;

    return new ListCodec<>(
        (Codec<Object>) elementCodec, (Supplier<List<Object>>) listSupplier, unmodifiable);
  }
}
