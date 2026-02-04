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
import com.hypixel.hytale.codec.exception.CodecException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class CodecUtil {
  public static final CodecResolver PRIMITIVE_RESOLVER =
      new CodecResolver() {
        private static final Map<Class<?>, Codec<?>> PRIMITIVE_CODEC_MAP =
            Map.ofEntries(
                Map.entry(boolean.class, Codec.BOOLEAN),
                Map.entry(Boolean.class, Codec.BOOLEAN),
                Map.entry(byte.class, Codec.BYTE),
                Map.entry(Byte.class, Codec.BYTE),
                Map.entry(short.class, Codec.SHORT),
                Map.entry(Short.class, Codec.SHORT),
                Map.entry(int.class, Codec.INTEGER),
                Map.entry(Integer.class, Codec.INTEGER),
                Map.entry(float.class, Codec.FLOAT),
                Map.entry(Float.class, Codec.FLOAT),
                Map.entry(long.class, Codec.LONG),
                Map.entry(Long.class, Codec.LONG),
                Map.entry(double.class, Codec.DOUBLE),
                Map.entry(Double.class, Codec.DOUBLE),
                Map.entry(String.class, Codec.STRING));

        @Override
        public Codec<?> resolve(Field field) {
          return PRIMITIVE_CODEC_MAP.get(field.getType());
        }
      };

  private CodecUtil() {}

  public static <T> BuilderCodec<T> modelBuilderCodec(Class<T> model, CodecResolver resolver) {
    try {
      return model0(model, resolver);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> BuilderCodec<T> model0(Class<T> model, CodecResolver resolver)
      throws NoSuchMethodException,
          IllegalAccessException,
          InvocationTargetException,
          InstantiationException {
    var lookup = MethodHandles.publicLookup();
    var constructor = lookup.findConstructor(model, MethodType.methodType(void.class));

    var builder =
        BuilderCodec.builder(
            model,
            () -> {
              try {
                //noinspection unchecked
                return (T) constructor.invoke();
              } catch (Throwable e) {
                throw new CodecException("Problem invoking constructor", e);
              }
            });

    var classDoc = model.getDeclaredAnnotation(Doc.class);
    if (classDoc != null) builder = builder.documentation(classDoc.value());

    var fields = model.getDeclaredFields();
    for (var field : fields) {
      if (field.isAnnotationPresent(Skip.class)) continue;

      int modifiers = field.getModifiers();
      if (Modifier.isFinal(modifiers)
          || !Modifier.isPublic(modifiers)
          || Modifier.isStatic(modifiers)) continue;

      Codec<?> codec;

      var use = field.getDeclaredAnnotation(Use.class);

      if (use != null) codec = use.value().getConstructor().newInstance().codec();
      else {
        codec = resolver.resolve(field);
        if (codec == null) throw new CodecException("Missing codec for field " + field);
      }

      var setter = lookup.unreflectSetter(field);
      var getter = lookup.unreflectGetter(field);

      var key =
          new KeyedCodec<>(
              field.getName(), (Codec<Object>) codec, !field.isAnnotationPresent(Opt.class));

      var fieldBuilder =
          builder.append(
              key,
              (self, value) -> {
                try {
                  setter.invoke(self, value);
                } catch (Throwable e) {
                  throw new CodecException("Problem setting field", e);
                }
              },
              (self) -> {
                try {
                  return getter.invoke(self);
                } catch (Throwable e) {
                  throw new CodecException("Problem getting field", e);
                }
              });

      var fieldDoc = field.getDeclaredAnnotation(Doc.class);
      if (fieldDoc != null) fieldBuilder = fieldBuilder.documentation(fieldDoc.value());

      builder = fieldBuilder.add();
    }

    return builder.build();
  }
}
