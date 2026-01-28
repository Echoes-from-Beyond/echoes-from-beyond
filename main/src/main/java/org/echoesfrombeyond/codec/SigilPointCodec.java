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

package org.echoesfrombeyond.codec;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.RawJsonCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import java.util.regex.Pattern;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.echoesfrombeyond.asset.SigilPattern;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * {@link Codec} used to encode and decode points in a {@link SigilPattern}.
 *
 * @see SigilPointCodec#SIGIL_POINT_PATTERN for details on string validation
 */
@ApiStatus.Internal
@NullMarked
public final class SigilPointCodec implements Codec<SigilPoint>, RawJsonCodec<SigilPoint> {
  /**
   * Pattern used to validate a {@link SigilPoint}.
   *
   * <p>Valid points are strings containing two single-digit numbers, each ranging from 0 to 3 (both
   * inclusive), separated by a comma, and any number of tabs or spaces before and after each
   * number.
   */
  public static final Pattern SIGIL_POINT_PATTERN =
      Pattern.compile("^[ \t]*[0123][ \t]*,[ \t]*[0123][ \t]*$");

  SigilPointCodec() {}

  @Override
  public SigilPoint decode(BsonValue bsonValue, ExtraInfo extraInfo) {
    var value = bsonValue.asString().getValue();
    var split = value.split(",", 2);

    var x = Integer.parseInt(split[0].strip());
    var y = Integer.parseInt(split[1].strip());

    return new SigilPoint(x, y);
  }

  @Override
  public BsonValue encode(SigilPoint sigilPoint, ExtraInfo extraInfo) {
    return new BsonString(sigilPoint.x() + ", " + sigilPoint.y());
  }

  @Override
  public Schema toSchema(SchemaContext schemaContext) {
    return toSchema0(null);
  }

  @Override
  public Schema toSchema(SchemaContext context, @Nullable SigilPoint def) {
    return toSchema0(def);
  }

  private static Schema toSchema0(@Nullable SigilPoint def) {
    var schema = new StringSchema();
    schema.setPattern(SIGIL_POINT_PATTERN);
    schema.setTitle("Sigil Point");

    if (def != null) {
      schema.setDefault(def.x() + ", " + def.y());
    }

    return schema;
  }
}
