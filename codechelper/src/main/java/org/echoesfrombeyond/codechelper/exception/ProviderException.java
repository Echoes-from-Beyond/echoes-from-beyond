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

package org.echoesfrombeyond.codechelper.exception;

import org.echoesfrombeyond.codechelper.provider.Provider;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@NullMarked
@ApiStatus.Internal
public class ProviderException extends Exception {
  private static String formatMessage(Class<?> providerType, String message) {
    return String.format("Provider class: %s\n%s", providerType.getName(), message);
  }

  public ProviderException(Class<?> provider, String message) {
    super(formatMessage(provider, message));
  }

  public ProviderException(
      Class<? extends Provider<?, ?>> provider, String message, Throwable cause) {
    super(formatMessage(provider, message), cause);
  }
}
