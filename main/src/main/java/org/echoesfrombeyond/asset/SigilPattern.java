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

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.validation.Validators;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.echoesfrombeyond.codec.SigilPoint;
import org.echoesfrombeyond.codec.validation.CustomValidators;
import org.echoesfrombeyond.sigil.SigilKey;
import org.echoesfrombeyond.sigil.SigilValidation;
import org.echoesfrombeyond.util.Check;
import org.echoesfrombeyond.util.thread.Once;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A Sigil pattern. Call {@link SigilPattern#getSigilKey()} to access the canonicalized sigil.
 *
 * @see SigilValidation for canonicalization details
 */
@NullMarked
public class SigilPattern
    implements JsonAssetWithMap<String, DefaultAssetMap<String, SigilPattern>> {
  /** Asset store supplier. This should not be called until after plugin setup. */
  public static final Once<AssetStore<String, SigilPattern, DefaultAssetMap<String, SigilPattern>>>
      ASSET_STORE = Once.of(() -> Check.nonNull(AssetRegistry.getAssetStore(SigilPattern.class)));

  /** The codec. */
  public static final AssetBuilderCodec<String, SigilPattern> CODEC;

  static {
    CODEC =
        AssetBuilderCodec.builder(
                SigilPattern.class,
                SigilPattern::new,
                Codec.STRING,
                (sigil, id) -> sigil.id = id,
                SigilPattern::getId,
                (sigil, data) -> sigil.data = data,
                (sigil) -> sigil.data)
            .documentation(
                "A Sigil pattern, which only consists of an identifier and list of points.")
            .append(
                new KeyedCodec<>("Points", SigilPoint.ARRAY_CODEC, true),
                (self, points) -> self.points = SigilPoint.encodeArray(points),
                (self) -> SigilPoint.decodeArray(self.points))
            .documentation("An array of strings containing comma-separated pairs of numbers.")
            .addValidator(Validators.nonNull())
            .addValidator(Validators.arraySizeRange(2, SigilValidation.MAX_SIGIL_LENGTH))
            .addValidator(CustomValidators.canonicalSigil())
            .add()
            .build();
  }

  private @Nullable String id;
  private AssetExtraInfo.@Nullable Data data;

  private byte @Nullable [] points;

  private final AtomicReference<@Nullable SigilKey> keyCache;

  private SigilPattern() {
    this.keyCache = new AtomicReference<>(null);
  }

  private Optional<SigilKey> canonicalizeAndCache() {
    SigilKey value = keyCache.get();

    if (value != null) return Optional.of(value);
    if (points == null) return Optional.empty();

    Optional<SigilKey> attempt = SigilValidation.canonicalize(points);
    attempt.ifPresent(key -> keyCache.compareAndSet(null, key));

    return attempt;
  }

  /**
   * Gets the canonicalized {@link SigilKey} instance.
   *
   * @return the canonical key
   * @throws IllegalStateException if this asset was serialized from invalid data
   */
  public SigilKey getSigilKey() {
    return canonicalizeAndCache()
        .orElseThrow(() -> new IllegalStateException(SigilValidation.NON_CANONICAL_ERR_MSG));
  }

  @Override
  public String getId() {
    return Check.nonNull(id);
  }
}
