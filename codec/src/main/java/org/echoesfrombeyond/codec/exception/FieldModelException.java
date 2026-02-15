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

package org.echoesfrombeyond.codec.exception;

import java.lang.reflect.Field;
import org.echoesfrombeyond.codec.CodecResolver;
import org.echoesfrombeyond.codec.CodecUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

/** Exception thrown by {@link CodecUtil#modelBuilder(Class, CodecResolver)} and overloads. */
@ApiStatus.Internal
@NullMarked
public class FieldModelException extends ModelException {
  private static String formatMessage(Field field, String message) {
    return String.format("Model field: %s\n%s", field, message);
  }

  /**
   * @param modelType the model type
   * @param field the field associated with this exception
   * @param message the error message
   */
  public FieldModelException(Class<?> modelType, Field field, String message) {
    super(modelType, formatMessage(field, message));
  }
}
