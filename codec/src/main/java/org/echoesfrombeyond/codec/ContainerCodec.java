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
import java.util.Collection;
import org.bson.BsonArray;
import org.bson.BsonValue;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
class ContainerCodec<Element extends @Nullable Object, Container extends Collection<Element>>
    implements Codec<Container>, WrappedCodec<Element> {
  private final Codec<Element> elementCodec;
  private final ContainerProvider.Spec<Container> spec;

  ContainerCodec(Codec<Element> elementCodec, ContainerProvider.Spec<Container> spec) {
    this.elementCodec = elementCodec;
    this.spec = spec;
  }

  @Override
  public @Nullable Container decode(BsonValue bsonValue, ExtraInfo extraInfo) {
    var list = bsonValue.asArray();
    if (list.isEmpty())
      return spec.immutable() == null ? spec.creator().get() : spec.immutable().emptyImmutable();

    var out = spec.creator().get();
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

    return spec.immutable() == null ? out : spec.immutable().makeImmutable().apply(out);
  }

  @Override
  public @Nullable Container decodeJson(RawJsonReader reader, ExtraInfo extraInfo)
      throws IOException {
    reader.expect('[');
    reader.consumeWhiteSpace();
    if (reader.tryConsume(']')) return spec.creator().get();

    var i = 0;
    var out = spec.creator().get();

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
      if (reader.tryConsumeOrExpect(']', ',')) return out;
      reader.consumeWhiteSpace();
    }
  }

  @Override
  public BsonValue encode(Container elements, ExtraInfo extraInfo) {
    var out = new BsonArray();
    var key = 0;

    for (var element : elements) {
      extraInfo.pushIntKey(key++);

      try {
        out.add(elementCodec.encode(element, extraInfo));
      } finally {
        extraInfo.popKey();
      }
    }

    return out;
  }

  @Override
  public Schema toSchema(SchemaContext schemaContext) {
    var schema = new ArraySchema();
    schema.setTitle("Collection");
    schema.setItem(schemaContext.refDefinition(elementCodec));
    return schema;
  }

  @Override
  public Codec<Element> getChildCodec() {
    return elementCodec;
  }
}
