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

package org.echoesfrombeyond.codec.validation;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import org.echoesfrombeyond.asset.SigilPattern;
import org.echoesfrombeyond.codec.SigilPoint;
import org.echoesfrombeyond.sigil.SigilValidation;
import org.jspecify.annotations.NullMarked;

/**
 * Validator that ensures an array of {@link SigilPoint} constitutes a valid {@link SigilPattern}.
 *
 * <p>This class uses a singleton pattern; the instance is {@link CanonicalSigilValidator#INSTANCE}.
 *
 * @see SigilValidation#canonicalize(byte[]) for details on what constitutes a valid sigil
 */
@NullMarked
public class CanonicalSigilValidator implements Validator<SigilPoint[]> {
  /** The singleton instance. */
  public static CanonicalSigilValidator INSTANCE = new CanonicalSigilValidator();

  private CanonicalSigilValidator() {}

  @Override
  public void accept(SigilPoint[] points, ValidationResults validationResults) {
    // There's no obvious way to use the SigilKey that's produced by canonicalization here. So, for
    // now, we are doing duplicate work as we also canonicalize in SigilPattern.
    if (SigilValidation.canonicalize(SigilPoint.encodeArray(points)).isEmpty()) {
      validationResults.fail("Sigils must be canonical");
    }
  }

  @Override
  public void updateSchema(SchemaContext schemaContext, Schema schema) {}
}
