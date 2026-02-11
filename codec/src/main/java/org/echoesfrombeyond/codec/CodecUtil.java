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
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.builder.BuilderField;
import com.hypixel.hytale.codec.exception.CodecException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.echoesfrombeyond.codec.annotation.*;
import org.echoesfrombeyond.codec.cache.CodecCache;
import org.jspecify.annotations.NullMarked;

/**
 * Convenience utilities for working with {@link Codec}. Contains several static Codec
 * implementations as well as utilities to generate codecs automatically using an annotation-driven
 * API.
 */
@NullMarked
public final class CodecUtil {
  /** {@link Character} codec, (de)serialized as a String of length 1 */
  public static final Codec<Character> CHARACTER_CODEC = new CharacterCodec();

  /** Primitive {@code boolean} array codec. */
  public static final Codec<boolean[]> BOOLEAN_ARRAY_CODEC = new BooleanArrayCodec();

  /** Primitive {@code byte} array codec. */
  public static final Codec<byte[]> BYTE_ARRAY_CODEC = new ByteArrayCodec();

  /** Primitive {@code char} array codec. */
  public static final Codec<char[]> CHAR_ARRAY_CODEC = new CharArrayCodec();

  /** Primitive {@code short} array codec. */
  public static final Codec<short[]> SHORT_ARRAY_CODEC = new ShortArrayCodec();

  private CodecUtil() {}

  /**
   * Works the same as {@link CodecUtil#modelBuilder(Class, CodecResolver)}, but uses the provided
   * {@link CodecCache} to cache generated codecs and prevent unnecessary resolution.
   *
   * @param model the model type
   * @param resolver the resolver
   * @param cache the cache used to avoid duplicate work
   * @return a new {@link BuilderCodec}
   * @param <T> the model type, which must be annotated with {@link ModelBuilder}
   * @throws IllegalArgumentException if the codec cannot be resolved for any reason
   */
  public static <T> BuilderCodec<T> modelBuilder(
      Class<T> model, CodecResolver resolver, CodecCache cache) {
    return cache.compute(model, resolver, () -> modelBuilder(model, resolver));
  }

  /**
   * Generates a {@link BuilderCodec} from an arbitrary user-defined class. The class must be
   * public, annotated with {@link ModelBuilder}, and it must provide a public parameterless
   * constructor.
   *
   * <p>The declared, public, non-final fields of {@code model} will be examined in order. Those
   * annotated with {@link Skip} will be ignored. Each eligible field will be used to construct a
   * {@link KeyedCodec} according to the following guidelines:
   *
   * <ol>
   *   <li>If the field is annotated with {@link Name}, the value of that annotation is passed to
   *       the {@code key} parameter of the KeyedCodec constructor. Otherwise, the name of the field
   *       is passed as-is.
   *   <li>{@link CodecResolver#resolve(Type, Field)} is called with the generic type of the field
   *       and the field itself.
   *   <li>If the resolver returned null, throw an {@link IllegalArgumentException}. Otherwise, the
   *       codec is passed to the {@code codec} parameter of the KeyedCodec constructor.
   *   <li>The value of the {@code required} parameter is {@code false} if the field is annotated
   *       with {@link Opt}, and {@code true} otherwise.
   * </ol>
   *
   * <p>The constructed KeyedCodec is passed to {@link BuilderCodec.Builder#append(KeyedCodec,
   * BiConsumer, Function)}; the {@code setter} and {@code getter} parameters of this constructor
   * are generated using reflection.
   *
   * <p>Finally, if the field is annotated with {@link Doc}, its value is passed to {@link
   * BuilderField.FieldBuilder#documentation(String)}.
   *
   * @param model the model type class
   * @param resolver the resolver used to generate {@link Codec}s based on the field type
   * @return the generated BuilderCodec
   * @param <T> the model type
   */
  @SuppressWarnings("unchecked")
  public static <T> BuilderCodec<T> modelBuilder(Class<T> model, CodecResolver resolver) {
    if (!model.isAnnotationPresent(ModelBuilder.class))
      throw new IllegalArgumentException("Missing ModelBuilder annotation " + model.getName());

    MethodHandle constructor;
    try {
      constructor =
          MethodHandles.publicLookup().findConstructor(model, MethodType.methodType(void.class));
    } catch (NoSuchMethodException _) {
      throw new IllegalArgumentException(
          "Model missing public parameterless constructor " + model.getName());
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException(model.getName(), e);
    }

    var builder =
        BuilderCodec.builder(
            model,
            () -> {
              try {
                return model.cast(constructor.invoke());
              } catch (Throwable e) {
                throw new RuntimeException(e);
              }
            });

    var topLevelDocumentation = model.getDeclaredAnnotation(Doc.class);
    if (topLevelDocumentation != null)
      builder = builder.documentation(topLevelDocumentation.value());

    var fields = model.getDeclaredFields();

    for (var field : fields) {
      int modifiers = field.getModifiers();
      if (Modifier.isStatic(modifiers)
          || !Modifier.isPublic(modifiers)
          || Modifier.isFinal(modifiers)) continue;

      if (field.isAnnotationPresent(Skip.class)) continue;

      var nameAnnotation = field.getDeclaredAnnotation(Name.class);
      var name = nameAnnotation == null ? field.getName() : nameAnnotation.value();

      var resolve = (Codec<Object>) resolver.resolve(field.getGenericType(), field);
      if (resolve == null)
        throw new IllegalArgumentException("Could not resolve codec for field " + field);

      var key = new KeyedCodec<>(name, resolve, !field.isAnnotationPresent(Opt.class));

      MethodHandle read;
      MethodHandle write;
      try {
        read = MethodHandles.publicLookup().unreflectGetter(field);
        write = MethodHandles.publicLookup().unreflectSetter(field);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }

      var fieldBuilder =
          builder.append(
              key,
              (self, value) -> {
                try {
                  write.invoke(self, value);
                } catch (Throwable e) {
                  throw new CodecException("Write error", e);
                }
              },
              (self) -> {
                try {
                  return read.invoke(self);
                } catch (Throwable e) {
                  throw new CodecException("Read error", e);
                }
              });

      var fieldDocumentation = field.getDeclaredAnnotation(Doc.class);
      if (fieldDocumentation != null)
        fieldBuilder = fieldBuilder.documentation(fieldDocumentation.value());

      builder = fieldBuilder.add();
    }

    return builder.build();
  }
}
