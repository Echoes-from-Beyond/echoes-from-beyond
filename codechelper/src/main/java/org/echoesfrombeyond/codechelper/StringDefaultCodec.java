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

package org.echoesfrombeyond.codechelper;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.WrappedCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import java.util.function.BiFunction;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
class StringDefaultCodec<T> implements Codec<T>, WrappedCodec<T> {
  private final Codec<T> inner;
  private final BiFunction<String, ? super ExtraInfo, ? extends T> fromString;
  private final BiFunction<? super T, ? super ExtraInfo, @Nullable String> toString;

  StringDefaultCodec(
      Codec<T> inner,
      BiFunction<String, ? super ExtraInfo, ? extends T> fromString,
      BiFunction<? super T, ? super ExtraInfo, @Nullable String> toString) {
    this.inner = inner;
    this.fromString = fromString;
    this.toString = toString;
  }

  @Override
  public @Nullable T decode(BsonValue value, ExtraInfo extraInfo) {
    if (value.isString()) return fromString.apply(value.asString().getValue(), extraInfo);
    return inner.decode(value, extraInfo);
  }

  @Override
  public @Nullable T decodeJson(RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
    int peek = reader.peek();
    return switch (peek) {
      case '"' -> fromString.apply(reader.readString(), extraInfo);
      case '{' -> inner.decodeJson(reader, extraInfo);
      default ->
          throw new IOException(
              "Unexpected character: "
                  + Integer.toHexString((char) peek)
                  + ", '"
                  + (char) peek
                  + "' expected '\"' or '{'!");
    };
  }

  @Override
  public BsonValue encode(T data, ExtraInfo extra) {
    var string = toString.apply(data, extra);
    if (string != null) return new BsonString(string);

    return inner.encode(data, extra);
  }

  @Override
  public Codec<T> getChildCodec() {
    return inner;
  }

  @Override
  public Schema toSchema(SchemaContext context) {
    return Schema.anyOf(inner.toSchema(context), new StringSchema());
  }
}
