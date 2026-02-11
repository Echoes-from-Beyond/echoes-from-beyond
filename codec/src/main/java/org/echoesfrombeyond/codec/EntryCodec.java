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
import com.hypixel.hytale.codec.exception.CodecException;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ObjectSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import java.util.Map;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
class EntryCodec<Key extends @Nullable Object, Value extends @Nullable Object>
    implements Codec<Entry<Key, Value>> {
  private final Codec<Key> keyCodec;
  private final Codec<Value> valueCodec;
  private final String keyName;
  private final String valueName;

  EntryCodec(Codec<Key> keyCodec, Codec<Value> valueCodec, String keyName, String valueName) {
    this.keyCodec = keyCodec;
    this.valueCodec = valueCodec;
    this.keyName = keyName;
    this.valueName = valueName;
  }

  @Override
  public Entry<Key, Value> decode(BsonValue bsonValue, ExtraInfo extraInfo) {
    var document = bsonValue.asDocument();

    var decodedKey = decodeKeyOrValue(keyCodec, keyName, document.get(keyName), extraInfo);
    var decodedValue = decodeKeyOrValue(valueCodec, valueName, document.get(valueName), extraInfo);

    return new Entry<>(decodedKey, decodedValue);
  }

  @Override
  public BsonValue encode(Entry<Key, Value> keyValueEntry, ExtraInfo extraInfo) {
    var document = new BsonDocument();
    encodeKeyOrValue(document, keyCodec, keyName, keyValueEntry.key(), extraInfo);
    encodeKeyOrValue(document, valueCodec, valueName, keyValueEntry.value(), extraInfo);
    return document;
  }

  private static <T extends @Nullable Object> void encodeKeyOrValue(
      BsonDocument document, Codec<T> codec, String name, T value, ExtraInfo extraInfo) {
    extraInfo.pushKey(name);

    try {
      document.put(name, codec.encode(value, extraInfo));
    } finally {
      extraInfo.popKey();
    }
  }

  private static <T extends @Nullable Object> T decodeKeyOrValue(
      Codec<T> codec, String name, @Nullable BsonValue value, ExtraInfo extraInfo) {
    extraInfo.pushKey(name);

    try {
      return codec.decode(value, extraInfo);
    } finally {
      extraInfo.popKey();
    }
  }

  private static <T extends @Nullable Object> T decodeKeyOrValueJson(
      Codec<T> codec, String name, RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
    extraInfo.pushKey(name);

    try {
      return codec.decodeJson(reader, extraInfo);
    } finally {
      extraInfo.popKey();
    }
  }

  @Override
  public @Nullable Entry<Key, Value> decodeJson(RawJsonReader reader, ExtraInfo extraInfo)
      throws IOException {
    reader.expect('{');
    reader.consumeWhiteSpace();

    var firstString = reader.readString();

    var firstIsKey = firstString.equals(keyName);
    if (!firstIsKey && !firstString.equals(valueName))
      throw new CodecException("Unexpected key/value name " + firstString, reader, extraInfo, null);

    reader.consumeWhiteSpace();
    reader.expect(':');
    reader.consumeWhiteSpace();

    Key decodedKey = null;
    Value decodedValue = null;

    if (firstIsKey) decodedKey = decodeKeyOrValueJson(keyCodec, keyName, reader, extraInfo);
    else decodedValue = decodeKeyOrValueJson(valueCodec, valueName, reader, extraInfo);

    reader.consumeWhiteSpace();
    reader.expect(',');
    reader.consumeWhiteSpace();

    var secondString = reader.readString();
    if ((firstIsKey && !secondString.equals(valueName))
        || (!firstIsKey && !secondString.equals(keyName)))
      throw new CodecException(
          "Unexpected key/value name " + secondString, reader, extraInfo, null);

    reader.consumeWhiteSpace();
    reader.expect(':');
    reader.consumeWhiteSpace();

    if (firstIsKey) decodedValue = decodeKeyOrValueJson(valueCodec, valueName, reader, extraInfo);
    else decodedKey = decodeKeyOrValueJson(keyCodec, keyName, reader, extraInfo);

    reader.consumeWhiteSpace();
    reader.expect('}');
    return new Entry<>(decodedKey, decodedValue);
  }

  @Override
  public Schema toSchema(SchemaContext schemaContext) {
    var schema = new ObjectSchema();
    schema.setTitle("Entry");
    schema.setProperties(
        Map.of(
            keyName,
            schemaContext.refDefinition(keyCodec),
            valueName,
            schemaContext.refDefinition(valueCodec)));

    return schema;
  }
}
