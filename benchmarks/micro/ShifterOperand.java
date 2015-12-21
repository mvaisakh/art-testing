/*
 *    Copyright 2015 Linaro Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package benchmarks.micro;


/*
 * Description: Tests around binary operations taking shifts or type conversions
 * as inputs that can be merged into the shifter operand.
 *
 * Main Focus: shifter operand
 *
 * Secondary Focus: instruction scheduling
 *
 * Some comments in this file assume that shift or extend operations are merged
 * into the shifter operand of the binary operations using them.
 */

// We declare many temporary local variables with similar names. Avoid the extra
// lines that would be required with one declaration per line.
// CHECKSTYLE.OFF: MultipleVariableDeclarations

public class ShifterOperand {
  public static int timeIntSingleUseLatency1(int iterations) {
    int t1 = 0;
    for (int iter = 0; iter < iterations; iter++) {
      // Each bitfield operation has a single use.
      // Results are used one instruction after being produced.
      t1 |= iter >> 1;
      t1 &= iter << 2;
      t1 ^= iter >>> 3;
      t1 += (byte)iter;
      t1 -= (char)iter;
      t1 += (short)iter;
    }
    return t1;
  }

  public static boolean verifyIntSingleUseLatency1() {
    final int expected = 14;
    int found = timeIntSingleUseLatency1(10);
    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  // The `ThreadN` suffix indicates that the loop contains `N` independent
  // computation threads that execute (almost exactly) the same thing.  The
  // multiple threads allow for instruction scheduling to kick in.
  public static int timeIntSingleUseLatency1Thread2(int iterations) {
    int t1 = 0, t2 = 1;
    for (int iter1 = 0, iter2 = 1;
         iter1 < iterations;
         iter1++, iter2++) {
      // Each bitfield operation has a single use.
      // Results are used one instruction after being produced.

      // Computation thread 1.
      t1 |= iter1 >> 1;
      t1 &= iter1 << 2;
      t1 ^= iter1 >>> 3;
      t1 += (byte)iter1;
      t1 -= (char)iter1;
      t1 += (short)iter1;

      // Computation thread 2.
      t2 |= iter2 >> 1;
      t2 &= iter2 << 2;
      t2 ^= iter2 >>> 3;
      t2 += (byte)iter2;
      t2 -= (char)iter2;
      t2 += (short)iter2;
    }
    return t1;
  }

  public static boolean verifyIntSingleUseLatency1Thread2() {
    final int expected = 14;
    int found = timeIntSingleUseLatency1Thread2(10);
    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  public static int timeIntSingleUseLatency1Thread3(int iterations) {
    int t1 = 0, t2 = 1, t3 = 2;
      // Each bitfield operation has a single use.
      // Results are used one instruction after being produced.

    for (int iter1 = 0, iter2 = 1, iter3 = 2;
         iter1 < iterations;
         iter1++, iter2++, iter3++) {
      // Computation thread 1.
      t1 |= iter1 >> 1;
      t1 &= iter1 << 2;
      t1 ^= iter1 >>> 3;
      t1 += (byte)iter1;
      t1 -= (char)iter1;
      t1 += (short)iter1;

      // Computation thread 2.
      t2 |= iter2 >> 1;
      t2 &= iter2 << 2;
      t2 ^= iter2 >>> 3;
      t2 += (byte)iter2;
      t2 -= (char)iter2;
      t2 += (short)iter2;

      // Computation thread 3.
      t3 |= iter3 >> 1;
      t3 &= iter3 << 2;
      t3 ^= iter3 >>> 3;
      t3 += (byte)iter3;
      t3 -= (char)iter3;
      t3 += (short)iter3;
    }
    return t1;
  }

  public static boolean verifyIntSingleUseLatency1Thread3() {
    final int expected = 14;
    int found = timeIntSingleUseLatency1Thread3(10);
    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  public static int timeIntSingleUseLatency1Thread4(int iterations) {
    int t1 = 0, t2 = 1, t3 = 2, t4 = 3;
      // Each bitfield operation has a single use.
      // Results are used one instruction after being produced.

    for (int iter1 = 0, iter2 = 1, iter3 = 2, iter4 = 3;
         iter1 < iterations;
         iter1++, iter2++, iter3++, iter4++) {
      // Computation thread 1.
      t1 |= iter1 >> 1;
      t1 &= iter1 << 2;
      t1 ^= iter1 >>> 3;
      t1 += (byte)iter1;
      t1 -= (char)iter1;
      t1 += (short)iter1;

      // Computation thread 2.
      t2 |= iter2 >> 1;
      t2 &= iter2 << 2;
      t2 ^= iter2 >>> 3;
      t2 += (byte)iter2;
      t2 -= (char)iter2;
      t2 += (short)iter2;

      // Computation thread 3.
      t3 |= iter3 >> 1;
      t3 &= iter3 << 2;
      t3 ^= iter3 >>> 3;
      t3 += (byte)iter3;
      t3 -= (char)iter3;
      t3 += (short)iter3;

      // Computation thread 4.
      t4 |= iter4 >> 1;
      t4 &= iter4 << 2;
      t4 ^= iter4 >>> 3;
      t4 += (byte)iter4;
      t4 -= (char)iter4;
      t4 += (short)iter4;
    }
    return t1;
  }

  public static boolean verifyIntSingleUseLatency1Thread4() {
    final int expected = 14;
    int found = timeIntSingleUseLatency1Thread4(10);
    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  public static int timeIntSingleUseLatency2(int iterations) {
    int t1 = 0, t2 = 0;
    for (int iter = 0; iter < iterations; iter++) {
      // Each bitfield operation has a single use.
      // Results are used two instructions after being produced.
      t1 |= iter >> 1;
      t2 &= iter << 2;
      t1 ^= iter >>> 3;
      t2 += (byte)iter;
      t1 -= (char)iter;
      t2 += (short)iter;
    }
    return t1 | t2;
  }

  public static boolean verifyIntSingleUseLatency2() {
    final int expected = -5;
    int found = timeIntSingleUseLatency2(10);
    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  public static int timeIntSingleUseLatency3(int iterations) {
    int t1 = 0, t2 = 0, t3 = 0;
    for (int iter = 0; iter < iterations; iter++) {
      // Each bitfield operation has a single use.
      // Results are used three instructions after being produced.
      t1 |= iter >> 1;
      t2 &= iter << 2;
      t3 ^= iter >>> 3;
      t1 += (byte)iter;
      t2 -= (char)iter;
      t3 += (short)iter;
    }
    return t1 | t2;
  }

  public static boolean verifyIntSingleUseLatency3() {
    final int expected = -1;
    int found = timeIntSingleUseLatency3(10);
    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  public static int timeIntSingleUseLatencyLoop(int iterations) {
    int t1 = 0, t2 = 0, t3 = 0, t4 = 0, t5 = 0, t6 = 0;
    for (int iter = 0; iter < iterations; iter++) {
      // Each bitfield operation has a single use.
      // Results are only used in the next iteration of the loop.
      t1 |= iter >> 1;
      t2 &= iter << 2;
      t3 ^= iter >>> 3;
      t4 += (byte)iter;
      t5 -= (char)iter;
      t6 += (short)iter;
    }
    return t1 | t2 | t3 | t4 | t5 | t6;
  }

  public static boolean verifyIntSingleUseLatencyLoop() {
    final int expected = -1;
    int found = timeIntSingleUseLatencyLoop(10);
    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  public static long timeLongSingleUseLatency1(long iterations) {
    long t1 = 0;
    for (long iter = 0; iter < iterations; iter++) {
      // Each bitfield operation has a single use.
      // Results are used one instruction after being produced.
      t1 |= iter >> 1;
      t1 &= iter << 2;
      t1 ^= iter >>> 3;
      t1 += (byte)iter;
      t1 -= (char)iter;
      t1 += (short)iter;
    }
    return t1;
  }

  public static boolean verifyLongSingleUseLatency1() {
    long expected = 14;
    long found = timeLongSingleUseLatency1(10);
    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  // The `ThreadN` suffix indicates that the loop contains `N` independent
  // computation threads that execute (almost exactly) the same thing.  The
  // multiple threads allow for instruction scheduling to kick in.
  public static long timeLongSingleUseLatency1Thread2(long iterations) {
    long t1 = 0, t2 = 1;
    for (long iter1 = 0, iter2 = 1;
         iter1 < iterations;
         iter1++, iter2++) {
      // Each bitfield operation has a single use.
      // Results are used one instruction after being produced.

      // Computation thread 1.
      t1 |= iter1 >> 1;
      t1 &= iter1 << 2;
      t1 ^= iter1 >>> 3;
      t1 += (byte)iter1;
      t1 -= (char)iter1;
      t1 += (short)iter1;

      // Computation thread 2.
      t2 |= iter2 >> 1;
      t2 &= iter2 << 2;
      t2 ^= iter2 >>> 3;
      t2 += (byte)iter2;
      t2 -= (char)iter2;
      t2 += (short)iter2;
    }
    return t1;
  }

  public static boolean verifyLongSingleUseLatency1Thread2() {
    long expected = 14;
    long found = timeLongSingleUseLatency1Thread2(10);
    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  public static long timeLongSingleUseLatency1Thread3(long iterations) {
    long t1 = 0, t2 = 1, t3 = 2;
      // Each bitfield operation has a single use.
      // Results are used one instruction after being produced.

    for (long iter1 = 0, iter2 = 1, iter3 = 2;
         iter1 < iterations;
         iter1++, iter2++, iter3++) {
      // Computation thread 1.
      t1 |= iter1 >> 1;
      t1 &= iter1 << 2;
      t1 ^= iter1 >>> 3;
      t1 += (byte)iter1;
      t1 -= (char)iter1;
      t1 += (short)iter1;

      // Computation thread 2.
      t2 |= iter2 >> 1;
      t2 &= iter2 << 2;
      t2 ^= iter2 >>> 3;
      t2 += (byte)iter2;
      t2 -= (char)iter2;
      t2 += (short)iter2;

      // Computation thread 3.
      t3 |= iter3 >> 1;
      t3 &= iter3 << 2;
      t3 ^= iter3 >>> 3;
      t3 += (byte)iter3;
      t3 -= (char)iter3;
      t3 += (short)iter3;
    }
    return t1;
  }

  public static boolean verifyLongSingleUseLatency1Thread3() {
    long expected = 14;
    long found = timeLongSingleUseLatency1Thread3(10);
    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  public static long timeLongSingleUseLatency1Thread4(long iterations) {
    long t1 = 0, t2 = 1, t3 = 2, t4 = 3;
      // Each bitfield operation has a single use.
      // Results are used one instruction after being produced.

    for (long iter1 = 0, iter2 = 1, iter3 = 2, iter4 = 3;
         iter1 < iterations;
         iter1++, iter2++, iter3++, iter4++) {
      // Computation thread 1.
      t1 |= iter1 >> 1;
      t1 &= iter1 << 2;
      t1 ^= iter1 >>> 3;
      t1 += (byte)iter1;
      t1 -= (char)iter1;
      t1 += (short)iter1;

      // Computation thread 2.
      t2 |= iter2 >> 1;
      t2 &= iter2 << 2;
      t2 ^= iter2 >>> 3;
      t2 += (byte)iter2;
      t2 -= (char)iter2;
      t2 += (short)iter2;

      // Computation thread 3.
      t3 |= iter3 >> 1;
      t3 &= iter3 << 2;
      t3 ^= iter3 >>> 3;
      t3 += (byte)iter3;
      t3 -= (char)iter3;
      t3 += (short)iter3;

      // Computation thread 4.
      t4 |= iter4 >> 1;
      t4 &= iter4 << 2;
      t4 ^= iter4 >>> 3;
      t4 += (byte)iter4;
      t4 -= (char)iter4;
      t4 += (short)iter4;
    }
    return t1;
  }

  public static boolean verifyLongSingleUseLatency1Thread4() {
    long expected = 14;
    long found = timeLongSingleUseLatency1Thread4(10);
    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  public static long timeLongSingleUseLatency2(long iterations) {
    long t1 = 0, t2 = 0;
    for (long iter = 0; iter < iterations; iter++) {
      // Each bitfield operation has a single use.
      // Results are used two instructions after being produced.
      t1 |= iter >> 1;
      t2 &= iter << 2;
      t1 ^= iter >>> 3;
      t2 += (byte)iter;
      t1 -= (char)iter;
      t2 += (short)iter;
    }
    return t1 | t2;
  }

  public static boolean verifyLongSingleUseLatency2() {
    long expected = -5;
    long found = timeLongSingleUseLatency2(10);
    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  public static long timeLongSingleUseLatency3(long iterations) {
    long t1 = 0, t2 = 0, t3 = 0;
    for (long iter = 0; iter < iterations; iter++) {
      // Each bitfield operation has a single use.
      // Results are used three instructions after being produced.
      t1 |= iter >> 1;
      t2 &= iter << 2;
      t3 ^= iter >>> 3;
      t1 += (byte)iter;
      t2 -= (char)iter;
      t3 += (short)iter;
    }
    return t1 | t2;
  }

  public static boolean verifyLongSingleUseLatency3() {
    long expected = -1;
    long found = timeLongSingleUseLatency3(10);
    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  public static long timeLongSingleUseLatencyLoop(long iterations) {
    long t1 = 0, t2 = 0, t3 = 0, t4 = 0, t5 = 0, t6 = 0;
    for (long iter = 0; iter < iterations; iter++) {
      // Each bitfield operation has a single use.
      // Results are only used in the next iteration of the loop.
      t1 |= iter >> 1;
      t2 &= iter << 2;
      t3 ^= iter >>> 3;
      t4 += (byte)iter;
      t5 -= (char)iter;
      t6 += (short)iter;
    }
    return t1 | t2 | t3 | t4 | t5 | t6;
  }

  public static boolean verifyLongSingleUseLatencyLoop() {
    long expected = -1;
    long found = timeLongSingleUseLatencyLoop(10);
    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  public static int timeMultipleUsesDifferentPathInt(int iterations) {
    int res = 0;
    for (int iter = 0; iter < iterations; iter++) {
      // Each bitfield operation has two uses on different paths.
      int temp1 = iter >> 1;
      int temp2 = iter << 2;
      int temp3 = iter >>> 3;
      int temp4 = (byte)iter;
      int temp5 = (char)iter;
      int temp6 = (short)iter;
      int temp7 = (int)iter;
      // The condition is true most of the time, so the branch predictor should
      // predict it correctly.
      if (iter > 1) {
        res += (((((temp1 | temp2) & temp3) ^ temp4) + temp5) - temp6) + temp7;
      } else {
        res += (((((temp1 + temp2) | temp3) & temp4) & temp5) + temp6) - temp7;
      }
    }
    return res;
  }

  public static boolean verifyMultipleUsesDifferentPathInt() {
    final int expected = 88;
    int found = timeMultipleUsesDifferentPathInt(10);
    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  public static long timeMultipleUsesDifferentPathLong(int iterations) {
    long res = 0;
    for (long iter = 0; iter < iterations; iter++) {
      // Each bitfield operation has two uses on different paths.
      long temp1 = iter >> 1;
      long temp2 = iter << 2;
      long temp3 = iter >>> 3;
      long temp4 = (byte)iter;
      long temp5 = (char)iter;
      long temp6 = (short)iter;
      long temp7 = (int)iter;
      // The condition is true most of the time, so the branch predictor should
      // predict it correctly.
      if (iter > 1) {
        res += (((((temp1 | temp2) & temp3) ^ temp4) + temp5) - temp6) + temp7;
      } else {
        res += (((((temp1 + temp2) | temp3) & temp4) & temp5) + temp6) - temp7;
      }
    }
    return res;
  }

  public static boolean verifyMultipleUsesDifferentPathLong() {
    long expected = 88;
    long found = timeMultipleUsesDifferentPathLong(10);
    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  public static int timeMultipleSuccessiveUsesSamePathInt(int iterations) {
    int res = 0;
    for (int iter = 0; iter < iterations; iter++) {
      // Each bitfield operation has two successive uses on the same path.
      int temp1 = iter >> 1;
      res += temp1;
      res += temp1;
      int temp2 = iter << 2;
      res += temp2;
      res += temp2;
      int temp3 = iter >>> 3;
      res += temp3;
      res += temp3;
      int temp4 = (byte)iter;
      res += temp4;
      res += temp4;
      int temp5 = (char)iter;
      res += temp5;
      res += temp5;
      int temp6 = (short)iter;
      res += temp6;
      res += temp6;
      int temp7 = (int)iter;
      res += temp7;
      res += temp7;
    }
    return res;
  }

  public static boolean verifyMultipleSuccessiveUsesSamePathInt() {
    final int expected = 764;
    int found = timeMultipleSuccessiveUsesSamePathInt(10);
    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  public static long timeMultipleSuccessiveUsesSamePathLong(int iterations) {
    long res = 0;
    for (long iter = 0; iter < iterations; iter++) {
      // Each bitfield operation has two successive uses on the same path.
      long temp1 = iter >> 1;
      res += temp1;
      res += temp1;
      long temp2 = iter << 2;
      res += temp2;
      res += temp2;
      long temp3 = iter >>> 3;
      res += temp3;
      res += temp3;
      long temp4 = (byte)iter;
      res += temp4;
      res += temp4;
      long temp5 = (char)iter;
      res += temp5;
      res += temp5;
      long temp6 = (short)iter;
      res += temp6;
      res += temp6;
      long temp7 = (int)iter;
      res += temp7;
      res += temp7;
    }
    return res;
  }

  public static boolean verifyMultipleSuccessiveUsesSamePathLong() {
    long expected = 764;
    long found = timeMultipleSuccessiveUsesSamePathLong(10);
    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  public static int timeMultipleSeparatedUsesSamePathInt(int iterations) {
    int res = 0;
    for (int iter = 0; iter < iterations; iter++) {
      // Each bitfield operation has two separated uses on the same path.
      int temp1 = iter >> 1;
      res += temp1;
      int temp2 = iter << 2;
      res += temp2;
      res += temp1;
      int temp3 = iter >>> 3;
      res += temp3;
      res += temp2;
      int temp4 = (byte)iter;
      res += temp4;
      res += temp3;
      int temp5 = (char)iter;
      res += temp5;
      res += temp4;
      int temp6 = (short)iter;
      res += temp6;
      res += temp5;
      int temp7 = (int)iter;
      res += temp7;
      res += temp6;
      res += temp7;
    }
    return res;
  }

  public static boolean verifyMultipleSeparatedUsesSamePathInt() {
    final int expected = 764;
    int found = timeMultipleSeparatedUsesSamePathInt(10);
    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  public static long timeMultipleSeparatedUsesSamePathLong(int iterations) {
    long res = 0;
    for (long iter = 0; iter < iterations; iter++) {
      // Each bitfield operation has two separated uses on the same path.
      long temp1 = iter >> 1;
      res += temp1;
      long temp2 = iter << 2;
      res += temp2;
      res += temp1;
      long temp3 = iter >>> 3;
      res += temp3;
      res += temp2;
      long temp4 = (byte)iter;
      res += temp4;
      res += temp3;
      long temp5 = (char)iter;
      res += temp5;
      res += temp4;
      long temp6 = (short)iter;
      res += temp6;
      res += temp5;
      long temp7 = (int)iter;
      res += temp7;
      res += temp6;
      res += temp7;
    }
    return res;
  }

  public static boolean verifyMultipleSeparatedUsesSamePathLong() {
    long expected = 764;
    long found = timeMultipleSeparatedUsesSamePathLong(10);
    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  public static void main(String[] args) {
    int rc = 0;
    int iterations = 100000;
    long start;
    long end;

    String prefix = "benchmarks/micro/ShifterOperand/";

    // CHECKSTYLE.OFF: LineLength
    // CHECKSTYLE.OFF: OneStatementPerLine
    // CHECKSTYLE.OFF: LeftCurly
    start = System.currentTimeMillis(); timeIntSingleUseLatency1(500000); end = System.currentTimeMillis();
    if (!verifyIntSingleUseLatency1()) { rc++; }
    System.out.println(prefix + "IntSingleUseLatency1: " + (end - start));
    start = System.currentTimeMillis(); timeIntSingleUseLatency1Thread2(300000); end = System.currentTimeMillis();
    if (!verifyIntSingleUseLatency1Thread2()) { rc++; }
    System.out.println(prefix + "IntSingleUseLatency1Thread2: " + (end - start));
    start = System.currentTimeMillis(); timeIntSingleUseLatency1Thread3(200000); end = System.currentTimeMillis();
    if (!verifyIntSingleUseLatency1Thread3()) { rc++; }
    System.out.println(prefix + "IntSingleUseLatency1Thread3: " + (end - start));
    start = System.currentTimeMillis(); timeIntSingleUseLatency1Thread4(150000); end = System.currentTimeMillis();
    if (!verifyIntSingleUseLatency1Thread4()) { rc++; }
    System.out.println(prefix + "IntSingleUseLatency1Thread4: " + (end - start));
    start = System.currentTimeMillis(); timeIntSingleUseLatency2(400000); end = System.currentTimeMillis();
    if (!verifyIntSingleUseLatency2()) { rc++; }
    System.out.println(prefix + "IntSingleUseLatency2: " + (end - start));
    start = System.currentTimeMillis(); timeIntSingleUseLatency3(500000); end = System.currentTimeMillis();
    if (!verifyIntSingleUseLatency3()) { rc++; }
    System.out.println(prefix + "IntSingleUseLatency3: " + (end - start));
    start = System.currentTimeMillis(); timeIntSingleUseLatencyLoop(700000); end = System.currentTimeMillis();
    if (!verifyIntSingleUseLatencyLoop()) { rc++; }
    System.out.println(prefix + "IntSingleUseLatencyLoop: " + (end - start));

    // The `long` versions use the same iteration counts as the `int` versions
    // above.
    start = System.currentTimeMillis(); timeLongSingleUseLatency1(500000); end = System.currentTimeMillis();
    if (!verifyLongSingleUseLatency1()) { rc++; }
    System.out.println(prefix + "LongSingleUseLatency1: " + (end - start));
    start = System.currentTimeMillis(); timeLongSingleUseLatency1Thread2(300000); end = System.currentTimeMillis();
    if (!verifyLongSingleUseLatency1Thread2()) { rc++; }
    System.out.println(prefix + "LongSingleUseLatency1Thread2: " + (end - start));
    start = System.currentTimeMillis(); timeLongSingleUseLatency1Thread3(200000); end = System.currentTimeMillis();
    if (!verifyLongSingleUseLatency1Thread3()) { rc++; }
    System.out.println(prefix + "LongSingleUseLatency1Thread3: " + (end - start));
    start = System.currentTimeMillis(); timeLongSingleUseLatency1Thread4(150000); end = System.currentTimeMillis();
    if (!verifyLongSingleUseLatency1Thread4()) { rc++; }
    System.out.println(prefix + "LongSingleUseLatency1Thread4: " + (end - start));
    start = System.currentTimeMillis(); timeLongSingleUseLatency2(400000); end = System.currentTimeMillis();
    if (!verifyLongSingleUseLatency2()) { rc++; }
    System.out.println(prefix + "LongSingleUseLatency2: " + (end - start));
    start = System.currentTimeMillis(); timeLongSingleUseLatency3(500000); end = System.currentTimeMillis();
    if (!verifyLongSingleUseLatency3()) { rc++; }
    System.out.println(prefix + "LongSingleUseLatency3: " + (end - start));
    start = System.currentTimeMillis(); timeLongSingleUseLatencyLoop(700000); end = System.currentTimeMillis();
    if (!verifyLongSingleUseLatencyLoop()) { rc++; }
    System.out.println(prefix + "LongSingleUseLatencyLoop: " + (end - start));


    start = System.currentTimeMillis(); timeMultipleUsesDifferentPathInt(500000); end = System.currentTimeMillis();
    if (!verifyMultipleUsesDifferentPathInt()) { rc++; }
    System.out.println(prefix + "MultipleUsesDifferentPathInt: " + (end - start));
    start = System.currentTimeMillis(); timeMultipleSuccessiveUsesSamePathInt(350000); end = System.currentTimeMillis();
    if (!verifyMultipleSuccessiveUsesSamePathInt()) { rc++; }
    System.out.println(prefix + "MultipleSuccessiveUsesSamePathInt: " + (end - start));
    start = System.currentTimeMillis(); timeMultipleSeparatedUsesSamePathInt(400000); end = System.currentTimeMillis();
    if (!verifyMultipleSeparatedUsesSamePathInt()) { rc++; }
    System.out.println(prefix + "MultipleSeparatedUsesSamePathInt: " + (end - start));

    // The `long` versions use the same iteration counts as the `int` versions
    // above.
    start = System.currentTimeMillis(); timeMultipleUsesDifferentPathLong(500000); end = System.currentTimeMillis();
    if (!verifyMultipleUsesDifferentPathLong()) { rc++; }
    System.out.println(prefix + "MultipleUsesDifferentPathLong: " + (end - start));
    start = System.currentTimeMillis(); timeMultipleSuccessiveUsesSamePathLong(350000); end = System.currentTimeMillis();
    if (!verifyMultipleSuccessiveUsesSamePathLong()) { rc++; }
    System.out.println(prefix + "MultipleSuccessiveUsesSamePathLong: " + (end - start));
    start = System.currentTimeMillis(); timeMultipleSeparatedUsesSamePathLong(400000); end = System.currentTimeMillis();
    if (!verifyMultipleSeparatedUsesSamePathLong()) { rc++; }
    System.out.println(prefix + "MultipleSeparatedUsesSamePathLong: " + (end - start));
    // CHECKSTYLE.ON: LineLength
    // CHECKSTYLE.ON: OneStatementPerLine
    // CHECKSTYLE.ON: LeftCurly

    System.exit(rc);
  }
}
