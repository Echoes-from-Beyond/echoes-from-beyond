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
import javassist.ClassPool;
import javassist.CtClass;
import javassist.Modifier;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

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

      try {
        injectPluginManager();
      } catch (Throwable e) {
        throw new RuntimeException(e);
      }

      Main.main(new String[] {"--disable-sentry", "--assets", assetZip});
    } finally {
      // signal waiting threads that we booted the server
      BOOTED.countDown();
    }
  }

  /**
   * Injects {@link com.hypixel.hytale.server.core.plugin.PluginManager} to prevent it from failing
   * launch on duplicate plugins, among other things.
   *
   * @throws Exception if there was a problem injecting or compiling code
   */
  private static void injectPluginManager() throws Exception {
    var pool = ClassPool.getDefault();

    var CT_MAP = pool.get("java.util.Map");
    var CT_PATH = pool.get("java.nio.file.Path");
    var CT_PLUGIN_MANAGER = pool.get("com.hypixel.hytale.server.core.plugin.PluginManager");
    var CT_PENDING_LOAD_PLUGIN =
        pool.get("com.hypixel.hytale.server.core.plugin.pending.PendingLoadPlugin");

    var CT_PLUGIN_CLASS_LOADER =
        pool.get("com.hypixel.hytale.server.core.plugin.PluginClassLoader");
    CT_PLUGIN_CLASS_LOADER.getDeclaredField("childFirst").setModifiers(Modifier.PUBLIC);
    CT_PLUGIN_CLASS_LOADER.toClass(com.hypixel.hytale.server.core.plugin.PluginType.class);

    var loadPluginsInClasspath =
        CT_PLUGIN_MANAGER.getDeclaredMethod(
            "loadPluginsInClasspath", new CtClass[] {CT_MAP, CT_MAP});

    var loadPluginsFromDirectory =
        CT_PLUGIN_MANAGER.getDeclaredMethod(
            "loadPluginsFromDirectory",
            new CtClass[] {CT_MAP, CT_PATH, CtClass.booleanType, CT_MAP});

    var loadPendingPlugin =
        CT_PLUGIN_MANAGER.getDeclaredMethod(
            "loadPendingPlugin", new CtClass[] {CT_MAP, CT_PENDING_LOAD_PLUGIN});

    loadPluginsInClasspath.insertAfter(
        """
        {
          java.util.Iterator classloaderIterator = $0.classLoaders.values().iterator();
          while (classloaderIterator.hasNext()) {
            ClassLoader next = (ClassLoader) classloaderIterator.next();
            if (next.getName().startsWith("BuiltinPlugin(org.echoesfrombeyond")) {
              LOGGER.at(java.util.logging.Level.WARNING).log(">>INJECTOR<< Found unwanted classloader " + next.getName());
              classloaderIterator.remove();
            }
          }

          $0.classpathAssetPacks.clear();
        }
        """);

    loadPluginsFromDirectory.insertAfter(
        """
        {
          java.util.Iterator classloaderIterator = $0.classLoaders.entrySet().iterator();

          while (classloaderIterator.hasNext()) {
            java.util.Map.Entry next = (java.util.Map.Entry) classloaderIterator.next();

            java.nio.file.Path key = (java.nio.file.Path) next.getKey();
            com.hypixel.hytale.server.core.plugin.PluginClassLoader value =
              (com.hypixel.hytale.server.core.plugin.PluginClassLoader) next.getValue();

            if (key.getFileName().toString().endsWith(".jar") &&
              value.getName().startsWith("ThirdParty(org.echoesfrombeyond")) {
              value.childFirst = true;
            }
          }
        }
        """);

    loadPendingPlugin.insertBefore(
        """
        {
          if ($2.getIdentifier().getGroup().equals("org.echoesfrombeyond") &&
            (!$2.getPath().getFileName().toString().endsWith(".jar"))) {
            LOGGER.at(java.util.logging.Level.WARNING).log(">>INJECTOR<< Found unwanted classpath plugin!");
            return;
          }
        }
        """);

    CT_PLUGIN_MANAGER.toClass(com.hypixel.hytale.server.core.plugin.PluginType.class);
  }
}
