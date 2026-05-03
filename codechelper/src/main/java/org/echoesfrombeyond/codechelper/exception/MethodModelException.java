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

import java.lang.reflect.Method;
import org.echoesfrombeyond.codechelper.CodecResolver;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

/** Exception thrown by {@link CodecUtil#modelBuilder(Class, CodecResolver)} and overloads. */
@ApiStatus.Internal
@NullMarked
public class MethodModelException extends ModelException {
  private static String formatMessage(Method method, String message) {
    return String.format("Model method: %s\n%s", method, message);
  }

  /**
   * @param modelType the model type
   * @param method the method associated with this exception
   * @param message the error message
   */
  public MethodModelException(Class<?> modelType, Method method, String message) {
    super(modelType, formatMessage(method, message));
  }

  /**
   * @param modelType the model type
   * @param method the method associated with this exception
   * @param message the error message
   * @param cause the error cause
   */
  public MethodModelException(Class<?> modelType, Method method, String message, Throwable cause) {
    super(modelType, formatMessage(method, message), cause);
  }
}
