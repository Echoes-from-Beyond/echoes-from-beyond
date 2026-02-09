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
import java.util.Collection;
import java.util.List;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@FunctionalInterface
public interface CodecResolver {
  @Nullable Codec<?> resolve(Type type, Field field);

  sealed interface Builder permits BuilderImpl {
    Builder chain(CodecResolver resolver);

    Builder withCollectionSupport(ImplementationProvider<Collection<?>> implementationProvider);

    Builder withRecursiveResolution();

    Builder withRecursiveResolution(CodecCache cache);

    CodecResolver build();
  }

  static Builder builder() {
    return new BuilderImpl();
  }

  final class BuilderImpl implements Builder {
    private List<CodecResolver> resolvers;
    private CodecResolver result;

    private BuilderImpl() {
      this.resolvers = new ArrayList<>();
      this.result = new ChainedResolver(this.resolvers);
    }

    @Override
    public Builder chain(CodecResolver resolver) {
      resolvers.add(resolver);
      return this;
    }

    @Override
    public Builder withCollectionSupport(
        ImplementationProvider<Collection<?>> implementationProvider) {
      resolvers.add(new CollectionResolver(result, implementationProvider));
      return this;
    }

    @Override
    public Builder withRecursiveResolution() {
      resolvers.add(new RecursiveResolver(result, null));
      return this;
    }

    @Override
    public Builder withRecursiveResolution(CodecCache cache) {
      resolvers.add(new RecursiveResolver(result, cache));
      return this;
    }

    @Override
    public CodecResolver build() {
      var resolvers = this.resolvers;
      var result = this.result;

      this.resolvers = new ArrayList<>(resolvers);
      this.result = new ChainedResolver(this.resolvers);

      return result;
    }
  }
}
