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

package org.echoesfrombeyond.echoesfrombeyond.sigil;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import org.echoesfrombeyond.util.array.ArrayUtil;
import org.junit.jupiter.api.Test;

public class SigilValidationTest {
  private static final int EXPLORE_LEN = 7;

  interface PointVisitor {
    void visit(int x, int y);
  }

  @SuppressWarnings("SameParameterValue")
  void visitPerimeter(int gridSize, PointVisitor visitor) {
    // Visit the north side of the grid.
    for (int i = 0; i < gridSize; i++) {
      visitor.visit(i, 0);
    }

    // Visit the east side of the grid.
    for (int i = 1; i < gridSize; i++) {
      visitor.visit(gridSize - 1, i);
    }

    // Visit the south side of the grid.
    for (int i = gridSize - 2; i >= 0; i--) {
      visitor.visit(i, gridSize - 1);
    }

    // Visit the west side of the grid.
    for (int i = gridSize - 2; i >= 1; i--) {
      visitor.visit(0, i);
    }
  }

  Optional<SigilKey> check(String message, byte[] data, Predicate<Optional<SigilKey>> checker) {
    byte[] originalCopy = Arrays.copyOf(data, data.length);

    Optional<SigilKey> result = SigilValidation.canonicalize(data);

    assertTrue(checker.test(result), message + ": " + Arrays.toString(data));

    result.ifPresent(
        key ->
            assertTrue(
                key.isCanonical(),
                "Result should have been canonical: "
                    + Arrays.toString(originalCopy)
                    + " -> "
                    + key));

    assertArrayEquals(originalCopy, data, "Canonicalization should not mutate the array");

    return result;
  }

  @Test
  public void emptyBytesAreInvalid() {
    check("Empty point array should be invalid", new byte[0], Optional::isEmpty);
  }

  @Test
  public void doesNotEqualNull() {
    SigilKey key =
        check(
                "Should be valid",
                new byte[] {SigilValidation.encodePoint(0, 0), SigilValidation.encodePoint(0, 1)},
                Optional::isPresent)
            .orElseThrow();

    assertNotEquals(null, key, "Should not equal null");
  }

  @Test
  public void oneByteIsInvalid() {
    check(
        "Single point array should be invalid",
        new byte[] {SigilValidation.encodePoint(0, 0)},
        Optional::isEmpty);
  }

  @Test
  public void repetitionIsInvalid() {
    check(
        "Repeating points should be invalid",
        new byte[] {SigilValidation.encodePoint(0, 0), SigilValidation.encodePoint(0, 0)},
        Optional::isEmpty);
  }

  @Test
  public void distantPointsAreInvalid() {
    check(
        "Non-adjacent points should be invalid",
        new byte[] {SigilValidation.encodePoint(0, 0), SigilValidation.encodePoint(0, 2)},
        Optional::isEmpty);
  }

  @Test
  @SuppressWarnings("DataFlowIssue")
  public void tooLargePointIsInvalid() {
    check(
        "Sigil with out-of-bounds point should be invalid",
        new byte[] {SigilValidation.encodePoint(0, SigilValidation.GRID_SIZE)},
        Optional::isEmpty);
  }

  @Test
  public void redrawnLineIsInvalid() {
    byte[] bytes =
        new byte[] {
          SigilValidation.encodePoint(0, 0),
          SigilValidation.encodePoint(0, 1),
          SigilValidation.encodePoint(1, 1),
          SigilValidation.encodePoint(1, 0),
          SigilValidation.encodePoint(0, 0),
          SigilValidation.encodePoint(0, 1),
        };

    check("Sigil with redrawn line should be invalid", bytes, Optional::isEmpty);
  }

  @Test
  public void reversedCanonicalizationIsEquivalent() {
    byte[] bytes =
        new byte[] {
          SigilValidation.encodePoint(0, 0),
          SigilValidation.encodePoint(0, 1),
          SigilValidation.encodePoint(0, 2),
          SigilValidation.encodePoint(0, 3),
        };

    SigilKey first =
        check("Straight light should be valid", bytes, Optional::isPresent).orElseThrow();
    ArrayUtil.reverse(bytes, 0, bytes.length);
    SigilKey second =
        check("Reversed straight line should be valid", bytes, Optional::isPresent).orElseThrow();

    assertEquals(first, second);
  }

  @Test
  public void serializationRoundTrips() throws IOException, ClassNotFoundException {
    SigilKey key =
        check(
                "should have been valid",
                new byte[] {
                  SigilValidation.encodePoint(0, 0),
                  SigilValidation.encodePoint(0, 1),
                  SigilValidation.encodePoint(0, 2),
                  SigilValidation.encodePoint(0, 3),
                },
                Optional::isPresent)
            .orElseThrow();

    ByteArrayOutputStream inner = new ByteArrayOutputStream(4096);
    try (ObjectOutputStream ois = new ObjectOutputStream(inner)) {
      ois.writeObject(key);
      ois.flush();
    }

    try (ObjectInputStream inputStream =
        new ObjectInputStream(new ByteArrayInputStream(inner.toByteArray()))) {
      assertEquals(key, inputStream.readObject(), "deserialized key should equal the original");
    }
  }

  @Test
  public void invalidDataDoesNotDeserialize() throws IOException {
    byte[] badData =
        new byte[] {
          SigilValidation.encodePoint(0, 0),
          SigilValidation.encodePoint(1, 1),

          // this point is too distant from (1, 1)
          SigilValidation.encodePoint(3, 3),
        };

    ByteArrayOutputStream inner = new ByteArrayOutputStream(4096);
    try (ObjectOutputStream ois = new ObjectOutputStream(inner)) {
      ois.writeObject(SigilKey.serializationProxyFromBytes(badData));
      ois.flush();
    }

    try (ObjectInputStream inputStream =
        new ObjectInputStream(new ByteArrayInputStream(inner.toByteArray()))) {
      assertThrows(InvalidObjectException.class, inputStream::readObject);
    }
  }

  @Test
  public void loopCanonicalizationIsEquivalent() {
    int perimeterSize = ((SigilValidation.GRID_SIZE - 2) * 4) + 4;
    byte[] points = new byte[perimeterSize];

    int[] count = new int[1];
    visitPerimeter(
        SigilValidation.GRID_SIZE,
        (x, y) -> points[count[0]++] = SigilValidation.encodePoint(x, y));

    byte[] sigil = new byte[perimeterSize + 1];

    Set<SigilKey> keys = new HashSet<>(perimeterSize);
    for (int i = 0; i < perimeterSize; i++) {
      sigil[sigil.length - 1] = points[i];

      for (int j = 0; j < perimeterSize; j++) sigil[j] = points[(i + j) % perimeterSize];

      SigilKey newKey =
          check("Perimeter loop Sigil should be valid", sigil, Optional::isPresent).orElseThrow();

      ArrayUtil.reverse(sigil, 1, sigil.length - 2);

      SigilKey reversedKey =
          check("Reversed perimeter loop Sigil should be valid", sigil, Optional::isPresent)
              .orElseThrow();

      if ((keys.add(newKey) | keys.add(reversedKey)) && keys.size() > 1)
        fail("All perimeter loop Sigils should be equivalent");
    }
  }

  private record Point(int x, int y) {
    private Point add(Point other) {
      return new Point(x + other.x, y + other.y);
    }

    private boolean inBounds() {
      return x >= 0 && y >= 0 && x < SigilValidation.GRID_SIZE && y < SigilValidation.GRID_SIZE;
    }
  }

  private static final Point[] VALID_MOVES =
      new Point[] {
        new Point(0, 1),
        new Point(1, 1),
        new Point(1, 0),
        new Point(1, -1),
        new Point(0, -1),
        new Point(-1, -1),
        new Point(-1, 0),
        new Point(-1, 1),
      };

  private static class Node {
    private final Point pos;

    private int stage;

    private Node(Point pos) {
      this.pos = pos;
    }

    @Override
    public String toString() {
      return "Node[pos=" + pos + ", stage=" + stage + "]";
    }
  }

  @Test
  public void generatedSigilsAreValid() {
    List<Node> stack = new ArrayList<>(EXPLORE_LEN);

    // Depth-first search of every valid Sigil up to EXPLORE_LEN length.
    for (int i = 0; i < (SigilValidation.GRID_SIZE * SigilValidation.GRID_SIZE); i++) {
      int startX = i % SigilValidation.GRID_SIZE;
      int startY = i / SigilValidation.GRID_SIZE;

      stack.addLast(new Node(new Point(startX, startY)));

      outer:
      while (!stack.isEmpty()) {
        Node cur = stack.getLast();

        if (stack.size() < EXPLORE_LEN) {
          int prevIndex = -1;

          for (int j = stack.size() - 2; j >= 0; j--) {
            if (stack.get(j).pos.equals(cur.pos)) {
              prevIndex = j;
              break;
            }
          }

          inner:
          while (cur.stage < VALID_MOVES.length) {
            Point move = VALID_MOVES[cur.stage++];
            Point candidate = cur.pos.add(move);

            if (!candidate.inBounds()) continue;

            // small loops e.g. (x, y), (a, b), (x, y) aren't allowed
            if (stack.size() > 1 && stack.get(stack.size() - 2).pos.equals(candidate)) continue;

            // we are part of a loop; make sure our candidate doesn't repeat a move
            if (prevIndex >= 0) {
              for (int j = 0; j < stack.size(); j++) {
                if (!stack.get(j).pos.equals(cur.pos)) continue;

                Point firstNeighbor = j > 0 ? stack.get(j - 1).pos : null;
                Point secondNeighbor = j < stack.size() - 1 ? stack.get(j + 1).pos : null;

                if (Objects.equals(candidate, firstNeighbor)
                    || Objects.equals(candidate, secondNeighbor)) continue inner;
              }
            }

            stack.addLast(new Node(candidate));
            continue outer;
          }
        }

        if (stack.size() > 1) {
          byte[] points = new byte[stack.size()];
          for (int j = 0; j < stack.size(); j++) {
            Point point = stack.get(j).pos;
            points[j] = SigilValidation.encodePoint(point.x, point.y);
          }

          check("Should have been valid", points, Optional::isPresent);
        }

        stack.removeLast();
      }
    }
  }
}
