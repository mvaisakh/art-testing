/*
 * Copyright (C) 2017 Linaro Limited. All rights reserved.
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

/*
 * Description:     Tracks performance of Integer and Long OneBit intrinsics.
 */

package benchmarks.micro.intrinsics;

public class OneBitIntrinsics {

  // Number of test cases.
  private static final int kIntegerNumTests = 36;
  private static final int kLongNumTests = 68;

  // Test cases for Integer and Long OneBit methods.
  private static int[] integerTestCases = new int[kIntegerNumTests];
  private static long[] longTestCases = new long[kLongNumTests];

  // Benchmark expected results.
  private static int[] integerHighestBitExpected = new int[kIntegerNumTests];
  private static int[] integerLowestBitExpected = new int[kIntegerNumTests];
  private static long[] longHighestBitExpected = new long[kLongNumTests];
  private static long[] longLowestBitExpected = new long[kLongNumTests];

  // Benchmark actual results.
  private static int[] integerHighestBitResults = new int[kIntegerNumTests];
  private static int[] integerLowestBitResults = new int[kIntegerNumTests];
  private static long[] longHighestBitResults = new long[kLongNumTests];
  private static long[] longLowestBitResults = new long[kLongNumTests];

  // Initialize array with test cases for Integer OneBit intrinsics.
  public static void genIntTestCasesArray() {
    integerTestCases[0] = 0x00000000;

    for (int i = 1; i < kIntegerNumTests - 4; i++) {
      integerTestCases[i] = 1 << (i - 1);
    }
    integerTestCases[kIntegerNumTests - 4] = 0xF0000000;
    integerTestCases[kIntegerNumTests - 3] = 0x00011000;
    integerTestCases[kIntegerNumTests - 2] = 0x00FF0000;
    integerTestCases[kIntegerNumTests - 1] = 0xFFFFFFFF;
  }

  // Initialize array with test cases for Long OneBit intrinsics.
  public static void genLongTestCasesArray() {
    longTestCases[0] = 0x0000000000000000L;

    for (int i = 1; i < kLongNumTests - 4; i++) {
      longTestCases[i] = 1L << (i - 1);
    }
    longTestCases[kLongNumTests - 4] = 0xF000000000000000L;
    longTestCases[kLongNumTests - 3] = 0x0000000110000000L;
    longTestCases[kLongNumTests - 2] = 0x0000FFFF00000000L;
    longTestCases[kLongNumTests - 1] = 0xFFFFFFFFFFFFFFFFL;
  }

  // Initialize arrays with expected results for each Integer test case.
  public static void genIntExpectedResultsArray() {
    for (int i = 0; i < kIntegerNumTests - 4; i++) {
      integerHighestBitExpected[i] = integerTestCases[i];
      integerLowestBitExpected[i] = integerTestCases[i];
    }
    integerHighestBitExpected[kIntegerNumTests - 4] = 0x80000000;
    integerLowestBitExpected[kIntegerNumTests - 4] = 0x10000000;
    integerHighestBitExpected[kIntegerNumTests - 3] = 0x00010000;
    integerLowestBitExpected[kIntegerNumTests - 3] = 0x00001000;
    integerHighestBitExpected[kIntegerNumTests - 2] = 0x00800000;
    integerLowestBitExpected[kIntegerNumTests - 2] = 0x00010000;
    integerHighestBitExpected[kIntegerNumTests - 1] = 0x80000000;
    integerLowestBitExpected[kIntegerNumTests - 1] = 0x00000001;
  }

  // Initialize arrays with expected results for each Long test case.
  public static void genLongExpectedResultsArray() {
    for (int i = 0; i < kLongNumTests - 4; i++) {
      longHighestBitExpected[i] = longTestCases[i];
      longLowestBitExpected[i] = longTestCases[i];
    }
    longHighestBitExpected[kLongNumTests - 4] = 0x8000000000000000L;
    longLowestBitExpected[kLongNumTests - 4] = 0x1000000000000000L;
    longHighestBitExpected[kLongNumTests - 3] = 0x0000000100000000L;
    longLowestBitExpected[kLongNumTests - 3] = 0x0000000010000000L;
    longHighestBitExpected[kLongNumTests - 2] = 0x0000800000000000L;
    longLowestBitExpected[kLongNumTests - 2] = 0x0000000100000000L;
    longHighestBitExpected[kLongNumTests - 1] = 0x8000000000000000L;
    longLowestBitExpected[kLongNumTests - 1] = 0x0000000000000001L;
  }

  public void timeIntegerHighestOneBit(int iterations) {
    genIntTestCasesArray();
    for (int i = 0; i < iterations; i++) {
      for (int j = 0; j < kIntegerNumTests; j++) {
        integerHighestBitResults[j] = Integer.highestOneBit(integerTestCases[j]);
      }
    }
  }

  public void timeIntegerLowestOneBit(int iterations) {
    genIntTestCasesArray();
    for (int i = 0; i < iterations; i++) {
      for (int j = 0; j < kIntegerNumTests; j++) {
        integerLowestBitResults[j] = Integer.lowestOneBit(integerTestCases[j]);
      }
    }
  }

  public void timeLongHighestOneBit(int iterations) {
    genLongTestCasesArray();
    for (int i = 0; i < iterations; i++) {
      for (int j = 0; j < kLongNumTests; j++) {
        longHighestBitResults[j] = Long.highestOneBit(longTestCases[j]);
      }
    }
  }

  public void timeLongLowestOneBit(int iterations) {
    genLongTestCasesArray();
    for (int i = 0; i < iterations; i++) {
      for (int j = 0; j < kLongNumTests; j++) {
        longLowestBitResults[j] = Long.lowestOneBit(longTestCases[j]);
      }
    }
  }

  public boolean verifyOneBitIntrinsics() {

    genIntTestCasesArray();
    genIntExpectedResultsArray();
    genLongTestCasesArray();
    genLongExpectedResultsArray();

    timeIntegerHighestOneBit(1);
    timeIntegerLowestOneBit(1);
    timeLongHighestOneBit(1);
    timeLongLowestOneBit(1);

    for (int i = 0; i < kIntegerNumTests; i++) {
      if (integerHighestBitExpected[i] != integerHighestBitResults[i]) {
        System.out.println("ERROR: Expected " + integerHighestBitExpected[i] +
                           " but found " + integerHighestBitResults[i] +
                           " on input " + integerTestCases[i] + ", on test " + i +
                           " ---> highestOneBit, Integer.");
        return false;
      }
      if (integerLowestBitExpected[i] != integerLowestBitResults[i]) {
        System.out.println("ERROR: Expected " + integerLowestBitExpected[i] +
                           " but found " + integerLowestBitResults[i] +
                           " on input " + integerTestCases[i] +  ", on test " + i +
                           " ---> lowestOneBit, Integer.");
        return false;
      }
    }

    for (int i = 0; i < kLongNumTests; i++) {
      if (longHighestBitExpected[i] != longHighestBitResults[i]) {
        System.out.println("ERROR: Expected " + longHighestBitExpected[i] +
                           " but found " + longHighestBitResults[i] +
                           " on input " + longTestCases[i] +  ", on test " + i +
                           " ---> highestOneBit, Long.");
        return false;
      }
      if (longLowestBitExpected[i] != longLowestBitResults[i]) {
        System.out.println("ERROR: Expected " + longLowestBitExpected[i] +
                           " but found " + longLowestBitResults[i] +
                           " on input " + longTestCases[i] +  ", on test " + i +
                           " ---> lowestOneBit, Long.");
        return false;
      }
    }
    return true;
  }

  private static final int ITER_COUNT = 900000;

  public static void main(String[] args) {
    OneBitIntrinsics obj = new OneBitIntrinsics();
    int rc = 0;

    long before = System.currentTimeMillis();
    obj.timeIntegerHighestOneBit(ITER_COUNT);
    long after = System.currentTimeMillis();
    System.out.println("benchmarks/micro/OneBitIntrinsics/IntegerHighestOneBit: " +
                       (after - before));

    before = System.currentTimeMillis();
    obj.timeIntegerLowestOneBit(ITER_COUNT);
    after = System.currentTimeMillis();
    System.out.println("benchmarks/micro/OneBitIntrinsics/IntegerLowestOneBit: " +
                       (after - before));

    before = System.currentTimeMillis();
    obj.timeLongHighestOneBit(ITER_COUNT);
    after = System.currentTimeMillis();
    System.out.println("benchmarks/micro/OneBitIntrinsics/LongHighestOneBit: " +
                       (after - before));

    before = System.currentTimeMillis();
    obj.timeLongLowestOneBit(ITER_COUNT);
    after = System.currentTimeMillis();
    System.out.println("benchmarks/micro/OneBitIntrinsics/LongLowestOneBit: " +
                       (after - before));

    if (!obj.verifyOneBitIntrinsics()) {
      rc++;
    }

    System.exit(rc);
  }

}
