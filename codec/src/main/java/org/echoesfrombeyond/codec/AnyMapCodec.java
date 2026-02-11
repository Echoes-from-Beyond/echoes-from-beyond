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
import com.hypixel.hytale.codec.WrappedCodec;
import com.hypixel.hytale.codec.exception.CodecException;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ArraySchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;
import org.bson.BsonArray;
import org.bson.BsonValue;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Generic {@link Codec} for subclasses of {@link Map}. Supports any key type. Maps are deserialized
 * as arrays of {@link Entry}.
 *
 * @param <Key> the map key type
 * @param <Value> the map value type
 * @param <Container> the map type
 * @see MapResolver
 * @see EntryCodec
 */
@NullMarked
class AnyMapCodec<
        Key extends @Nullable Object,
        Value extends @Nullable Object,
        Container extends Map<Key, Value>>
    implements Codec<Container>, WrappedCodec<Entry<Key, Value>> {
  private final Codec<Entry<Key, Value>> entryCodec;
  private final Supplier<Container> containerSupplier;

  /**
   * Creates a new instance of this class.
   *
   * @param entryCodec the entry codec
   * @param containerSupplier supplier to create instances of this map
   */
  AnyMapCodec(Codec<Entry<Key, Value>> entryCodec, Supplier<Container> containerSupplier) {
    this.entryCodec = entryCodec;
    this.containerSupplier = containerSupplier;
  }

  @Override
  public Container decode(BsonValue bsonValue, ExtraInfo extraInfo) {
    var list = bsonValue.asArray();
    if (list.isEmpty()) return containerSupplier.get();

    var out = containerSupplier.get();
    for (int i = 0; i < list.size(); ++i) {
      var value = list.get(i);
      extraInfo.pushIntKey(i);

      try {
        var entry = entryCodec.decode(value, extraInfo);
        if (entry == null) throw new CodecException("Missing entry", value, extraInfo, null);

        out.put(entry.key(), entry.value());
      } catch (Exception e) {
        throw new CodecException("Failed to decode", value, extraInfo, e);
      } finally {
        extraInfo.popKey();
      }
    }

    return out;
  }

  @Override
  public Container decodeJson(RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
    reader.expect('[');
    reader.consumeWhiteSpace();
    if (reader.tryConsume(']')) return containerSupplier.get();

    var i = 0;
    var out = containerSupplier.get();

    while (true) {
      extraInfo.pushIntKey(i, reader);

      try {
        var entry = entryCodec.decodeJson(reader, extraInfo);
        if (entry == null) throw new CodecException("Missing entry", reader, extraInfo, null);

        out.put(entry.key(), entry.value());
        i++;
      } catch (Exception e) {
        throw new CodecException("Failed to decode", reader, extraInfo, e);
      } finally {
        extraInfo.popKey();
      }

      reader.consumeWhiteSpace();
      if (reader.tryConsumeOrExpect(']', ',')) return out;
      reader.consumeWhiteSpace();
    }
  }

  @Override
  public BsonValue encode(Container container, ExtraInfo extraInfo) {
    var out = new BsonArray();
    var key = 0;

    for (var element : container.entrySet()) {
      extraInfo.pushIntKey(key++);

      try {
        out.add(entryCodec.encode(new Entry<>(element.getKey(), element.getValue()), extraInfo));
      } finally {
        extraInfo.popKey();
      }
    }

    return out;
  }

  @Override
  public Schema toSchema(SchemaContext schemaContext) {
    var schema = new ArraySchema();
    schema.setTitle("Map");
    schema.setItem(schemaContext.refDefinition(entryCodec));

    return schema;
  }

  @Override
  public Codec<Entry<Key, Value>> getChildCodec() {
    return entryCodec;
  }
}
