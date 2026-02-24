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

package org.echoesfrombeyond.util.iterable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class IterableUtil {
  private static final Iterator<?> EMPTY_ITERATOR =
      new Iterator<>() {
        @Override
        public boolean hasNext() {
          return false;
        }

        @Override
        public Object next() {
          throw new IllegalArgumentException();
        }
      };

  @SuppressWarnings("unchecked")
  public static <T> Iterator<T> emptyIterator() {
    return (Iterator<T>) EMPTY_ITERATOR;
  }

  public static <T extends @Nullable Object> Iterable<T> onceIterable(T value) {
    return () ->
        new Iterator<>() {
          private boolean iterated;

          @Override
          public boolean hasNext() {
            return !iterated;
          }

          @Override
          public T next() {
            if (iterated) throw new NoSuchElementException();
            iterated = true;
            return value;
          }
        };
  }
}
