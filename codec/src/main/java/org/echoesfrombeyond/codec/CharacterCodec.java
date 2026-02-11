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
import com.hypixel.hytale.codec.PrimitiveCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.jspecify.annotations.NullMarked;

/** Codec serializing a character as a {@link String} of length 1. */
@NullMarked
class CharacterCodec implements Codec<Character>, PrimitiveCodec {
  /** Creates a new instance of this class. */
  CharacterCodec() {}

  private static Character convertValue(String value) {
    if (value.length() != 1) throw new IllegalArgumentException("Expected a string of length 1");

    return value.charAt(0);
  }

  @Override
  public Character decode(BsonValue bsonValue, ExtraInfo extraInfo) {
    return convertValue(bsonValue.asString().getValue());
  }

  @Override
  public BsonValue encode(Character value, ExtraInfo extraInfo) {
    return new BsonString(Character.toString(value));
  }

  @Override
  public Character decodeJson(RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
    return convertValue(reader.readString());
  }

  @Override
  public Schema toSchema(SchemaContext schemaContext) {
    var schema = new StringSchema();
    schema.setTitle("Character");
    schema.setMinLength(1);
    schema.setMaxLength(1);

    return schema;
  }
}
