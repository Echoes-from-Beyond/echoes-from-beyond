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

package org.echoesfrombeyond.codechelper;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.exception.CodecException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import org.echoesfrombeyond.util.type.TypeUtil;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Generic resolver supporting subclasses of {@link Collection}.
 *
 * @see CollectionCodec
 */
@NullMarked
class CollectionResolver implements CodecResolver {
  private final CodecResolver root;

  /**
   * Creates a new instance of this class.
   *
   * @param root the root resolver, which is used to obtain the component type
   */
  CollectionResolver(CodecResolver root) {
    this.root = root;
  }

  @Override
  @SuppressWarnings("unchecked")
  public @Nullable Codec<?> resolve(Type type, Field field) {
    var raw = TypeUtil.getRawType(type);
    if (raw == null
        || !Collection.class.isAssignableFrom(raw)
        || Modifier.isAbstract(raw.getModifiers())) return null;

    var params = TypeUtil.resolveSupertypeParameters(type, Collection.class);
    assert params != null;

    var elementType = params.get(TypeVariables.COLLECTION_ELEMENT_TYPE);
    if (elementType == null) return null;

    var elementCodec = root.resolve(elementType, field);
    if (elementCodec == null) return null;

    MethodHandle ctor;
    try {
      ctor = MethodHandles.publicLookup().findConstructor(raw, MethodType.methodType(void.class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      return null;
    }

    return new CollectionCodec<>((Codec<Object>) elementCodec, () -> construct(ctor));
  }

  @SuppressWarnings("unchecked")
  private static Collection<Object> construct(MethodHandle ctor) {
    try {
      return (Collection<Object>) ctor.invoke();
    } catch (Throwable e) {
      throw new CodecException("Problem invoking collection constructor", e);
    }
  }
}
