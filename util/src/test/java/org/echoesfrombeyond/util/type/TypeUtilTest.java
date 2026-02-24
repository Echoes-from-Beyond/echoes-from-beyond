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

package org.echoesfrombeyond.util.type;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.junit.jupiter.api.Test;

class TypeUtilTest {
  interface Aif {}

  interface Bif extends Aif {}

  static class A implements Aif {}

  static class B extends A implements Bif {}

  static class C extends B {}

  static class Z {}

  @SuppressWarnings("unused")
  static class Param<K, V> {}

  static class ParamSub<P> extends Param<String, P> {}

  static final Param<Short, Integer> paramField = null;

  @Test
  public void inheritanceDistanceIs0ForSameClass() {
    assertEquals(0, TypeUtil.inheritanceDistance(C.class, C.class));
  }

  @Test
  public void inheritanceDistanceIsNegativeForUnrelatedClass() {
    assertTrue(TypeUtil.inheritanceDistance(Z.class, A.class) < 0);
  }

  @Test
  public void inheritanceDistanceIs1ForCtoB() {
    assertEquals(1, TypeUtil.inheritanceDistance(C.class, B.class));
  }

  @Test
  public void inheritanceDistanceIs1ForBtoA() {
    assertEquals(1, TypeUtil.inheritanceDistance(B.class, A.class));
  }

  @Test
  public void inheritanceDistanceIs2ForCtoA() {
    assertEquals(2, TypeUtil.inheritanceDistance(C.class, A.class));
  }

  @Test
  public void inheritanceDistanceIs3ForCtoAif() {
    assertEquals(3, TypeUtil.inheritanceDistance(C.class, Aif.class));
  }

  @Test
  public void replaceRaw() throws NoSuchFieldException {
    var baseType = TypeUtilTest.class.getDeclaredField("paramField").getGenericType();

    var replaced = TypeUtil.replaceRawType(baseType, ParamSub.class);
    var param = assertInstanceOf(ParameterizedType.class, replaced);

    assertEquals(ParamSub.class, param.getRawType());
    assertArrayEquals(new Type[] {Integer.class}, param.getActualTypeArguments());
    assertEquals(TypeUtilTest.class, param.getOwnerType());
  }
}
