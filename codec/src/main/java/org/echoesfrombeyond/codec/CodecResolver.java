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
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import org.echoesfrombeyond.codec.annotation.ModelBuilder;
import org.echoesfrombeyond.codec.cache.CodecCache;
import org.echoesfrombeyond.util.Check;
import org.echoesfrombeyond.util.type.HashClassHierarchyMap;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Given a {@link Type} and a {@link Field}, attempts to resolve a {@link Codec} capable of
 * serializing values contained in the field, as well as deserializing data that may be written to
 * the field.
 *
 * @see CodecResolver#builder() builder method to compose instances of this interface
 * @see CodecUtil to use instances of this class to generate custom Codec instances
 */
@NullMarked
@FunctionalInterface
public interface CodecResolver {
  /** The default field name for the key component of a map entry. */
  String DEFAULT_MAP_KEY_NAME = "Key";

  /** The default field name for the value component of a map entry. */
  String DEFAULT_MAP_VALUE_NAME = "Value";

  /**
   * A {@link CodecResolver} capable of resolving all primitive types, their associated wrapper
   * (boxed) types, as well as {@link String}.
   */
  CodecResolver PRIMITIVE =
      new CodecResolver() {
        private static final Map<Class<?>, Codec<?>> PRIMITIVE_CODEC_MAP =
            Map.ofEntries(
                Map.entry(boolean.class, Codec.BOOLEAN),
                Map.entry(Boolean.class, Codec.BOOLEAN),
                Map.entry(byte.class, Codec.BYTE),
                Map.entry(Byte.class, Codec.BYTE),
                Map.entry(short.class, Codec.SHORT),
                Map.entry(Short.class, Codec.SHORT),
                Map.entry(int.class, Codec.INTEGER),
                Map.entry(Integer.class, Codec.INTEGER),
                Map.entry(float.class, Codec.FLOAT),
                Map.entry(Float.class, Codec.FLOAT),
                Map.entry(long.class, Codec.LONG),
                Map.entry(Long.class, Codec.LONG),
                Map.entry(double.class, Codec.DOUBLE),
                Map.entry(Double.class, Codec.DOUBLE),
                Map.entry(String.class, Codec.STRING));

        @Override
        public @Nullable Codec<?> resolve(Type type, Field field) {
          if (!(type instanceof Class<?> raw)) return null;
          return PRIMITIVE_CODEC_MAP.get(raw);
        }
      };

  /**
   * Resolves a codec, given a {@link Type} (which may contain generic information) and {@link
   * Field}.
   *
   * <p>The type may differ from the field's type. For example, a resolver attempting to resolve a
   * field of type List&lt;String&gt; may first need to resolve the raw type (List), and then the
   * component type (String), separately but for the same field.
   *
   * @param type the type to resolve
   * @param field the field
   * @return {@code null} if this resolver cannot resolve the type; otherwise the resolved {@link
   *     Codec}
   */
  @Nullable Codec<?> resolve(Type type, Field field);

  /** Builder of composite {@link CodecResolver}s. */
  sealed interface Builder permits BuilderImpl {
    /**
     * Adds a new resolver. This is order-sensitive: resolvers {@code chain}ed last will be
     * evaluated last. The first resolver whose {@link CodecResolver#resolve(Type, Field)} method
     * returns a non-null value will determine the result of the composite resolver.
     *
     * <p>Pass {@link CodecResolver#PRIMITIVE} here to enable resolution of basic primitive types.
     *
     * @param resolver the resolver
     * @return this instance
     * @throws NullPointerException if {@code resolver} is null
     */
    @Contract("_ -> this")
    Builder chain(CodecResolver resolver);

    /**
     * Adds a subtype mapping.
     *
     * <p>When the resulting resolver encounters a field of type {@code baseClass}, it will instead
     * attempt to resolve a codec for {@code subClass}. This is particularly useful when {@code
     * baseClass} is an abstract class or interface that cannot be instantiated directly.
     *
     * <p>Subclass resolution does not require an exact {@code baseClass} match. For example, if
     * base class {@link List} is mapped to subclass {@link ArrayList}, instances of {@link
     * Collection} will match, because List is a sub-interface of Collection.
     *
     * <p>However, if there is a mapping from Collection to e.g. {@link HashSet}, the latter will be
     * used instead. This is because, judging by the inheritance tree, Collection is closer to
     * itself than it is to List.
     *
     * @param baseClass the base class
     * @param subClass the subclass
     * @return this instance
     * @param <T> the base class type
     */
    @Contract("_, _ -> this")
    <T> Builder withSubtypeMapping(Class<T> baseClass, Class<? extends T> subClass);

    /**
     * Enables "recursive resolution", that is, types annotated with {@link ModelBuilder} containing
     * types that are, themselves, annotated with {@link ModelBuilder}.
     *
     * <p>Recursive resolution will not use any {@link CodecCache}.
     *
     * @return this instance
     */
    @Contract("-> this")
    Builder withRecursiveResolution();

    /**
     * Enables "recursive resolution", that is, types annotated with {@link ModelBuilder} containing
     * types that are, themselves, annotated with {@link ModelBuilder}.
     *
     * <p>Recursive resolution will use the provided cache.
     *
     * @param cache the cache to use when resolving recursively
     * @return this instance
     */
    @Contract("_ -> this")
    Builder withRecursiveResolution(CodecCache cache);

    /**
     * Enables support for array codec resolution.
     *
     * <p>On its own, this will enable all primitive arrays to be resolved, as well as all arrays
     * whose component type is supported, recursively (i.e. multidimensional arrays are supported).
     *
     * @return this instance
     */
    @Contract("-> this")
    Builder withArraySupport();

    /**
     * Enables support for collection resolution.
     *
     * <p>Users will typically want to add one or more subtype mappings, as it is conventional to
     * use abstract types or interfaces for type declarations of collections. For example, call
     * {@code builder.withSubtypeMapping(List.class, ArrayList.class)} to construct {@link
     * ArrayList} instances whenever a {@link List} is used as a field type.
     *
     * @return this instance
     */
    @Contract("-> this")
    Builder withCollectionSupport();

    /**
     * Enables support for {@link Map} implementations.
     *
     * <p>Maps that have a key type of {@link String} will be (de)serialized as per {@link
     * MapCodec}. Maps that do not will be deserialized as an array of entry objects, each of which
     * has 2 fields, named {@code Key} and {@code Value}. To change the name of these fields, see
     * {@link Builder#withMapSupport(String, String)}
     *
     * <p>Otherwise works similarly to {@link Builder#withCollectionSupport()}, including the
     * requirement to add subtype mappings if it is desirable to resolve abstract types.
     *
     * @return this instance
     */
    @Contract("-> this")
    Builder withMapSupport();

    /**
     * Works identically to {@link Builder#withMapSupport()}, but sets the map entry key/value field
     * names to arbitrary values instead of the default of {@code Key}/{@code Value}.
     *
     * @param keyName the new key field name
     * @param valueName the new value field name
     * @return this instance
     * @throws IllegalArgumentException if {@code keyName.equals(valueName)}
     * @throws NullPointerException if {@code keyName == null || valueName == null}
     */
    @Contract("_, _ -> this")
    Builder withMapSupport(String keyName, String valueName);

    /**
     * Builds a new {@link CodecResolver}. This method may be called multiple times to construct
     * multiple instances of {@link CodecResolver} with the same settings.
     *
     * @return a new CodecResolver
     */
    @Contract("-> new")
    CodecResolver build();
  }

  /**
   * Constructs a new {@link Builder}.
   *
   * @return a new Builder
   */
  @Contract("-> new")
  static Builder builder() {
    return new BuilderImpl();
  }

  /** Internal {@link Builder} implementation. */
  final class BuilderImpl implements Builder {
    private final List<CodecResolver> resolvers;
    private final Map<Class<?>, Class<?>> subtypeMap;

    private boolean recursiveResolution;
    private @Nullable CodecCache recursiveResolutionCache;
    private boolean arraySupport;
    private boolean collectionSupport;
    private boolean mapSupport;

    private String keyName = DEFAULT_MAP_KEY_NAME;
    private String valueName = DEFAULT_MAP_VALUE_NAME;

    private BuilderImpl() {
      this.resolvers = new ArrayList<>();
      this.subtypeMap = new HashMap<>();
    }

    @Override
    public Builder chain(CodecResolver resolver) {
      resolvers.add(Check.nonNull(resolver));
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
    public Builder withRecursiveResolution(CodecCache cache) {
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
    public Builder withMapSupport() {
      mapSupport = true;
      keyName = DEFAULT_MAP_KEY_NAME;
      valueName = DEFAULT_MAP_VALUE_NAME;
      return this;
    }

    @Override
    public Builder withMapSupport(String keyName, String valueName) {
      Check.nonNull(keyName);
      Check.nonNull(valueName);

      if (keyName.equals(valueName))
        throw new IllegalArgumentException("keyName may not equal valueName");

      mapSupport = true;
      this.keyName = keyName;
      this.valueName = valueName;
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
      if (mapSupport) resolversCopy.add(new MapResolver(chained, keyName, valueName));
      if (recursiveResolution)
        resolversCopy.add(new RecursiveResolver(chained, recursiveResolutionCache));

      resolversCopy.trimToSize();
      return chained;
    }
  }
}
