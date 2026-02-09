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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

@NullMarked
class CodecUtilTest {
  @ModelBuilder
  public static class Simple {
    public int Value;
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
  public static class SimpleImmutableCollection {
    @Immutable public List<String> StringList;
  }

  private <T> void assertFieldsEqual(@Nullable T expected, @Nullable T actual) {
    if (expected == null && actual == null) return;
    if (expected == null ^ actual == null) {
      if (expected == null) fail("expected was null while actual was non-null");
      else fail("expected was non-null while actual was null");
    }

    var fields = expected.getClass().getDeclaredFields();

    for (var field : fields) {
      int modifiers = field.getModifiers();
      if (field.isAnnotationPresent(Skip.class)
          || Modifier.isFinal(modifiers)
          || !Modifier.isPublic(modifiers)
          || Modifier.isStatic(modifiers)) continue;

      try {
        var expectedValue = field.get(expected);
        var actualValue = field.get(actual);

        if (expectedValue == actualValue) continue;

        if (expectedValue == null ^ actualValue == null) {
          if (expectedValue == null) fail(field + " (expected) was null");
          else fail(field + " (actual) was null");
        }

        var expectedClass = expectedValue.getClass();
        var actualClass = actualValue.getClass();

        if (!expectedClass.isAnnotationPresent(ModelBuilder.class))
          assertEquals(expectedValue, actualValue);
        else {
          if (!expectedClass.equals(actualClass))
            fail(
                field
                    + " (expected) type was "
                    + expectedClass.getName()
                    + ", actual was "
                    + actualClass.getName());

          assertFieldsEqual(expectedValue, actualValue);
        }
      } catch (IllegalAccessException e) {
        fail(e);
      }
    }
  }

  private <T extends @Nullable Object> T assertRoundTripEquals(T data, T expected, Codec<T> codec) {
    var encoded = codec.encode(data, new ExtraInfo());
    var decoded = codec.decode(encoded, new ExtraInfo());

    assertFieldsEqual(data, decoded);
    assertFieldsEqual(expected, decoded);

    return decoded;
  }

  @Test
  public void simpleCodecResolution() {
    var builderCodec = CodecUtil.modelBuilder(Simple.class, CodecUtil.PRIMITIVE_RESOLVER);

    var actual = new Simple();
    actual.Value = 67;

    var expected = new Simple();
    expected.Value = 67;

    assertRoundTripEquals(actual, expected, builderCodec);
  }

  @Test
  public void simpleSkipCodecResolution() {
    var builderCodec = CodecUtil.modelBuilder(SimpleSkip.class, CodecUtil.PRIMITIVE_RESOLVER);

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
            SimpleNested.class, CodecUtil.PRIMITIVE_RESOLVER.withRecursiveResolution());

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
            CodecUtil.PRIMITIVE_RESOLVER.withCollectionSupport(
                ImplementationProvider.<Collection<?>>builder()
                    .withAbstractMapping(List.class, ArrayList.class)
                    .build()));

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
  public void simpleImmutableCollectionResolution() {
    var builderCodec =
        CodecUtil.modelBuilder(
            SimpleImmutableCollection.class,
            CodecUtil.PRIMITIVE_RESOLVER.withCollectionSupport(
                ImplementationProvider.<Collection<?>>builder()
                    .withAbstractMapping(List.class, ArrayList.class)
                    .withImmutable(
                        new ImplementationProvider.Spec.Immutable<List<?>>(
                            List.class, List::copyOf, List.of()))
                    .build()));

    var actual = new SimpleImmutableCollection();
    actual.StringList = new ArrayList<>();
    actual.StringList.add("Hello");
    actual.StringList.add("World");

    var expected = new SimpleImmutableCollection();
    expected.StringList = new ArrayList<>();
    expected.StringList.add("Hello");
    expected.StringList.add("World");

    var decoded = assertRoundTripEquals(actual, expected, builderCodec);

    // ensure the list is mutable
    assertThrows(UnsupportedOperationException.class, () -> decoded.StringList.add("!"));
  }
}
