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

package org.echoesfrombeyond.sigil;

import java.util.Arrays;
import java.util.Optional;
import org.echoesfrombeyond.util.ArrayUtil;

/** {@link SigilKey} static utilities and factory functions. */
public final class SigilValidation {
  /**
   * Size of the Sigil grid. Should always be a power of 2. The largest supported value for this
   * field is 16.
   */
  public static final int GRID_SIZE = 4;

  /** There's a hard limit of 32 points in a valid Sigil. */
  public static final int MAX_SIGIL_LENGTH = 32;

  private static final byte X_MASK = (byte) 0xF0;

  private static final byte X_SHIFT = 4;
  private static final byte Y_MASK = (byte) 0x0F;

  private static final byte COMPACT_SHIFT =
      (byte) (X_SHIFT - Integer.numberOfTrailingZeros(GRID_SIZE));

  private static final int TABLE_SIZE = GRID_SIZE * GRID_SIZE;

  private SigilValidation() {}

  static int compactKey(byte point) {
    // One of the many footguns to watch out for when doing bit hacking shenanigans in Java: e.g.
    // casting the all 1s byte (-1) to int will result in an all 1s int (still -1), which WILL lead
    // to the wrong result here (we actually want 255...)
    int unsigned = Byte.toUnsignedInt(point);

    // With a Sigil grid of 16 possible points, we can get away with only using the least
    // significant 4 bits for our key.
    return (unsigned >>> COMPACT_SHIFT) | (unsigned & Y_MASK);
  }

  static boolean pointOutsideGrid(byte point) {
    return unpackX(point) >= GRID_SIZE || unpackY(point) >= GRID_SIZE;
  }

  static byte unpackX(byte point) {
    return (byte) ((point & X_MASK) >>> X_SHIFT);
  }

  static byte unpackY(byte point) {
    return (byte) (point & Y_MASK);
  }

  /**
   * Validates and canonicalizes a Sigil, whose points are given as an array of bytes, where each
   * element represents a single point (x and y). {@code points} will not be mutated by this process
   * and may be reused as necessary. This method is built to safely handle untrusted input.
   *
   * <p>The exact method used to encode coordinates visited by the Sigil is not specified, though it
   * will always be the same as {@link SigilValidation#encodePoint(int, int)}, which should be used
   * whenever necessary to construct ad hoc Sigils.
   *
   * <p>Sigils should be thought of as arrays of integer (x, y) coordinates, where (0, 0) is the
   * upper-left corner of the grid, y increasing while moving down, x increasing while moving right.
   * Given this model, this method will ensure the point array upholds certain invariants, namely:
   *
   * <ul>
   *   <li>Negative coordinates are not allowed
   *   <li>Length cannot exceed {@link SigilValidation#MAX_SIGIL_LENGTH}
   *   <li>Coordinates may not exceed a grid of size {@link SigilValidation#GRID_SIZE}
   *   <li>Identical coordinates may not appear adjacent to each other
   *   <li>No two pairs of adjacent points may appear more than once (i.e. lines cannot be drawn
   *       over more than once)
   *   <li>Adjacent coordinates must not differ by more than 1 along the x or y axes
   * </ul>
   *
   * <h4>Canonicalization</h4>
   *
   * The canonicalization process ensures that identical Sigils produce equal {@link SigilKey}s,
   * regardless of the order in which they were drawn.
   *
   * <p>This process is opaque and not specified, but it is guaranteed to be consistent across
   * platforms and through reboots of the JVM. For example, storing {@link SigilKey}s and
   * deserializing them later is supported.
   *
   * @param sigil the points making up the Sigil, in compact form (see {@link
   *     SigilValidation#encodePoint(int, int)}).
   * @return an empty {@link Optional} if {@code points} does not uphold the required invariants;
   *     otherwise a {@link SigilKey} representing the canonical form of the specified Sigil
   */
  public static Optional<SigilKey> canonicalize(byte[] sigil) {
    // Sigils must always be at least 2 points and less than or equal to MAX_SIGIL_LENGTH. Also,
    // this is an implicit null check for `sigil`.
    if (sigil.length <= 1 || sigil.length > MAX_SIGIL_LENGTH) return Optional.empty();

    byte[] points = Arrays.copyOf(sigil, sigil.length);

    if (pointOutsideGrid(points[0])) return Optional.empty();

    // Check some of our invariants early, namely:
    // - No two points next to each other are the same
    // - No point is outside the grid
    for (int i = 1; i < points.length; i++) {
      byte first = points[i - 1];
      byte second = points[i];

      if (first == second
          || pointOutsideGrid(second)
          || Math.abs(unpackX(first) - unpackX(second)) > 1
          || Math.abs(unpackY(first) - unpackY(second)) > 1) return Optional.empty();
    }

    byte first = points[0];
    byte last = points[points.length - 1];

    // If we loop on ourselves, we may start at any point. So to canonicalize, we rearrange the
    // array in-place so that the starting index is 0.
    //
    // If we don't loop on ourselves, we can only start from one of two points. We therefore need to
    // reverse the entire array if the starting index is larger than the ending one.
    //
    // Reversing the whole array like this is always fine because it does not change which nodes
    // neighbor each other, and thus cannot violate any of our invariants.
    if (first == last) {
      int smallestIndex = -1;

      // We treat this as an unsigned byte for the purposes of comparison! Therefore -1 can be
      // thought of as 255; the all 1s bit pattern.
      byte smallest = -1;

      for (int i = 0; i < points.length; i++) {
        byte sample = points[i];

        if (Byte.compareUnsigned(sample, smallest) < 0) {
          smallestIndex = i;
          smallest = sample;
        }
      }

      // Even on a 16x16 grid where the largest value is 0xFF, our Sigil must have at least 2
      // points, and these cannot be identical because we checked that no two adjacent points are
      // the same. So `smallestIndex` will always be assigned a nonzero value.
      assert smallestIndex > -1;

      // If the starting index is already the smallest, we don't need to do anything here.
      //
      // Otherwise, we use a fairly complex algorithm to rearrange the points array while upholding
      // the invariants we care about:
      // - Duplicate invariant: no two identical points may appear adjacent to each other
      // - Distance invariant: no two adjacent points may differ by more than 1 unit in the x or y
      //   axes
      //
      // The following presents a conceptual overview. The actual algorithm used differs slightly in
      // the details for performance reasons.
      //
      // The first step is simple: we take all points from the smallest point `S` to the ending
      // point `E`, and put all of them directly before the rest of the points in the array.
      //
      // But, now our duplicate invariant is violated: we just placed the old first point right next
      // to the old end point. We know these are the same because we checked `first` == `last` in
      // the condition above.
      //
      // However, we can actually just delete the one of the duplicate points: we already checked
      // that the points next to both duplicates uphold the distance invariant; and none of their
      // neighbors change as a result of this deletion.
      //
      // Due to the above deletion, we have to shift down all points greater than it to avoid
      // leaving a gap in the middle, but this leaves a gap of 1 at the very end of our array.
      //
      // How do we fill in this gap? The only thing we know about the singular node adjacent to the
      // gap is that it used to be adjacent to the smallest node that we just searched for, which is
      // also the node currently at the beginning of the array.
      //
      // Since we want our start and end nodes to be the same to preserve the fact that this Sigil
      // is a complete loop, we can actually just copy the starting node to the gap. This won't
      // violate the distance invariant because the node just before the gap was previously adjacent
      // to the node now at the beginning of the array, and we already checked that pair of points
      // for the appropriate distance.
      if (smallestIndex > 0) {
        byte[] newStartSequence = new byte[points.length - smallestIndex];

        // Copy all points starting from `startIndex` (inclusive).
        System.arraycopy(points, smallestIndex, newStartSequence, 0, newStartSequence.length);

        // Move the points at the beginning to the end, leaving enough room for `newStartSequence`.
        // But skip the first point to avoid needing to delete it later.
        System.arraycopy(points, 1, points, newStartSequence.length, smallestIndex - 1);

        // Move in the new starting points.
        System.arraycopy(newStartSequence, 0, points, 0, newStartSequence.length);

        // Preserve the loop-ness of the Sigil.
        points[points.length - 1] = smallest;
      }
    } else if (Byte.compareUnsigned(first, last) > 0) ArrayUtil.reverse(points, 0, points.length);

    // Finding loops is really the same as finding pairs of duplicate values.
    //
    // We want to find all duplicate values that are more than 2 indices apart from each other. This
    // is because there is no point reversing a sequence of 3 points: there is only one way to
    // represent (0, 0), (0, 1), (0, 0) for example. Also, we already ensured that identical points
    // aren't next to each other.
    //
    // Therefore, the smallest loop we care about looks like this: (x, y), (a, b), (c, d), (x, y).
    //
    // `duplicates` is an integer array with a special bit representation: the index of every set
    // bit, where the least significant is seen as index 0, is an index at which a particular point
    // was found.
    //
    // To find the duplicate locations for a given point, the `compactKey` method is used to
    // generate a key. Since this uses a perfect hashing scheme, there is no need to do any sort of
    // probing.
    int[] duplicates = new int[TABLE_SIZE];

    for (int i = 0; i < points.length; i++) {
      // Compact the byte so we don't have to make our perfect hash table too large.
      duplicates[compactKey(points[i])] |= (1 << i);
    }

    // Check for redrawn lines. We take advantage of the index information we recorded in
    // `duplicates` to avoid unnecessary work: namely, we can look up exactly where duplicates occur
    // and thus avoid redundant searches.
    for (int data : duplicates) {
      // The number of 1's in the binary representation of `data` is the number of duplicates we
      // have to check. The index of each set 1 is an index into the point array.
      //
      // Every iteration of this loop trims the highest set bit off of `data`.
      //
      // If `duplicateCount` is 1 or 0, that means we have, respectively, 1 or 0 instances of the
      // corresponding point in `points`, and thus there is no point trying to check for redrawn
      // lines.
      for (int i = Integer.bitCount(data); i > 1; i--) {
        int mask = Integer.highestOneBit(data);
        int firstIndex = Integer.numberOfTrailingZeros(mask);

        // This just "turns off" the highest set bit, so we don't try to visit it again on
        // subsequent iterations.
        data ^= mask;

        // This value is mutated by the loop below. We don't want to clobber the value of `data` for
        // future iterations.
        int basis = data;

        // We use do-while here strictly because the outer loop already ensures that if we reach
        // this point, `basis` is non-zero even after trimming the highest bit.
        do {
          // `secondIndex` is always smaller than firstIndex.
          int basisMask = Integer.highestOneBit(basis);
          int secondIndex = Integer.numberOfTrailingZeros(basisMask);

          basis ^= basisMask;

          // This number can be thought of as a perfect hash set with a restricted input: it can
          // only contain integers in range [0, 16).
          //
          // Using a single number like this lets us massively cut down on the number of branches.
          //
          // NOTE: the type of this variable MUST be widened if the table size constant increases!
          // For example, a 16x16 table would necessitate this be changed to a long.
          short set = 0;

          // Since we know that secondIndex < firstIndex, and both are valid indices:
          // - secondIndex is not the last index
          // - firstIndex is not the first index
          //
          // This means we don't need to do bounds checks on these particular array accesses.
          set |= (short) (1 << compactKey(points[firstIndex - 1]));
          set |= (short) (1 << compactKey(points[secondIndex + 1]));

          // This is how may 1s we expect to find in `set`. If the observed count of 1s differs from
          // `expected`, we have identified a retraced line.
          int expected = 2;

          // Check if firstIndex is the last element. If it is the last element, there's nothing to
          // compare with. If it isn't, we have an additional neighbor to check. We expect this
          // neighbor to be unique, so increment our expected count.
          if (firstIndex < points.length - 1) {
            set |= (short) (1 << compactKey(points[firstIndex + 1]));
            expected++;
          }

          // Same as the above, except checking the lower neighbor of secondIndex.
          if (secondIndex > 0) {
            set |= (short) (1 << compactKey(points[secondIndex - 1]));
            expected++;
          }

          if (Integer.bitCount(Short.toUnsignedInt(set)) != expected) return Optional.empty();
        } while (basis > 0);
      }
    }

    // Using `duplicates` as a guide to figure out what points are actually duplicates, iterate
    // through the Sigil.
    //
    // We start at index 3 because we check backwards for loops. Remember that we only care about
    // loops that span at least 3 indices; a loop like (0, 0), (0, 1), (0, 0) never needs to be
    // reversed, and so we can skip the first 3 points.
    //
    // Note that we do NOT use the index information encoded in the values of `duplicates`! This is
    // deliberate, as we may change the indices of one or more duplicate entries, in the process of
    // canonicalizing loops, which would render the data in `duplicates` out-of-date.
    for (int i = 3; i < points.length; i++) {
      byte point = points[i];

      // Ignore points for which there are no duplicates, meaning they are not at the intersection
      // point of a loop. This lets us avoid unnecessary backtracking.
      if (Integer.bitCount(duplicates[compactKey(point)]) <= 1) continue;

      // `point` is part of a loop. Backtrack through the Sigil looking for its nearest duplicate.
      for (int j = i - 3; j >= 0; j--) {
        // If we found the duplicate, check that the loop is in canonical order. If it's already in
        // canonical order, we don't need to do anything.
        if (points[j] != point || (Byte.compareUnsigned(points[j + 1], points[i - 1]) <= 0))
          continue;

        // The loop isn't in canonical order. So we have to reverse it.
        ArrayUtil.reverse(points, j + 1, i - j - 1);

        // Reversing the above sequence may have broken the canonical ordering of other loops. So we
        // revert `i` to the index of the duplicate we encountered.
        //
        // Note that `i` will immediately increment again as soon as flow returns to the outer loop.
        // So we are really reverting to index `j + 1`; which is the lowest index that was actually
        // modified by the loop.
        i = j;

        break;
      }
    }

    return Optional.of(new SigilKey(points));
  }

  /**
   * Encodes an integer coordinate pair into a single byte, which can then be stored in an array
   * suitable for passing to {@link SigilValidation#canonicalize(byte[])}.
   *
   * <p>Note that this function does not perform validation on the coordinates. Points are validated
   * during canonicalization. All possible values for (x, y) may be passed to this function without
   * issue.
   *
   * @param x the x-coordinate of this point in the Sigil
   * @param y the y-coordinate of this point in the Sigil
   * @return the encoded point
   */
  public static byte encodePoint(int x, int y) {
    return (byte) (((x << X_SHIFT) & X_MASK) | (y & Y_MASK));
  }
}
