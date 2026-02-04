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

import static org.junit.jupiter.api.Assertions.*;

import com.hypixel.hytale.codec.ExtraInfo;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.junit.jupiter.api.Test;

@NullMarked
class CodecUtilTest {
  @NullUnmarked
  public static final class Simple {
    public String First;
    public String Second;

    public int Value;
    public Integer SecondValue;
  }

  @Test
  void simpleModel() {
    var codec = CodecUtil.modelBuilderCodec(Simple.class, CodecUtil.PRIMITIVE_RESOLVER);

    var doc = new BsonDocument();
    doc.put("First", new BsonString("first"));
    doc.put("Second", new BsonString("second"));

    doc.put("Value", new BsonInt32(42));
    doc.put("SecondValue", new BsonInt32(67));

    var result = codec.decode(doc, new ExtraInfo());

    assertNotNull(result);

    assertEquals("first", result.First);
    assertEquals("second", result.Second);

    assertEquals(42, result.Value);
    assertEquals(67, result.SecondValue);
  }
}
