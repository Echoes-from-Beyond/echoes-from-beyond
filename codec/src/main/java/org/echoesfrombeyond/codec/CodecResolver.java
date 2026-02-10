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
import java.util.*;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Given a {@link Type} and a {@link Field}, attempts to resolve a {@link Codec} capable of
 * serializing values contained in the field, as well as deserializing data that may be written to
 * the field.
 */
@NullMarked
@FunctionalInterface
public interface CodecResolver {
  @Nullable Codec<?> resolve(Type type, Field field);

  sealed interface Builder permits BuilderImpl {
    Builder chain(CodecResolver resolver);

    <T> Builder withSubtypeMapping(Class<T> baseClass, Class<? extends T> subClass);

    Builder withRecursiveResolution();

    Builder withRecursiveResolution(@Nullable CodecCache cache);

    Builder withArraySupport();

    Builder withCollectionSupport();

    CodecResolver build();
  }

  static Builder builder() {
    return new BuilderImpl();
  }

  final class BuilderImpl implements Builder {
    private final List<CodecResolver> resolvers;
    private final Map<Class<?>, Class<?>> subtypeMap;

    private boolean recursiveResolution;
    private @Nullable CodecCache recursiveResolutionCache;
    private boolean arraySupport;
    private boolean collectionSupport;

    private BuilderImpl() {
      this.resolvers = new ArrayList<>();
      this.subtypeMap = new HashMap<>();
    }

    @Override
    public Builder chain(CodecResolver resolver) {
      resolvers.add(resolver);
      return this;
    }

    @Override
    public <T> Builder withSubtypeMapping(Class<T> baseClass, Class<? extends T> subClass) {
      if (!baseClass.isAssignableFrom(subClass))
        throw new IllegalArgumentException(
            subClass.getName() + " does not extend " + baseClass.getName());

      if (baseClass.equals(subClass))
        throw new IllegalArgumentException(
            "Superclass " + baseClass.getName() + " cannot be equal to subclass");

      subtypeMap.put(baseClass, subClass);
      return this;
    }

    @Override
    public Builder withRecursiveResolution() {
      recursiveResolution = true;
      return this;
    }

    @Override
    public Builder withRecursiveResolution(@Nullable CodecCache cache) {
      recursiveResolution = true;
      recursiveResolutionCache = cache;
      return this;
    }

    @Override
    public Builder withArraySupport() {
      arraySupport = true;
      return this;
    }

    @Override
    public Builder withCollectionSupport() {
      collectionSupport = true;
      return this;
    }

    @Override
    public CodecResolver build() {
      var resolversCopy = new ArrayList<>(resolvers);
      var chained = new ChainedResolver(resolversCopy);

      if (!subtypeMap.isEmpty()) {
        var map = new HashClassHierarchyMap<Class<?>>();
        for (var entry : subtypeMap.entrySet()) map.put(entry.getKey(), entry.getValue());
        resolversCopy.add(new SubtypeResolver(chained, map));
      }
      if (arraySupport) resolversCopy.add(new ArrayResolver(chained));
      if (collectionSupport) resolversCopy.add(new CollectionResolver(chained));
      if (recursiveResolution)
        resolversCopy.add(new RecursiveResolver(chained, recursiveResolutionCache));

      resolversCopy.trimToSize();
      return chained;
    }
  }
}
