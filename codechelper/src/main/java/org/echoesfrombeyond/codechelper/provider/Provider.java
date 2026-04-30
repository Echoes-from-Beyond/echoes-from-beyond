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

package org.echoesfrombeyond.codechelper.provider;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.echoesfrombeyond.codechelper.exception.ProviderException;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface Provider<Instance, Args> {
  Class<Args> getArgsType();

  @Nullable Instance getInstance(Args args, Field field);

  @ApiStatus.Internal
  static <I, A, P extends Provider<I, A>> I provide(P provider, Object args, Field field)
      throws ProviderException {
    try {
      I result = provider.getInstance(provider.getArgsType().cast(args), field);
      if (result == null)
        throw new ProviderException(
            provider.getClass(), "Provider must be able to provide for the field");

      return result;
    } catch (ClassCastException _) {
      throw new ProviderException(
          provider.getClass(),
          "Could not cast argument class "
              + args.getClass().getSimpleName()
              + " to argument type "
              + provider.getArgsType().getSimpleName());
    }
  }

  @ApiStatus.Internal
  static <P extends Provider<?, ?>> P loadSingleton(Class<P> cls) throws ProviderException {
    Field field;

    try {
      field = cls.getDeclaredField("INSTANCE");
    } catch (NoSuchFieldException e) {
      throw new ProviderException(cls, "Missing INSTANCE field", e);
    }

    int modifiers = field.getModifiers();
    if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers))
      throw new ProviderException(cls, "INSTANCE field is not public static");

    Object instanceFieldRaw;
    try {
      instanceFieldRaw = field.get(null);
    } catch (IllegalAccessException e) {
      throw new ProviderException(cls, "Couldn't access INSTANCE field", e);
    }

    if (instanceFieldRaw == null) throw new ProviderException(cls, "INSTANCE field was null");

    if (!cls.isAssignableFrom(instanceFieldRaw.getClass()))
      throw new ProviderException(cls, "INSTANCE field was not assignable to the class type");

    return cls.cast(instanceFieldRaw);
  }
}
