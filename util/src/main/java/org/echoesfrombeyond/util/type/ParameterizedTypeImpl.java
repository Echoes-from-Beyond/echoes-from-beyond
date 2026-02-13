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

package org.echoesfrombeyond.util.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
class ParameterizedTypeImpl implements ParameterizedType {
  private final @Nullable Type owner;
  private final Type raw;
  private final Type[] typeArguments;

  ParameterizedTypeImpl(@Nullable Type owner, Type raw, Type[] typeArguments) {
    this.owner = owner;
    this.raw = raw;
    this.typeArguments = typeArguments;
  }

  @Override
  public Type[] getActualTypeArguments() {
    return Arrays.copyOf(typeArguments, typeArguments.length);
  }

  @Override
  public Type getRawType() {
    return raw;
  }

  @Override
  public @Nullable Type getOwnerType() {
    return owner;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;

    return obj instanceof ParameterizedType other
        && Objects.equals(raw, other.getRawType())
        && Objects.equals(other.getOwnerType(), owner)
        && Arrays.equals(other.getActualTypeArguments(), typeArguments);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(typeArguments) ^ Objects.hashCode(owner) ^ Objects.hashCode(raw);
  }
}
