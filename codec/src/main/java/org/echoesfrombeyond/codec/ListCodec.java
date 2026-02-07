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
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.bson.BsonArray;
import org.bson.BsonValue;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
class ListCodec<V extends @Nullable Object, S extends List<V>>
    implements Codec<List<V>>, WrappedCodec<V> {
  private final Codec<V> elementCodec;
  private final Supplier<S> listSupplier;
  private final boolean unmodifiable;

  ListCodec(Codec<V> elementCodec, Supplier<S> listSupplier, boolean unmodifiable) {
    this.elementCodec = elementCodec;
    this.listSupplier = listSupplier;
    this.unmodifiable = unmodifiable;
  }

  public List<V> decode(BsonValue bsonValue, ExtraInfo extraInfo) {
    var list = bsonValue.asArray();
    if (list.isEmpty()) return unmodifiable ? List.of() : listSupplier.get();

    var out = listSupplier.get();

    for (int i = 0; i < list.size(); ++i) {
      var value = list.get(i);
      extraInfo.pushIntKey(i);

      try {
        out.add(elementCodec.decode(value, extraInfo));
      } catch (Exception e) {
        throw new CodecException("Failed to decode", value, extraInfo, e);
      } finally {
        extraInfo.popKey();
      }
    }

    return unmodifiable ? Collections.unmodifiableList(out) : out;
  }

  public List<V> decodeJson(RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
    reader.expect('[');
    reader.consumeWhiteSpace();
    if (reader.tryConsume(']')) return unmodifiable ? List.of() : listSupplier.get();

    int i = 0;
    var out = listSupplier.get();

    while (true) {
      extraInfo.pushIntKey(i, reader);

      try {
        var decoded = elementCodec.decodeJson(reader, extraInfo);
        out.add(decoded);
        ++i;
      } catch (Exception e) {
        throw new CodecException("Failed to decode", reader, extraInfo, e);
      } finally {
        extraInfo.popKey();
      }

      reader.consumeWhiteSpace();
      if (reader.tryConsumeOrExpect(']', ',')) {
        return unmodifiable ? Collections.unmodifiableList(out) : out;
      }

      reader.consumeWhiteSpace();
    }
  }

  public BsonValue encode(List<V> vs, ExtraInfo extraInfo) {
    var out = new BsonArray();
    var key = 0;

    for (var v : vs) {
      extraInfo.pushIntKey(key++);

      try {
        out.add(elementCodec.encode(v, extraInfo));
      } finally {
        extraInfo.popKey();
      }
    }

    return out;
  }

  public Schema toSchema(SchemaContext context) {
    ArraySchema schema = new ArraySchema();
    schema.setTitle("List");
    schema.setItem(context.refDefinition(elementCodec));
    return schema;
  }

  public Codec<V> getChildCodec() {
    return elementCodec;
  }
}
