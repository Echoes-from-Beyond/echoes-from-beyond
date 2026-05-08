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

package org.echoesfrombeyond.codechelper.validator;

import com.hypixel.hytale.codec.validation.Validator;
import java.lang.annotation.Annotation;
import org.echoesfrombeyond.codechelper.annotation.validator.ValidatorSpec;
import org.echoesfrombeyond.codechelper.provider.Provider;
import org.jspecify.annotations.NullMarked;

/**
 * Given an annotation and the field it is applied to, attempt to return a specific kind of {@link
 * Validator}. The annotation's values may be used to construct the validator instance.
 *
 * <p>Implementations of this interface are typically referenced as the value of {@link
 * ValidatorSpec} annotations. Such implementations <b>MUST</b> specify a {@code public static}
 * field named {@code INSTANCE}, whose type is the type of the declaring class.
 *
 * <p>By convention, {@code INSTANCE} should be the only instance of the implementation (subclasses
 * notwithstanding); i.e. implementations conventionally follow the singleton pattern.
 *
 * @param <A> the annotation type
 */
@NullMarked
public interface ValidatorProvider<A extends Annotation> extends Provider<Validator<?>, A> {}
