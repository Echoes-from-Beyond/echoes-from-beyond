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

package org.echoesfrombeyond.util.thread;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class OnceTest {
  @Test
  public void reentrantInvocationThrows() {
    Once<?>[] ref = new Once[1];
    ref[0] = Once.of(() -> ref[0].get());

    assertThrows(IllegalStateException.class, () -> ref[0].get());
  }

  @Test
  public void supplierIsOnlyCalledOnce() {
    AtomicInteger count = new AtomicInteger();
    Once<Object> once =
        Once.of(
            () -> {
              count.incrementAndGet();
              return new Object();
            });

    assertEquals(0, count.get());
    Object o = once.get();
    assertEquals(1, count.get());
    Object o2 = once.get();
    assertEquals(1, count.get());
    assertSame(o, o2);
  }

  @Test
  public void supplierCanReturnNull() {
    AtomicInteger count = new AtomicInteger();
    Once<Object> once =
        Once.of(
            () -> {
              count.getAndIncrement();
              return null;
            });

    assertEquals(0, count.get());
    assertNull(once.get());
    assertEquals(1, count.get());
    assertNull(once.get());
    assertEquals(1, count.get());
  }
}
