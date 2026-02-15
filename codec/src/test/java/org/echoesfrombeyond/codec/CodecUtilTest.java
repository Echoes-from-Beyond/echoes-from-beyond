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

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.RawJsonCodec;
import com.hypixel.hytale.codec.exception.CodecValidationException;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.json.JsonWriterSettings;
import org.echoesfrombeyond.codec.annotation.ModelBuilder;
import org.echoesfrombeyond.codec.annotation.Skip;
import org.echoesfrombeyond.codec.annotation.validator.ValidateNonEmpty;
import org.echoesfrombeyond.codec.annotation.validator.ValidateRequiredMapKeys;
import org.echoesfrombeyond.codec.cache.CodecCache;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

@NullMarked
class CodecUtilTest {
  @ModelBuilder
  public static class Simple {
    public int Value;

    @Override
    public boolean equals(Object obj) {
      return obj instanceof Simple simple && simple.Value == Value;
    }

    @Override
    public int hashCode() {
      return Integer.hashCode(Value);
    }
  }

  @ModelBuilder
  public static class SimpleSkip {
    public int Value;

    @Skip public int Skipped;
  }

  @ModelBuilder
  @NullUnmarked
  public static class SimpleNested {
    public int Outer;

    public SimpleSkip Inner;
  }

  @ModelBuilder
  @NullUnmarked
  public static class SimpleCollection {
    public List<String> StringList;
  }

  @ModelBuilder
  @NullUnmarked
  public static class PrimitiveArrays {
    public boolean[] Booleans;
    public byte[] Bytes;
    public short[] Shorts;
    public char[] Chars;
    public int[] Ints;
    public float[] Floats;
    public double[] Doubles;
    public long[] Longs;
    public String[] String;
  }

  @ModelBuilder
  @NullUnmarked
  public static class NestedArrays {
    public int[][] MultidimensionalIntArray;
    public Simple[] SubtypeArray;
  }

  @ModelBuilder
  @NullUnmarked
  public static class SimpleArray {
    public int[] Arrays;
  }

  @ModelBuilder
  @NullUnmarked
  @SuppressWarnings("unused")
  public static class CacheInner {
    public int Value;
  }

  @ModelBuilder
  @NullUnmarked
  @SuppressWarnings("unused")
  public static class CacheOuter {
    public CacheInner InnerOne;
    public CacheInner InnerTwo;
  }

  @ModelBuilder
  @NullUnmarked
  public static class StringKeyMap {
    public Map<String, Integer> StringToIntMap;
  }

  @ModelBuilder
  @NullUnmarked
  public static class AnyMap {
    public Map<Simple, Simple> Mapping;
  }

  @ModelBuilder
  @NullUnmarked
  public static class ValidatorTest {
    @ValidateNonEmpty public short[] Array;
  }

  @ModelBuilder
  @NullUnmarked
  public static class ValidateRequiredMapKeysTest {
    @ValidateRequiredMapKeys({"required", "key"})
    public Map<String, String> Map;
  }

  private void assertDeepEquals(@Nullable Object expected, @Nullable Object actual) {
    if (expected == null && actual == null) return;
    if (expected == null ^ actual == null) {
      if (expected == null) fail("expected was null while actual was non-null");
      else fail("expected was non-null while actual was null");
    }

    var expectedClass = expected.getClass();
    var actualClass = actual.getClass();

    if (expectedClass.isArray() && actualClass.isArray()) {
      var expectedComponent = expectedClass.getComponentType();
      var actualComponent = actualClass.getComponentType();

      if (!expectedComponent.equals(actualComponent))
        fail(
            "expected component type was "
                + expectedComponent.getName()
                + ", actual was "
                + actualComponent.getName());

      var expectedLen = Array.getLength(expected);
      var actualLen = Array.getLength(actual);

      assertEquals(expectedLen, actualLen, "actual length differed from expected length");

      for (int i = 0; i < expectedLen; i++)
        assertDeepEquals(Array.get(expected, i), Array.get(actual, i));
      return;
    }

    if (!expectedClass.isAnnotationPresent(ModelBuilder.class)
        || !actualClass.isAnnotationPresent(ModelBuilder.class)) {
      assertEquals(expected, actual);
      return;
    }

    if (!expectedClass.equals(actualClass))
      fail(
          "expected ModelBuilder class was "
              + expectedClass.getName()
              + ", actual ModelBuilder class was "
              + actualClass.getName());

    for (var field : expectedClass.getDeclaredFields()) {
      int modifiers = field.getModifiers();
      if (field.isAnnotationPresent(Skip.class)
          || Modifier.isFinal(modifiers)
          || !Modifier.isPublic(modifiers)
          || Modifier.isStatic(modifiers)) continue;

      try {
        assertDeepEquals(field.get(expected), field.get(actual));
      } catch (IllegalAccessException e) {
        fail(e);
      }
    }
  }

  private <T extends @Nullable Object, C extends Codec<T> & RawJsonCodec<T>>
      T assertRoundTripEquals(T data, T expected, C codec) {
    var encoded = codec.encode(data, new ExtraInfo());
    var decoded = codec.decode(encoded, new ExtraInfo());

    assertDeepEquals(data, decoded);
    assertDeepEquals(expected, decoded);

    var rawJsonReader =
        new RawJsonReader(
            new CharArrayReader(encoded.asDocument().toJson().toCharArray()), new char[4096]);

    T rawDecoded = null;
    try {
      rawDecoded = codec.decodeJson(rawJsonReader, new ExtraInfo());
    } catch (IOException e) {
      fail(e);
    }

    assertDeepEquals(data, rawDecoded);
    return decoded;
  }

  @Test
  public void simpleCodecResolution() {
    var builderCodec = CodecUtil.modelBuilder(Simple.class, CodecResolver.PRIMITIVE);

    var actual = new Simple();
    actual.Value = 67;

    var expected = new Simple();
    expected.Value = 67;

    assertRoundTripEquals(actual, expected, builderCodec);
  }

  @Test
  public void simpleSkipCodecResolution() {
    var builderCodec = CodecUtil.modelBuilder(SimpleSkip.class, CodecResolver.PRIMITIVE);

    var actual = new SimpleSkip();
    actual.Value = 67;
    actual.Skipped = 100;

    var expected = new SimpleSkip();
    expected.Value = 67;
    expected.Skipped = 42;

    assertRoundTripEquals(actual, expected, builderCodec);
  }

  @Test
  public void simpleNestedCodecResolution() {
    var builderCodec =
        CodecUtil.modelBuilder(
            SimpleNested.class,
            CodecResolver.builder()
                .chain(CodecResolver.PRIMITIVE)
                .withRecursiveResolution()
                .build());

    var actual = new SimpleNested();
    actual.Inner = new SimpleSkip();
    actual.Inner.Value = 67;
    actual.Inner.Skipped = 100;
    actual.Outer = -10;

    var expected = new SimpleNested();
    expected.Inner = new SimpleSkip();
    expected.Inner.Value = 67;
    expected.Inner.Skipped = 42;
    expected.Outer = -10;

    assertRoundTripEquals(actual, expected, builderCodec);
  }

  @Test
  public void simpleMutableCollectionResolution() {
    var builderCodec =
        CodecUtil.modelBuilder(
            SimpleCollection.class,
            CodecResolver.builder()
                .chain(CodecResolver.PRIMITIVE)
                .withCollectionSupport()
                .withSubtypeMapping(List.class, ArrayList.class)
                .build());

    var actual = new SimpleCollection();
    actual.StringList = new ArrayList<>();
    actual.StringList.add("Hello");
    actual.StringList.add("World");

    var expected = new SimpleCollection();
    expected.StringList = new ArrayList<>();
    expected.StringList.add("Hello");
    expected.StringList.add("World");

    var decoded = assertRoundTripEquals(actual, expected, builderCodec);

    // ensure the list is mutable
    decoded.StringList.add("!");
  }

  @Test
  public void primitiveArraysResolution() {
    var builderCodec =
        CodecUtil.modelBuilder(
            PrimitiveArrays.class,
            CodecResolver.builder().chain(CodecResolver.PRIMITIVE).withArraySupport().build());

    var actual = new PrimitiveArrays();
    actual.Booleans = new boolean[] {true, false};
    actual.Bytes = new byte[] {0, 4, 8, Byte.MAX_VALUE, Byte.MIN_VALUE};
    actual.Shorts = new short[] {123, 24, Short.MAX_VALUE, Short.MIN_VALUE};
    actual.Chars = new char[] {'a', 'b', '\0', 'z'};
    actual.Ints = new int[] {0, 10, 67, Integer.MAX_VALUE, Integer.MIN_VALUE};
    actual.Floats = new float[] {0.0F, 0.5F, 50F};
    actual.Longs = new long[] {Long.MAX_VALUE, Long.MIN_VALUE};
    actual.Doubles = new double[] {-67, 1000.5};
    actual.String = new String[] {"Hello", "World", "!"};

    var expected = new PrimitiveArrays();
    expected.Booleans = new boolean[] {true, false};
    expected.Bytes = new byte[] {0, 4, 8, Byte.MAX_VALUE, Byte.MIN_VALUE};
    expected.Shorts = new short[] {123, 24, Short.MAX_VALUE, Short.MIN_VALUE};
    expected.Chars = new char[] {'a', 'b', '\0', 'z'};
    expected.Ints = new int[] {0, 10, 67, Integer.MAX_VALUE, Integer.MIN_VALUE};
    expected.Floats = new float[] {0.0F, 0.5F, 50F};
    expected.Longs = new long[] {Long.MAX_VALUE, Long.MIN_VALUE};
    expected.Doubles = new double[] {-67, 1000.5};
    expected.String = new String[] {"Hello", "World", "!"};

    assertRoundTripEquals(actual, expected, builderCodec);
  }

  @Test
  public void nestedArrayResolution() {
    var builderCodec =
        CodecUtil.modelBuilder(
            NestedArrays.class,
            CodecResolver.builder()
                .chain(CodecResolver.PRIMITIVE)
                .withArraySupport()
                .withRecursiveResolution()
                .build());

    var actual = new NestedArrays();
    actual.MultidimensionalIntArray = new int[][] {new int[] {0, 1, 2}, new int[] {3, 4, 5}};
    actual.SubtypeArray = new Simple[] {new Simple(), new Simple()};
    actual.SubtypeArray[0].Value = 67;
    actual.SubtypeArray[1].Value = 42;

    var expected = new NestedArrays();
    expected.MultidimensionalIntArray = new int[][] {new int[] {0, 1, 2}, new int[] {3, 4, 5}};
    expected.SubtypeArray = new Simple[] {new Simple(), new Simple()};
    expected.SubtypeArray[0].Value = 67;
    expected.SubtypeArray[1].Value = 42;

    assertRoundTripEquals(actual, expected, builderCodec);
  }

  @Test
  public void arrayResolutionWithoutPrimitive() {
    var builderCodec =
        CodecUtil.modelBuilder(
            SimpleArray.class, CodecResolver.builder().withArraySupport().build());

    var actual = new SimpleArray();
    actual.Arrays = new int[] {1, 2, 3};

    var expected = new SimpleArray();
    expected.Arrays = new int[] {1, 2, 3};

    assertRoundTripEquals(actual, expected, builderCodec);
  }

  @Test
  public void cacheDeduplicatesIdenticalCodecs() {
    var cache = CodecCache.cache();
    var resolver =
        CodecResolver.builder()
            .withRecursiveResolution(cache)
            .chain(CodecResolver.PRIMITIVE)
            .build();

    var firstOuter = CodecUtil.modelBuilder(CacheOuter.class, resolver, cache);
    var secondOuter = CodecUtil.modelBuilder(CacheOuter.class, resolver, cache);

    var inner = CodecUtil.modelBuilder(CacheInner.class, resolver, cache);
    var firstInner = firstOuter.getEntries().get("InnerOne").getFirst().getCodec().getChildCodec();

    assertSame(firstOuter, secondOuter);
    assertSame(inner, firstInner);
  }

  @Test
  public void stringToIntMap() {
    var resolver =
        CodecResolver.builder()
            .chain(CodecResolver.PRIMITIVE)
            .withMapSupport()
            .withSubtypeMapping(Map.class, HashMap.class)
            .build();

    var codec = CodecUtil.modelBuilder(StringKeyMap.class, resolver);

    var actual = new StringKeyMap();
    actual.StringToIntMap = new HashMap<>();
    actual.StringToIntMap.put("Hello", 67);
    actual.StringToIntMap.put("Goodbye", -67);

    var expected = new StringKeyMap();
    expected.StringToIntMap = new HashMap<>();
    expected.StringToIntMap.put("Hello", 67);
    expected.StringToIntMap.put("Goodbye", -67);

    assertRoundTripEquals(actual, expected, codec);
  }

  @Test
  public void anyValuedMap() {
    var resolver =
        CodecResolver.builder()
            .chain(CodecResolver.PRIMITIVE)
            .withRecursiveResolution()
            .withMapSupport()
            .withSubtypeMapping(Map.class, HashMap.class)
            .build();

    var codec = CodecUtil.modelBuilder(AnyMap.class, resolver);

    var actual = new AnyMap();

    var actualKey = new Simple();
    actualKey.Value = 10;

    var actualValue = new Simple();
    actualValue.Value = 67;

    actual.Mapping = new HashMap<>();
    actual.Mapping.put(actualKey, actualValue);

    var expected = new AnyMap();

    var expectedKey = new Simple();
    expectedKey.Value = 10;

    var expectedValue = new Simple();
    expectedValue.Value = 67;

    expected.Mapping = new HashMap<>();
    expected.Mapping.put(expectedKey, expectedValue);

    assertRoundTripEquals(actual, expected, codec);
  }

  @Test
  public void customKeyValueNamedMap() {
    var resolver =
        CodecResolver.builder()
            .chain(CodecResolver.PRIMITIVE)
            .withRecursiveResolution()
            .withMapSupport("NewKey", "NewValue")
            .withSubtypeMapping(Map.class, HashMap.class)
            .build();

    var codec = CodecUtil.modelBuilder(AnyMap.class, resolver);
    var actual = new AnyMap();

    var actualKey = new Simple();
    actualKey.Value = 10;

    var actualValue = new Simple();
    actualValue.Value = 67;

    actual.Mapping = new HashMap<>();
    actual.Mapping.put(actualKey, actualValue);

    var json =
        codec
            .encode(actual, new ExtraInfo())
            .toJson(JsonWriterSettings.builder().indent(false).build());
    assertEquals(
        "{\"Mapping\": [{\"NewKey\": {\"Value\": 10}, \"NewValue\": {\"Value\": 67}}]}", json);
  }

  @Test
  public void validatorTest() {
    var builderCodec =
        CodecUtil.modelBuilder(
            ValidatorTest.class,
            CodecResolver.builder()
                .chain(CodecResolver.PRIMITIVE)
                .withArraySupport()
                .withSubtypeMapping(Object.class, String.class)
                .build());

    var actual = new ValidatorTest();
    actual.Array = new short[0];

    var encoded = builderCodec.encode(actual, new ExtraInfo());

    assertThrows(
        CodecValidationException.class, () -> builderCodec.decode(encoded, new ExtraInfo()));
  }

  @Test
  public void validateMapTest() {
    var builderCodec =
        CodecUtil.modelBuilder(
            ValidateRequiredMapKeysTest.class,
            CodecResolver.builder()
                .chain(CodecResolver.PRIMITIVE)
                .withMapSupport()
                .withSubtypeMapping(Map.class, HashMap.class)
                .build());

    var actual = new ValidateRequiredMapKeysTest();
    actual.Map = new HashMap<>();
    actual.Map.put("required", "only one of the required keys...");

    var encoded = builderCodec.encode(actual, new ExtraInfo());

    assertThrows(
        CodecValidationException.class, () -> builderCodec.decode(encoded, new ExtraInfo()));
  }

  @Test
  public void validateMapTestValidatesWhenRequiredKeysExist() {
    var builderCodec =
        CodecUtil.modelBuilder(
            ValidateRequiredMapKeysTest.class,
            CodecResolver.builder()
                .chain(CodecResolver.PRIMITIVE)
                .withMapSupport()
                .withSubtypeMapping(Map.class, HashMap.class)
                .build());

    var actual = new ValidateRequiredMapKeysTest();
    actual.Map = new HashMap<>();
    actual.Map.put("required", "required value");
    actual.Map.put("key", "required key");

    var encoded = builderCodec.encode(actual, new ExtraInfo());
    builderCodec.decode(encoded, new ExtraInfo());
  }
}
