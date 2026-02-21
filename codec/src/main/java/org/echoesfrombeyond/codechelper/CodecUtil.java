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

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.JsonAsset;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.builder.BuilderField;
import com.hypixel.hytale.codec.exception.CodecException;
import com.hypixel.hytale.codec.validation.Validator;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.echoesfrombeyond.codechelper.annotation.*;
import org.echoesfrombeyond.codechelper.annotation.validator.ValidatorSpec;
import org.echoesfrombeyond.codechelper.cache.CodecCache;
import org.echoesfrombeyond.codechelper.exception.FieldModelException;
import org.echoesfrombeyond.codechelper.exception.ModelException;
import org.echoesfrombeyond.codechelper.exception.ValidatorModelException;
import org.echoesfrombeyond.codechelper.validator.ValidatorProvider;
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
    return cache.compute(
        model, null, null, null, BuilderCodec.class, resolver, () -> modelBuilder(model, resolver));
  }

  /**
   * Generates a {@link BuilderCodec} from an arbitrary user-defined class. The class must be
   * annotated with {@link ModelBuilder}. If non-{@code abstract}, the class must provide a
   * parameterless constructor.
   *
   * <p>The declared, non-final fields of {@code model} will be examined in order. Those annotated
   * with {@link Skip} will be ignored. Each eligible field will be used to construct a {@link
   * KeyedCodec} according to the following guidelines:
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
   * <p>If the field is annotated with {@link Doc}, its value is passed to {@link
   * BuilderField.FieldBuilder#documentation(String)}.
   *
   * <p>If the field is annotated with any annotation that is itself annotated with the
   * meta-annotation {@link ValidatorSpec}, the value of the (ValidatorSpec) annotation specifies a
   * {@link ValidatorProvider} instance that will be used to provide a {@link Validator} for the
   * field. Each field may contain multiple such annotations, which will provide validators
   * in-order, which are passed to {@link BuilderField.FieldBuilder#addValidator(Validator)}. See
   * {@link ValidatorProvider} for more details on how this process works.
   *
   * <p>If (and only if) {@code model} is abstract, the returned BuilderCodec will be abstract.
   *
   * @param model the model type class
   * @param resolver the resolver used to generate {@link Codec}s based on the field type
   * @return the generated BuilderCodec
   * @param <T> the model type
   */
  public static <T> BuilderCodec<T> modelBuilder(Class<T> model, CodecResolver resolver) {
    checkModelPreconditions(model);
    var lookup = getLookupForModel(model);

    BuilderCodec.Builder<T> builder;
    if (Modifier.isAbstract(model.getModifiers())) builder = BuilderCodec.abstractBuilder(model);
    else {
      var constructor = getConstructorForModel(model, lookup);

      builder =
          BuilderCodec.builder(
              model,
              () -> {
                try {
                  return model.cast(constructor.invoke());
                } catch (Throwable e) {
                  throw new CodecException("Error constructing model", e);
                }
              });
    }

    modelFields(builder, model, resolver, lookup);
    return builder.build();
  }

  public static <T extends JsonAsset<String>> AssetBuilderCodec<String, T> modelAssetBuilder(
      Class<T> model, CodecResolver resolver, CodecCache cache) {
    return cache.compute(
        model,
        null,
        String.class,
        Codec.STRING,
        AssetBuilderCodec.class,
        resolver,
        () -> modelAssetBuilder(model, String.class, Codec.STRING, resolver));
  }

  public static <T extends JsonAsset<String>> AssetBuilderCodec<String, T> modelAssetBuilder(
      Class<T> model, CodecResolver resolver) {
    return modelAssetBuilder(model, String.class, Codec.STRING, resolver);
  }

  public static <K, T extends JsonAsset<K>> AssetBuilderCodec<K, T> modelAssetBuilder(
      Class<T> model, Class<K> idClass, Codec<K> idCodec, CodecResolver resolver) {
    checkModelPreconditions(model);

    var lookup = getLookupForModel(model);
    var constructor = getConstructorForModel(model, lookup);

    var idField = getUniqueAnnotatedField(model, Id.class, idClass);
    var dataField = getUniqueAnnotatedField(model, Data.class, AssetExtraInfo.Data.class);

    MethodHandle idRead;
    MethodHandle idWrite;

    MethodHandle dataRead;
    MethodHandle dataWrite;

    try {
      idRead = lookup.unreflectGetter(idField);
      idWrite = lookup.unreflectSetter(idField);
    } catch (IllegalAccessException e) {
      throw new FieldModelException(model, idField, "Should be able to access @Id field", e);
    }

    try {
      dataRead = lookup.unreflectGetter(dataField);
      dataWrite = lookup.unreflectSetter(dataField);
    } catch (IllegalAccessException e) {
      throw new FieldModelException(model, dataField, "Should be able to access @Data field", e);
    }

    var builder =
        AssetBuilderCodec.builder(
            model,
            () -> {
              try {
                return model.cast(constructor.invoke());
              } catch (Throwable e) {
                throw new CodecException("Error constructing model", e);
              }
            },
            idCodec,
            (self, value) -> {
              try {
                idWrite.invoke(self, value);
              } catch (Throwable e) {
                throw new CodecException("Couldn't write @Id field", e);
              }
            },
            (self) -> {
              try {
                return idClass.cast(idRead.invoke(self));
              } catch (Throwable e) {
                throw new CodecException("Couldn't read @Id field", e);
              }
            },
            (self, data) -> {
              try {
                dataWrite.invoke(self, data);
              } catch (Throwable e) {
                throw new CodecException("Couldn't write @Data field", e);
              }
            },
            self -> {
              try {
                return (AssetExtraInfo.Data) dataRead.invoke(self);
              } catch (Throwable e) {
                throw new CodecException("Couldn't read @Data field", e);
              }
            });

    modelFields(builder, model, resolver, lookup);
    return builder.build();
  }

  private static Field getUniqueAnnotatedField(
      Class<?> model, Class<? extends Annotation> annotation, Class<?> requiredType) {
    Field found = null;
    for (var field : model.getDeclaredFields()) {
      if (!field.isAnnotationPresent(annotation)) continue;
      if (found == null) {
        found = field;
        continue;
      }

      throw new FieldModelException(
          model, field, "Expected to find only one @" + annotation.getCanonicalName() + " field");
    }

    if (found == null)
      throw new ModelException(
          model,
          "Expected to find exactly one field annotated with @" + annotation.getCanonicalName());

    if (!found.getType().equals(requiredType))
      throw new ModelException(
          model,
          "@"
              + annotation.getCanonicalName()
              + " field type should have been "
              + requiredType.getName());

    return found;
  }

  private static void checkModelPreconditions(Class<?> model) {
    if (!model.isAnnotationPresent(ModelBuilder.class))
      throw new ModelException(model, "Must be annotated with @ModelBuilder");
  }

  private static MethodHandles.Lookup getLookupForModel(Class<?> model) {
    try {
      return MethodHandles.privateLookupIn(model, MethodHandles.lookup());
    } catch (IllegalAccessException e) {
      throw new ModelException(model, "Private access must be possible");
    }
  }

  private static MethodHandle getConstructorForModel(Class<?> model, MethodHandles.Lookup lookup) {
    try {
      return lookup.findConstructor(model, MethodType.methodType(void.class));
    } catch (NoSuchMethodException _) {
      throw new ModelException(model, "Must have parameterless constructor");
    } catch (IllegalAccessException e) {
      throw new ModelException(model, "Parameterless constructor must be accessible", e);
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> void modelFields(
      BuilderCodec.BuilderBase<T, ?> builder,
      Class<T> model,
      CodecResolver resolver,
      MethodHandles.Lookup lookup) {
    var topLevelDocumentation = model.getDeclaredAnnotation(Doc.class);
    if (topLevelDocumentation != null)
      builder = builder.documentation(topLevelDocumentation.value());

    for (var field : model.getDeclaredFields()) {
      int modifiers = field.getModifiers();
      if (Modifier.isStatic(modifiers)
          || !Modifier.isPublic(modifiers)
          || Modifier.isFinal(modifiers)) continue;

      if (field.isAnnotationPresent(Skip.class)) continue;

      var nameAnnotation = field.getDeclaredAnnotation(Name.class);
      var name = nameAnnotation == null ? field.getName() : nameAnnotation.value();

      var resolve = (Codec<Object>) resolver.resolve(field.getGenericType(), field);
      if (resolve == null)
        throw new FieldModelException(model, field, "Field type must be resolvable");

      var key = new KeyedCodec<>(name, resolve, !field.isAnnotationPresent(Opt.class));

      MethodHandle read;
      MethodHandle write;
      try {
        read = lookup.unreflectGetter(field);
        write = lookup.unreflectSetter(field);
      } catch (IllegalAccessException e) {
        throw new FieldModelException(model, field, "Should be able to access field", e);
      }

      var fieldBuilder =
          builder.append(
              key,
              (self, value) -> {
                try {
                  write.invoke(self, value);
                } catch (Throwable e) {
                  throw new CodecException("Couldn't write to field", e);
                }
              },
              (self) -> {
                try {
                  return read.invoke(self);
                } catch (Throwable e) {
                  throw new CodecException("Couldn't read from field", e);
                }
              });

      var fieldDocumentation = field.getDeclaredAnnotation(Doc.class);
      if (fieldDocumentation != null)
        fieldBuilder = fieldBuilder.documentation(fieldDocumentation.value());

      for (var annotation : field.getDeclaredAnnotations()) {
        var spec = annotation.annotationType().getDeclaredAnnotation(ValidatorSpec.class);
        if (spec == null) continue;

        Field instanceField;
        try {
          instanceField = spec.value().getField("INSTANCE");
        } catch (NoSuchFieldException e) {
          throw new ValidatorModelException(
              model,
              field,
              spec,
              annotation.annotationType(),
              "ValidatorProvider must have a public, static field named INSTANCE");
        }

        var instanceFieldModifiers = instanceField.getModifiers();
        if (!Modifier.isPublic(instanceFieldModifiers)
            || !Modifier.isStatic(instanceFieldModifiers))
          throw new ValidatorModelException(
              model,
              field,
              spec,
              annotation.annotationType(),
              "ValidatorProvider INSTANCE field must be public static");

        Object instanceFieldRaw;
        try {
          instanceFieldRaw = instanceField.get(null);
        } catch (IllegalAccessException e) {
          throw new IllegalArgumentException(e);
        }

        if (instanceFieldRaw == null)
          throw new ValidatorModelException(
              model,
              field,
              spec,
              annotation.annotationType(),
              "ValidatorProvider INSTANCE field must not be null");

        if (!ValidatorProvider.class.isAssignableFrom(instanceFieldRaw.getClass()))
          throw new ValidatorModelException(
              model,
              field,
              spec,
              annotation.annotationType(),
              "ValidatorProvider INSTANCE field type must be assignable to ValidatorProvider");

        var validatorProvider = (ValidatorProvider<Annotation>) instanceFieldRaw;

        var validator = validatorProvider.getInstance(annotation, field);
        if (validator == null)
          throw new ValidatorModelException(
              model,
              field,
              spec,
              annotation.annotationType(),
              "ValidatorProvider must be able to provide for the field");

        fieldBuilder = fieldBuilder.addValidator((Validator<? super Object>) validator);
      }

      builder = fieldBuilder.add();
    }
  }
}
