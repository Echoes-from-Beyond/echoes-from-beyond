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

import org.echoesfrombeyond.codechelper.CodecResolver;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

/** Exception thrown by {@link CodecUtil#modelBuilder(Class, CodecResolver)} and overloads. */
@ApiStatus.Internal
@NullMarked
public class ModelException extends IllegalArgumentException {
  private static String formatMessage(Class<?> modelType, String message) {
    return String.format("Model class: %s\n%s", modelType.getName(), message);
  }

  /**
   * @param modelType the model type, typically annotated with {@link ModelBuilder}
   * @param message the message string
   */
  public ModelException(Class<?> modelType, String message) {
    super(formatMessage(modelType, message));
  }

  /**
   * @param modelType the model type, typically annotated with {@link ModelBuilder}
   * @param message the message string
   * @param cause the cause of this exception
   */
  public ModelException(Class<?> modelType, String message, Throwable cause) {
    super(formatMessage(modelType, message), cause);
  }
}
