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

package org.echoesfrombeyond.codechelper.annotation;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import java.lang.annotation.*;
import org.jspecify.annotations.NullMarked;

/**
 * This annotation can be applied to a {@link ModelBuilder} class to specify a codec version for it.
 *
 * <p>This is equivalent to calling {@link BuilderCodec.Builder#codecVersion(int, int)}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NullMarked
public @interface CodecVersion {
  /**
   * @return the minimum version; if unspecified defaults to {@code 0}
   */
  int min() default 0;

  /**
   * @return the codec version
   */
  int value();
}
