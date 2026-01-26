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

package org.echoesfrombeyond.ui.hud;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.entity.entities.player.hud.HudManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import org.echoesfrombeyond.Plugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Utilities for working with {@link CustomUIHud}s. Some utility methods will use MHUD if it is
 * available on the classpath.
 */
@NullMarked
public final class HudUtils {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  private static final class MHUD {
    private static final @Nullable Class<?> MHUD;
    private static final @Nullable Class<?> MHUD_MCUIH;

    private static final String GET_INSTANCE = "getInstance";
    private static final String GET_CUSTOM_HUDS = "getCustomHuds";
    private static final String SET_CUSTOM_HUD = "setCustomHud";
    private static final String HIDE_CUSTOM_HUD = "hideCustomHud";

    static {
      Class<?> mhud = null;
      Class<?> mhudMcuih = null;

      try {
        mhud = Class.forName("com.buuz135.mhud.MultipleHUD");
        mhudMcuih = Class.forName("com.buuz135.mhud.MultipleCustomUIHud");
      } catch (ClassNotFoundException ignored) {
        LOGGER.atInfo().log("MHUD not found on classpath; integration is not enabled");
      }

      if (mhud == null || mhudMcuih == null) MHUD = MHUD_MCUIH = null;
      else {
        MHUD = mhud;
        MHUD_MCUIH = mhudMcuih;

        LOGGER.atInfo().log("MHUD found on classpath; integration enabled");
      }
    }

    private static @Nullable Object getInstance() {
      try {
        assert MHUD != null;

        var instance = MHUD.getMethod(GET_INSTANCE).invoke(null);
        if (instance == null) logReflectNull(GET_INSTANCE);

        return instance;
      } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        logReflectFail(GET_INSTANCE, e);
      }

      return null;
    }

    private static void logReflectFail(String methodName, Object err) {
      LOGGER.atWarning().log("MHUD: Could not reflect `%s`: %s", methodName, err);
    }

    private static void logReflectNull(String methodName) {
      LOGGER.atWarning().log("MHUD: `%s` returned null", methodName);
    }

    private static String hudKey(Class<?> hudClass) {
      return Plugin.MOD_GROUP + ":" + hudClass.getName();
    }
  }

  private HudUtils() {}

  /**
   * Gets the HUD, if there is one of the specified type. If MHUD is installed, it will look up the
   * HUD class from {@code MultipleCustomUIHud}.
   *
   * @param hudClass the hud class
   * @param manager the manager
   * @return the hud
   * @param <T> the type of hud, or {@code null} if there was an error, or none could be found
   */
  public static <T extends CustomUIHud> @Nullable T getHud(Class<T> hudClass, HudManager manager) {
    var currentHud = manager.getCustomHud();
    if (currentHud == null) return null;

    if (MHUD.MHUD_MCUIH != null && MHUD.MHUD_MCUIH.isAssignableFrom(currentHud.getClass())) {
      try {
        var map = MHUD.MHUD_MCUIH.getMethod(MHUD.GET_CUSTOM_HUDS).invoke(currentHud);

        if (map instanceof Map<?, ?> hudMap) {
          var key = MHUD.hudKey(hudClass);

          var result = hudMap.get(key);
          if (result == null) return null;

          if (result instanceof CustomUIHud customUIHud) currentHud = customUIHud;
          else {
            LOGGER.atWarning().log(
                "MHUD: `%s` map contained unexpected value type %s at key %s",
                MHUD.GET_CUSTOM_HUDS, result.getClass().getName(), key);
          }
        } else if (map != null) {
          LOGGER.atWarning().log(
              "MHUD: `%s` returned unexpected type %s",
              MHUD.GET_CUSTOM_HUDS, map.getClass().getName());
        } else {
          MHUD.logReflectNull(MHUD.GET_CUSTOM_HUDS);
        }
      } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        MHUD.logReflectFail(MHUD.GET_CUSTOM_HUDS, e);
      }
    }

    if (hudClass.isAssignableFrom(currentHud.getClass())) return hudClass.cast(currentHud);
    else return null;
  }

  /**
   * Shows {@code hud} to the given player. Will use MHUD if it is available on the classpath.
   *
   * @param player the player
   * @param playerRef the player reference
   * @param hud the HUD to show
   */
  public static void showHud(Player player, PlayerRef playerRef, CustomUIHud hud) {
    Object instance;
    if (MHUD.MHUD != null && (instance = MHUD.getInstance()) != null) {
      try {
        instance
            .getClass()
            .getMethod(
                MHUD.SET_CUSTOM_HUD, Player.class, PlayerRef.class, String.class, CustomUIHud.class)
            .invoke(instance, player, playerRef, MHUD.hudKey(hud.getClass()), hud);

        return;
      } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        MHUD.logReflectFail(MHUD.SET_CUSTOM_HUD, e);
      }
    }

    player.getHudManager().setCustomHud(playerRef, hud);
  }

  /**
   * Hides the HUD of the specified type. Uses MHUD if it is available.
   *
   * @param hudClass the class of hud to hide
   * @param player the player
   * @param playerRef the player reference
   * @param <T> the hud type
   */
  public static <T extends CustomUIHud> void hideHud(
      Class<T> hudClass, Player player, PlayerRef playerRef) {
    Object instance;
    if (MHUD.MHUD != null && (instance = MHUD.getInstance()) != null) {
      try {
        instance
            .getClass()
            .getMethod(MHUD.HIDE_CUSTOM_HUD, Player.class, PlayerRef.class, String.class)
            .invoke(instance, player, playerRef, MHUD.hudKey(hudClass));

        return;
      } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        MHUD.logReflectFail(MHUD.HIDE_CUSTOM_HUD, e);
      }
    }

    player.getHudManager().setCustomHud(playerRef, new EmptyHud(playerRef));
  }
}
