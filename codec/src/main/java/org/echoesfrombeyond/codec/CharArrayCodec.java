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
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import java.util.Arrays;
import org.bson.BsonArray;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.jspecify.annotations.NullMarked;

/** {@link Codec} for an array of primitive char. */
@NullMarked
class CharArrayCodec implements Codec<char[]>, RawJsonCodec<char[]> {
  private static final char[] EMPTY_CHAR_ARRAY = new char[0];

  /** Creates a new instance of this class. */
  CharArrayCodec() {}

  public char[] decode(BsonValue bsonValue, ExtraInfo extraInfo) {
    var array = bsonValue.asArray();
    var result = new char[array.size()];

    for (int i = 0; i < result.length; ++i) {
      var character = CodecUtil.CHARACTER_CODEC.decode(array.get(i), extraInfo);
      assert character != null;

      result[i] = character;
    }
    return result;
  }

  public BsonValue encode(char[] values, ExtraInfo extraInfo) {
    var array = new BsonArray();
    for (var value : values) array.add(new BsonString(Character.toString(value)));
    return array;
  }

  public char[] decodeJson(RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
    reader.expect('[');
    reader.consumeWhiteSpace();
    if (reader.tryConsume(']')) return EMPTY_CHAR_ARRAY;

    var i = 0;
    var result = new char[10];

    while (true) {
      if (i == result.length) {
        var temp = new char[i + 1 + (i >> 1)];
        System.arraycopy(result, 0, temp, 0, i);
        result = temp;
      }

      var character = CodecUtil.CHARACTER_CODEC.decodeJson(reader, extraInfo);
      assert character != null;

      result[i++] = character;
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
    schema.setTitle("CharacterArray");
    schema.setItem(context.refDefinition(CodecUtil.CHARACTER_CODEC));

    return schema;
  }
}
