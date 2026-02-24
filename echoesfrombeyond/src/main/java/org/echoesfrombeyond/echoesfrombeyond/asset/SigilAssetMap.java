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

package org.echoesfrombeyond.asset;

import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.assetstore.codec.AssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;
import org.echoesfrombeyond.sigil.SigilKey;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Specialized {@link AssetMap} for {@link SigilPattern}s. Adds {@link
 * SigilAssetMap#getSigilPattern(SigilKey)} for efficiently retrieving pattern assets from a
 * canonical key.
 */
@NullMarked
public class SigilAssetMap extends DefaultAssetMap<String, SigilPattern> {
  private final StampedLock patternLock;
  private final Map<SigilKey, SigilPattern> patternMap;

  /** Creates a new instance of this class, that contains no sigil patterns initially. */
  public SigilAssetMap() {
    this.patternLock = new StampedLock();
    this.patternMap = new ConcurrentHashMap<>();
  }

  @Override
  protected void putAll(
      String packKey,
      AssetCodec<String, SigilPattern> codec,
      Map<String, SigilPattern> loadedAssets,
      Map<String, Path> loadedKeyToPathMap,
      Map<String, Set<String>> loadedAssetChildren) {
    super.putAll(packKey, codec, loadedAssets, loadedKeyToPathMap, loadedAssetChildren);

    var stamp = patternLock.writeLock();
    try {
      for (var pattern : loadedAssets.values()) patternMap.put(pattern.getSigilKey(), pattern);
    } finally {
      patternLock.unlockWrite(stamp);
    }
  }

  @Override
  protected void clear() {
    super.clear();

    var stamp = patternLock.writeLock();
    try {
      patternMap.clear();
    } finally {
      patternLock.unlockWrite(stamp);
    }
  }

  /**
   * Gets a {@link SigilPattern} given {@code key}.
   *
   * @param key the {@link SigilKey} used to look up a pattern
   * @return the pattern; or {@code null} if it does not exist
   */
  public @Nullable SigilPattern getSigilPattern(SigilKey key) {
    var read = patternLock.tryOptimisticRead();
    var value = patternMap.get(key);
    if (patternLock.validate(read)) return value;

    read = patternLock.readLock();
    try {
      return patternMap.get(key);
    } finally {
      patternLock.unlockRead(read);
    }
  }

  private void remove0(Set<String> toRemove) {
    var stamp = patternLock.writeLock();
    try {
      patternMap.values().removeIf(value -> toRemove.contains(value.getId()));
    } finally {
      patternLock.unlockWrite(stamp);
    }
  }

  @Override
  protected Set<String> remove(Set<String> keys) {
    remove0(keys);
    return super.remove(keys);
  }

  @Override
  protected Set<String> remove(
      String packKey, Set<String> keys, List<Map.Entry<String, Object>> pathsToReload) {
    remove0(keys);
    return super.remove(packKey, keys, pathsToReload);
  }
}
