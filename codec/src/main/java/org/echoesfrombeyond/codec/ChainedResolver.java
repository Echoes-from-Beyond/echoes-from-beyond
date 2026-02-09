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
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
class ChainedResolver implements CodecResolver {
  private final List<CodecResolver> resolvers;

  ChainedResolver(CodecResolver... initial) {
    this.resolvers = new ArrayList<>(initial.length);
    this.resolvers.addAll(Arrays.asList(initial));
  }

  void append(CodecResolver resolver) {
    resolvers.add(resolver);
  }

  @Override
  public @Nullable Codec<?> resolve(Type type, Field field) {
    for (var resolver : resolvers) {
      var codec = resolver.resolve(type, field);
      if (codec != null) return codec;
    }

    return null;
  }

  @Override
  public CodecResolver chain(CodecResolver other) {
    resolvers.add(other);
    return this;
  }

  @Override
  public CodecResolver withCollectionSupport(
      ImplementationProvider<Collection<?>> implementationProvider) {
    resolvers.add(new CollectionResolver(this, implementationProvider));
    return this;
  }

  @Override
  public CodecResolver withRecursiveResolution() {
    resolvers.add(new RecursiveResolver(this));
    return this;
  }
}
