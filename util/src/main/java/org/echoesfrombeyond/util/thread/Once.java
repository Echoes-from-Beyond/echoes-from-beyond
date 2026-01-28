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

import java.util.function.Supplier;
import org.echoesfrombeyond.util.Check;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A thread-safe {@link Supplier} that only initializes a value once, and caches it for subsequent
 * invocations of {@link Once#get()}. This is ideal for e.g. statically initialized values and lazy
 * suppliers that might be accessed by multiple threads.
 *
 * <p>Instances can be created from an arbitrary {@link Supplier} by calling {@link
 * Once#of(Supplier)}. The supplier is <i>guaranteed</i> to either have never been called or to be
 * called exactly once.
 *
 * <p>If a supplier throws an exception, it will not be called again. Other calls to {@link
 * Once#get()} will themselves throw an {@link IllegalStateException}.
 *
 * <p>{@code get} may not be invoked reentrantly. Attempting to do so will cause an {@link
 * IllegalStateException} to be thrown.
 *
 * @param <T> the type returned by the internal supplier, which may include {@code null}.
 */
@NullMarked
public final class Once<T extends @Nullable Object> implements Supplier<T> {
  private enum State {
    CALLING_SUPPLIER,
    EXCEPTION_THROWN,
    NORMAL
  }

  // Sentinel object used to represent an uninitialized value. This is used instead of null because
  // `supplier` might return null, and that is a valid value. UNINIT isn't, because external code
  // cannot access it normally.
  private static final Object UNINIT = new Object();

  private final Supplier<T> supplier;
  private final Object lock;

  private T value;

  private State state;

  /**
   * Returns an instance of this class.
   *
   * <p>If {@code getter} is already a {@link Once}, it is returned as-is.
   *
   * @param getter the getter {@link Supplier}
   * @return a Once instance
   * @param <T> the type returned by `getter`
   * @throws NullPointerException if {@code getter} is null
   */
  public static <T extends @Nullable Object> Once<T> of(Supplier<T> getter) {
    Check.nonNull(getter);

    if (getter instanceof Once<T> once) return once;
    return new Once<>(getter);
  }

  @SuppressWarnings("unchecked")
  private Once(Supplier<T> supplier) {
    this.supplier = supplier;
    this.lock = new Object();

    // If value is UNINIT, we never return it, so this shouldn't cause heap pollution
    this.value = (T) UNINIT;
    this.state = State.NORMAL;
  }

  @Override
  public T get() {
    T current = value;
    if (current != UNINIT) return current;

    synchronized (lock) {
      if (state == State.CALLING_SUPPLIER)
        throw new IllegalStateException("get() may not be invoked reentrantly");

      if (state == State.EXCEPTION_THROWN)
        throw new IllegalStateException("the supplier threw an exception");

      current = value;
      if (current != UNINIT) return current;

      state = State.CALLING_SUPPLIER;
      try {
        current = value = supplier.get();
        state = State.NORMAL;
      } finally {
        if (state == State.CALLING_SUPPLIER) state = State.EXCEPTION_THROWN;
      }
    }

    return current;
  }
}
