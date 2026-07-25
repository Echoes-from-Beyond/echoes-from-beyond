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

package org.echoesfrombeyond.test;

import com.hypixel.hytale.Main;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.extension.*;

@NullMarked
public class HytaleIntegrationTest implements BeforeAllCallback {
  private static final AtomicBoolean INIT = new AtomicBoolean(false);
  private static final CountDownLatch BOOTED = new CountDownLatch(1);

  @Override
  public void beforeAll(ExtensionContext context) {
    if (!INIT.compareAndSet(false, true)) {
      try {
        // server has maybe not started yet, wait for it
        BOOTED.await();
      } catch (InterruptedException _) {
      }
      return;
    }

    var assetZip = System.getProperty("org.echoesfrombeyond.assets-zip");

    try {
      if (assetZip == null)
        throw new IllegalStateException(
            "Property org.echoesfrombeyond.assets-zip must be set when using the Hytale test"
                + " extension");

      Main.main(new String[] {"--disable-sentry", "--assets", assetZip});
    } finally {
      // signal waiting threads that we booted the server
      BOOTED.countDown();
    }
  }
}
