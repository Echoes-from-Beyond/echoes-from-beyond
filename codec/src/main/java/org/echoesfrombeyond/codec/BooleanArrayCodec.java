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
import com.hypixel.hytale.codec.schema.config.ArraySchema;
import com.hypixel.hytale.codec.schema.config.BooleanSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import java.util.Arrays;
import org.bson.BsonArray;
import org.bson.BsonBoolean;
import org.bson.BsonValue;
import org.jspecify.annotations.NullMarked;

/** {@link Codec} for an array of primitive boolean. */
@NullMarked
class BooleanArrayCodec implements Codec<boolean[]>, RawJsonCodec<boolean[]> {
  private static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];

  /** Creates a new instance of this class. */
  BooleanArrayCodec() {}

  public boolean[] decode(BsonValue bsonValue, ExtraInfo extraInfo) {
    var array = bsonValue.asArray();
    var result = new boolean[array.size()];

    for (int i = 0; i < result.length; i++) result[i] = array.get(i).asBoolean().getValue();

    return result;
  }

  public BsonValue encode(boolean[] values, ExtraInfo extraInfo) {
    var array = new BsonArray();
    for (boolean bool : values) array.add(new BsonBoolean(bool));
    return array;
  }

  public boolean[] decodeJson(RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
    reader.expect('[');
    reader.consumeWhiteSpace();
    if (reader.tryConsume(']')) return EMPTY_BOOLEAN_ARRAY;

    var i = 0;
    var result = new boolean[10];

    while (true) {
      if (i == result.length) {
        var temp = new boolean[i + 1 + (i >> 1)];
        System.arraycopy(result, 0, temp, 0, i);
        result = temp;
      }

      result[i++] = reader.readBooleanValue();
      reader.consumeWhiteSpace();
      if (reader.tryConsumeOrExpect(']', ',')) {
        if (result.length == i) return result;
        else return Arrays.copyOf(result, i);
      }

      reader.consumeWhiteSpace();
    }
  }

  public Schema toSchema(SchemaContext context) {
    var schema = new ArraySchema();
    schema.setItem(new BooleanSchema());
    return schema;
  }
}
