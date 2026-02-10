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
import com.hypixel.hytale.codec.exception.CodecException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;
import org.echoesfrombeyond.util.type.TypeUtil;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Generic resolver supporting subclasses of {@link Map} with {@link String} as the key type.
 *
 * @see MapCodec
 */
@NullMarked
class MapResolver implements CodecResolver {
  private static class Vars {
    private static final TypeVariable<?> KEY_TYPE = Map.class.getTypeParameters()[0];
    private static final TypeVariable<?> VALUE_TYPE = Map.class.getTypeParameters()[1];
  }

  private final CodecResolver root;

  /**
   * Creates a new instance of this class.
   *
   * @param root the root resolver, which is used to obtain the component type
   */
  MapResolver(CodecResolver root) {
    this.root = root;
  }

  @SuppressWarnings("unchecked")
  @Override
  public @Nullable Codec<?> resolve(Type type, Field field) {
    var raw = TypeUtil.getRawType(type);
    if (raw == null || !Map.class.isAssignableFrom(raw) || Modifier.isAbstract(raw.getModifiers()))
      return null;

    var params = TypeUtil.resolveSupertypeParameters(type, Map.class);
    assert params != null;

    var keyType = params.get(Vars.KEY_TYPE);
    var valueType = params.get(Vars.VALUE_TYPE);
    if (keyType == null || valueType == null || !keyType.equals(String.class)) return null;

    var valueCodec = root.resolve(valueType, field);
    if (valueCodec == null) return null;

    MethodHandle ctor;
    try {
      ctor = MethodHandles.publicLookup().findConstructor(raw, MethodType.methodType(void.class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      return null;
    }

    return new MapCodec<>(
        (Codec<Object>) valueCodec,
        () -> {
          try {
            return (Map<String, Object>) ctor.invoke();
          } catch (Throwable e) {
            throw new CodecException("Problem invoking map constructor", e);
          }
        },
        false);
  }
}
