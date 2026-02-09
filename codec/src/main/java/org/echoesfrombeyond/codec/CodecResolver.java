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
import java.util.Collection;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@FunctionalInterface
public interface CodecResolver {
  @Nullable Codec<?> resolve(Type type, Field field);

  @ApiStatus.NonExtendable
  default CodecResolver chain(CodecResolver other) {
    return new ChainedResolver(this, other);
  }

  @ApiStatus.NonExtendable
  default CodecResolver withCollectionSupport(
      ImplementationProvider<Collection<?>> implementationProvider) {
    var chained = new ChainedResolver(this);
    chained.append(new CollectionResolver(chained, implementationProvider));
    return chained;
  }

  @ApiStatus.NonExtendable
  default CodecResolver withRecursiveResolution() {
    var chained = new ChainedResolver(this);
    chained.append(new RecursiveResolver(chained));
    return chained;
  }
}
