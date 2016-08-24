/*
 * Copyright (C) 2016 Linaro Limited. All rights reserved.
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
 * Description:     Tracks performance of String intrinsics, Java, and native methods.
 * Main Focus:      Looped memory compare.
 * Secondary Focus: Array access.
 *
 */

package benchmarks.micro;

import java.lang.StringBuilder;
import java.lang.System;
import java.util.Random;

public class StringOps {

  private static int RANDOM_STRING_8 = 0;
  private static int RANDOM_STRING_16 = 1;
  private static int RANDOM_STRING_32 = 2;
  private static int RANDOM_STRING_128 = 3;
  private static int RANDOM_STRING_512 = 4;
  private static int NUM_LENGTH_TESTS = 5;

  /* Intentionally use the same seed each time for consistency across benchmark runs. */
  private static int SAME_SEED = 0;

  /* Random string data. */
  private static Random rnd = new Random(SAME_SEED);
  private static String[] stringData = new String[NUM_LENGTH_TESTS];

  /* Same random string data as above for comparing different instances of the same char data. */
  private static Random rndAlt = new Random(SAME_SEED);
  private static String[] stringDataAlt = new String[NUM_LENGTH_TESTS];

  /* Benchmark results cache for preventing DCE. */
  private static boolean[] stringEqualsResults = new boolean[NUM_LENGTH_TESTS];
  private static boolean[] stringEqualsIgnoreCaseResults = new boolean[NUM_LENGTH_TESTS];
  private static boolean[] stringContentEqualsResults = new boolean[NUM_LENGTH_TESTS];
  private static int[] stringCompareToResults = new int[NUM_LENGTH_TESTS];
  private static int[] stringCompareToIgnoreCaseResults = new int[NUM_LENGTH_TESTS];
  private static boolean[] stringRegionMatchesResults = new boolean[NUM_LENGTH_TESTS];
  private static boolean[] stringRegionMatchesIgnoreCaseResults = new boolean[NUM_LENGTH_TESTS];
  private static char stringCharAtResult;
  private static int stringIndexOfResult;
  private static int stringIndexOfAfterResult;
  private static String[] stringNewStringFromBytesResult = new String[NUM_LENGTH_TESTS];
  private static String[] stringNewStringFromCharsResult = new String[NUM_LENGTH_TESTS];
  private static String[] stringNewStringFromStringResult = new String[NUM_LENGTH_TESTS];
  private static String[] stringGetCharsNoCheckResult = new String[NUM_LENGTH_TESTS];

  private static String generateRandomString(int len, Random rnd) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < len; i++) {
      /* Compose random string data from upper and lower case english alphabet entries plus a few
       * harmless characters in-between. */
      sb.append(Character.valueOf((char)(65 + rnd.nextInt(58))));
    }
    return sb.toString();
  }

  private static void generateRandomStrings(Random rnd, String[] output) {
    output[RANDOM_STRING_8] = generateRandomString(8, rnd);
    output[RANDOM_STRING_16] = generateRandomString(16, rnd);
    output[RANDOM_STRING_32] = generateRandomString(32, rnd);
    output[RANDOM_STRING_128] = generateRandomString(128, rnd);
    output[RANDOM_STRING_512] = generateRandomString(512, rnd);
  }

  static {
    generateRandomStrings(rnd, stringData);
    generateRandomStrings(rndAlt, stringDataAlt);
  }

  /**
   * String.equals
   */

  public void timeStringEquals008(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringEqualsResults[RANDOM_STRING_8]
        ^= stringData[RANDOM_STRING_8].equals(stringDataAlt[RANDOM_STRING_8]);
    }
  }

  public void timeStringEquals016(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringEqualsResults[RANDOM_STRING_16]
        ^= stringData[RANDOM_STRING_16].equals(stringDataAlt[RANDOM_STRING_16]);
    }
  }

  public void timeStringEquals032(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringEqualsResults[RANDOM_STRING_32]
        ^= stringData[RANDOM_STRING_32].equals(stringDataAlt[RANDOM_STRING_32]);
    }
  }

  public void timeStringEquals128(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringEqualsResults[RANDOM_STRING_128]
        ^= stringData[RANDOM_STRING_128].equals(stringDataAlt[RANDOM_STRING_128]);
    }
  }

  public void timeStringEquals512(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringEqualsResults[RANDOM_STRING_512]
        ^= stringData[RANDOM_STRING_512].equals(stringDataAlt[RANDOM_STRING_512]);
    }
  }

  /**
   * String.equalsIgnoreCase
   */

  public void timeStringEqualsIgnoreCase008(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringEqualsIgnoreCaseResults[RANDOM_STRING_8]
        ^= stringData[RANDOM_STRING_8].equalsIgnoreCase(
                stringDataAlt[RANDOM_STRING_8]);
    }
  }

  public void timeStringEqualsIgnoreCase016(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringEqualsIgnoreCaseResults[RANDOM_STRING_16]
        ^= stringData[RANDOM_STRING_16].equalsIgnoreCase(
                stringDataAlt[RANDOM_STRING_16]);
    }
  }

  public void timeStringEqualsIgnoreCase032(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringEqualsIgnoreCaseResults[RANDOM_STRING_32]
        ^= stringData[RANDOM_STRING_32].equalsIgnoreCase(
                stringDataAlt[RANDOM_STRING_32]);
    }
  }

  public void timeStringEqualsIgnoreCase128(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringEqualsIgnoreCaseResults[RANDOM_STRING_128]
        ^= stringData[RANDOM_STRING_128].equalsIgnoreCase(
                stringDataAlt[RANDOM_STRING_128]);
    }
  }

  public void timeStringEqualsIgnoreCase512(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringEqualsIgnoreCaseResults[RANDOM_STRING_512]
        ^= stringData[RANDOM_STRING_512].equalsIgnoreCase(
                stringDataAlt[RANDOM_STRING_512]);
    }
  }

  /**
   * String.contentEquals
   */

  public void timeStringContentEquals008(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringContentEqualsResults[RANDOM_STRING_8]
        ^= stringData[RANDOM_STRING_8].contentEquals(
                stringDataAlt[RANDOM_STRING_8]);
    }
  }

  public void timeStringContentEquals016(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringContentEqualsResults[RANDOM_STRING_16]
        ^= stringData[RANDOM_STRING_16].contentEquals(
                stringDataAlt[RANDOM_STRING_16]);
    }
  }

  public void timeStringContentEquals032(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringContentEqualsResults[RANDOM_STRING_32]
        ^= stringData[RANDOM_STRING_32].contentEquals(
                stringDataAlt[RANDOM_STRING_32]);
    }
  }

  public void timeStringContentEquals128(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringContentEqualsResults[RANDOM_STRING_128]
        ^= stringData[RANDOM_STRING_128].contentEquals(
                stringDataAlt[RANDOM_STRING_128]);
    }
  }

  public void timeStringContentEquals512(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringContentEqualsResults[RANDOM_STRING_512]
        ^= stringData[RANDOM_STRING_512].contentEquals(
                stringDataAlt[RANDOM_STRING_512]);
    }
  }

  /**
   * String.compareTo
   */

  public void timeStringCompareTo008(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringCompareToResults[RANDOM_STRING_8]
        += stringData[RANDOM_STRING_8].compareTo(stringDataAlt[RANDOM_STRING_8]);
    }
  }

  public void timeStringCompareTo016(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringCompareToResults[RANDOM_STRING_16]
        += stringData[RANDOM_STRING_16].compareTo(stringDataAlt[RANDOM_STRING_16]);
    }
  }

  public void timeStringCompareTo032(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringCompareToResults[RANDOM_STRING_32]
        += stringData[RANDOM_STRING_32].compareTo(stringDataAlt[RANDOM_STRING_32]);
    }
  }

  public void timeStringCompareTo128(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringCompareToResults[RANDOM_STRING_128]
        += stringData[RANDOM_STRING_128].compareTo(stringDataAlt[RANDOM_STRING_128]);
    }
  }

  public void timeStringCompareTo512(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringCompareToResults[RANDOM_STRING_512]
        += stringData[RANDOM_STRING_512].compareTo(stringDataAlt[RANDOM_STRING_512]);
    }
  }

  /**
   * String.compareToIgnoreCase
   */

  public void timeStringCompareToIgnoreCase008(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringCompareToIgnoreCaseResults[RANDOM_STRING_8]
        += stringData[RANDOM_STRING_8].compareToIgnoreCase(
                stringDataAlt[RANDOM_STRING_8]);
    }
  }

  public void timeStringCompareToIgnoreCase016(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringCompareToIgnoreCaseResults[RANDOM_STRING_16]
        += stringData[RANDOM_STRING_16].compareToIgnoreCase(
                stringDataAlt[RANDOM_STRING_16]);
    }
  }

  public void timeStringCompareToIgnoreCase032(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringCompareToIgnoreCaseResults[RANDOM_STRING_32]
        += stringData[RANDOM_STRING_32].compareToIgnoreCase(
                stringDataAlt[RANDOM_STRING_32]);
    }
  }

  public void timeStringCompareToIgnoreCase128(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringCompareToIgnoreCaseResults[RANDOM_STRING_128]
        += stringData[RANDOM_STRING_128].compareToIgnoreCase(
                stringDataAlt[RANDOM_STRING_128]);
    }
  }

  public void timeStringCompareToIgnoreCase512(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringCompareToIgnoreCaseResults[RANDOM_STRING_512]
        += stringData[RANDOM_STRING_512].compareToIgnoreCase(
                stringDataAlt[RANDOM_STRING_512]);
    }
  }

  /**
   * String.regionMatches
   */

  public void timeStringRegionMatches008(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringRegionMatchesResults[RANDOM_STRING_8]
        ^= stringData[RANDOM_STRING_8].regionMatches(
                0, stringDataAlt[RANDOM_STRING_8], 0, 8);
    }
  }

  public void timeStringRegionMatches016(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringRegionMatchesResults[RANDOM_STRING_16]
        ^= stringData[RANDOM_STRING_16].regionMatches(
                0, stringDataAlt[RANDOM_STRING_16], 0, 16);
    }
  }

  public void timeStringRegionMatches032(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringRegionMatchesResults[RANDOM_STRING_32]
        ^= stringData[RANDOM_STRING_32].regionMatches(
                0, stringDataAlt[RANDOM_STRING_32], 0, 32);
    }
  }

  public void timeStringRegionMatches128(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringRegionMatchesResults[RANDOM_STRING_128]
        ^= stringData[RANDOM_STRING_128].regionMatches(
                0, stringDataAlt[RANDOM_STRING_128], 0, 128);
    }
  }

  public void timeStringRegionMatches512(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringRegionMatchesResults[RANDOM_STRING_512]
        ^= stringData[RANDOM_STRING_512].regionMatches(
                0, stringDataAlt[RANDOM_STRING_512], 0, 512);
    }
  }

  /**
   * String.regionMatches
   */

  public void timeStringRegionMatchesIgnoreCase008(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringRegionMatchesIgnoreCaseResults[RANDOM_STRING_8]
        ^= stringData[RANDOM_STRING_8].regionMatches(
                true, 0, stringDataAlt[RANDOM_STRING_8], 0, 8);
    }
  }

  public void timeStringRegionMatchesIgnoreCase016(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringRegionMatchesIgnoreCaseResults[RANDOM_STRING_16]
        ^= stringData[RANDOM_STRING_16].regionMatches(
                true, 0, stringDataAlt[RANDOM_STRING_16], 0, 16);
    }
  }

  public void timeStringRegionMatchesIgnoreCase032(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringRegionMatchesIgnoreCaseResults[RANDOM_STRING_32]
        ^= stringData[RANDOM_STRING_32].regionMatches(
                true, 0, stringDataAlt[RANDOM_STRING_32], 0, 32);
    }
  }

  public void timeStringRegionMatchesIgnoreCase128(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringRegionMatchesIgnoreCaseResults[RANDOM_STRING_128]
        ^= stringData[RANDOM_STRING_128].regionMatches(
                true, 0, stringDataAlt[RANDOM_STRING_128], 0, 128);
    }
  }

  public void timeStringRegionMatchesIgnoreCase512(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringRegionMatchesIgnoreCaseResults[RANDOM_STRING_512]
        ^= stringData[RANDOM_STRING_512].regionMatches(
                true, 0, stringDataAlt[RANDOM_STRING_512], 0, 512);
    }
  }

  /**
   * String.charAt
   */

  public void timeStringCharAt(int iterations) {
    for (int i = 0; i < iterations; i++) {
      for (int j = 0; j < 512; j++) {
        stringCharAtResult = stringData[RANDOM_STRING_512].charAt(j);
      }
    }
  }

  /**
   * String.indexOf
   */

  public void timeStringIndexOf(int iterations) {
    char c = stringData[RANDOM_STRING_512].charAt(511);
    for (int i = 0; i < iterations; i++) {
      stringIndexOfResult += stringData[RANDOM_STRING_512].indexOf(c);
    }
  }

  /**
   * String.indexOfAfter
   */

  public void timeStringIndexOfAfter(int iterations) {
    char c = stringData[RANDOM_STRING_512].charAt(511);
    for (int i = 0; i < iterations; i++) {
      stringIndexOfAfterResult += stringData[RANDOM_STRING_512].indexOf(c, 1);
    }
  }

  /**
   * NewStringFromBytes
   */

  public void timeStringNewStringFromBytes008(int iterations) {
    byte[] bytes = stringData[RANDOM_STRING_8].getBytes();
    for (int i = 0; i < iterations; i++) {
      stringNewStringFromBytesResult[RANDOM_STRING_8] = new String(bytes);
    }
  }

  public void timeStringNewStringFromBytes016(int iterations) {
    byte[] bytes = stringData[RANDOM_STRING_16].getBytes();
    for (int i = 0; i < iterations; i++) {
      stringNewStringFromBytesResult[RANDOM_STRING_16] = new String(bytes);
    }
  }

  public void timeStringNewStringFromBytes032(int iterations) {
    byte[] bytes = stringData[RANDOM_STRING_32].getBytes();
    for (int i = 0; i < iterations; i++) {
      stringNewStringFromBytesResult[RANDOM_STRING_32] = new String(bytes);
    }
  }

  public void timeStringNewStringFromBytes128(int iterations) {
    byte[] bytes = stringData[RANDOM_STRING_128].getBytes();
    for (int i = 0; i < iterations; i++) {
      stringNewStringFromBytesResult[RANDOM_STRING_128] = new String(bytes);
    }
  }

  public void timeStringNewStringFromBytes512(int iterations) {
    byte[] bytes = stringData[RANDOM_STRING_512].getBytes();
    for (int i = 0; i < iterations; i++) {
      stringNewStringFromBytesResult[RANDOM_STRING_512] = new String(bytes);
    }
  }

  /**
   * NewStringFromChars
   */

  public void timeStringNewStringFromChars008(int iterations) {
    char[] chars = new char[8];
    stringData[RANDOM_STRING_8].getChars(0, 8, chars, 0);
    for (int i = 0; i < iterations; i++) {
      stringNewStringFromCharsResult[RANDOM_STRING_8] = new String(chars);
    }
  }

  public void timeStringNewStringFromChars016(int iterations) {
    char[] chars = new char[16];
    stringData[RANDOM_STRING_16].getChars(0, 16, chars, 0);
    for (int i = 0; i < iterations; i++) {
      stringNewStringFromCharsResult[RANDOM_STRING_16] = new String(chars);
    }
  }

  public void timeStringNewStringFromChars032(int iterations) {
    char[] chars = new char[32];
    stringData[RANDOM_STRING_32].getChars(0, 32, chars, 0);
    for (int i = 0; i < iterations; i++) {
      stringNewStringFromCharsResult[RANDOM_STRING_32] = new String(chars);
    }
  }

  public void timeStringNewStringFromChars128(int iterations) {
    char[] chars = new char[128];
    stringData[RANDOM_STRING_128].getChars(0, 128, chars, 0);
    for (int i = 0; i < iterations; i++) {
      stringNewStringFromCharsResult[RANDOM_STRING_128] = new String(chars);
    }
  }

  public void timeStringNewStringFromChars512(int iterations) {
    char[] chars = new char[512];
    stringData[RANDOM_STRING_512].getChars(0, 512, chars, 0);
    for (int i = 0; i < iterations; i++) {
      stringNewStringFromCharsResult[RANDOM_STRING_512] = new String(chars);
    }
  }

  /**
   * NewStringFromString
   */

  public void timeStringNewStringFromString008(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringNewStringFromStringResult[RANDOM_STRING_8] =
          new String(stringData[RANDOM_STRING_8]);
    }
  }

  public void timeStringNewStringFromString016(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringNewStringFromStringResult[RANDOM_STRING_16] =
          new String(stringData[RANDOM_STRING_16]);
    }
  }

  public void timeStringNewStringFromString032(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringNewStringFromStringResult[RANDOM_STRING_32] =
          new String(stringData[RANDOM_STRING_32]);
    }
  }

  public void timeStringNewStringFromString128(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringNewStringFromStringResult[RANDOM_STRING_128] =
          new String(stringData[RANDOM_STRING_128]);
    }
  }

  public void timeStringNewStringFromString512(int iterations) {
    for (int i = 0; i < iterations; i++) {
      stringNewStringFromStringResult[RANDOM_STRING_512] =
          new String(stringData[RANDOM_STRING_512]);
    }
  }

  /**
   * String.getCharsNoCheck
   */

  public void timeStringGetCharsNoCheck008(int iterations) {
    char[] chars = new char[8];
    for (int i = 0; i < iterations; i++) {
      stringData[RANDOM_STRING_8].getChars(0, 8, chars, 0);
    }
    stringGetCharsNoCheckResult[RANDOM_STRING_8] = new String(chars);
  }

  public void timeStringGetCharsNoCheck016(int iterations) {
    char[] chars = new char[16];
    for (int i = 0; i < iterations; i++) {
      stringData[RANDOM_STRING_16].getChars(0, 16, chars, 0);
    }
    stringGetCharsNoCheckResult[RANDOM_STRING_16] = new String(chars);
  }

  public void timeStringGetCharsNoCheck032(int iterations) {
    char[] chars = new char[32];
    for (int i = 0; i < iterations; i++) {
      stringData[RANDOM_STRING_32].getChars(0, 32, chars, 0);
    }
    stringGetCharsNoCheckResult[RANDOM_STRING_32] = new String(chars);
  }

  public void timeStringGetCharsNoCheck128(int iterations) {
    char[] chars = new char[128];
    for (int i = 0; i < iterations; i++) {
      stringData[RANDOM_STRING_128].getChars(0, 128, chars, 0);
    }
    stringGetCharsNoCheckResult[RANDOM_STRING_128] = new String(chars);
  }

  public void timeStringGetCharsNoCheck512(int iterations) {
    char[] chars = new char[512];
    for (int i = 0; i < iterations; i++) {
      stringData[RANDOM_STRING_512].getChars(0, 512, chars, 0);
    }
    stringGetCharsNoCheckResult[RANDOM_STRING_512] = new String(chars);
  }

  private static final int ITER_COUNT = 22000;

  public static void main(String[] args) {
    int result = 0;
    StringOps obj = new StringOps();
    long before = System.currentTimeMillis();
    obj.timeStringEquals008(ITER_COUNT);
    obj.timeStringEquals016(ITER_COUNT);
    obj.timeStringEquals032(ITER_COUNT);
    obj.timeStringEquals128(ITER_COUNT);
    obj.timeStringEquals512(ITER_COUNT);
    obj.timeStringEqualsIgnoreCase008(ITER_COUNT);
    obj.timeStringEqualsIgnoreCase016(ITER_COUNT);
    obj.timeStringEqualsIgnoreCase032(ITER_COUNT);
    obj.timeStringEqualsIgnoreCase128(ITER_COUNT);
    obj.timeStringEqualsIgnoreCase512(ITER_COUNT);
    obj.timeStringContentEquals008(ITER_COUNT);
    obj.timeStringContentEquals016(ITER_COUNT);
    obj.timeStringContentEquals032(ITER_COUNT);
    obj.timeStringContentEquals128(ITER_COUNT);
    obj.timeStringContentEquals512(ITER_COUNT);
    obj.timeStringCompareTo008(ITER_COUNT);
    obj.timeStringCompareTo016(ITER_COUNT);
    obj.timeStringCompareTo032(ITER_COUNT);
    obj.timeStringCompareTo128(ITER_COUNT);
    obj.timeStringCompareTo512(ITER_COUNT);
    obj.timeStringCompareToIgnoreCase008(ITER_COUNT);
    obj.timeStringCompareToIgnoreCase016(ITER_COUNT);
    obj.timeStringCompareToIgnoreCase032(ITER_COUNT);
    obj.timeStringCompareToIgnoreCase128(ITER_COUNT);
    obj.timeStringCompareToIgnoreCase512(ITER_COUNT);
    obj.timeStringRegionMatches008(ITER_COUNT);
    obj.timeStringRegionMatches016(ITER_COUNT);
    obj.timeStringRegionMatches032(ITER_COUNT);
    obj.timeStringRegionMatches128(ITER_COUNT);
    obj.timeStringRegionMatches512(ITER_COUNT);
    obj.timeStringRegionMatchesIgnoreCase008(ITER_COUNT);
    obj.timeStringRegionMatchesIgnoreCase016(ITER_COUNT);
    obj.timeStringRegionMatchesIgnoreCase032(ITER_COUNT);
    obj.timeStringRegionMatchesIgnoreCase128(ITER_COUNT);
    obj.timeStringRegionMatchesIgnoreCase512(ITER_COUNT);
    obj.timeStringCharAt(ITER_COUNT);
    obj.timeStringIndexOf(ITER_COUNT);
    obj.timeStringIndexOfAfter(ITER_COUNT);
    obj.timeStringNewStringFromBytes008(ITER_COUNT);
    obj.timeStringNewStringFromBytes016(ITER_COUNT);
    obj.timeStringNewStringFromBytes032(ITER_COUNT);
    obj.timeStringNewStringFromBytes128(ITER_COUNT);
    obj.timeStringNewStringFromBytes512(ITER_COUNT);
    obj.timeStringNewStringFromChars008(ITER_COUNT);
    obj.timeStringNewStringFromChars016(ITER_COUNT);
    obj.timeStringNewStringFromChars032(ITER_COUNT);
    obj.timeStringNewStringFromChars128(ITER_COUNT);
    obj.timeStringNewStringFromChars512(ITER_COUNT);
    obj.timeStringNewStringFromString008(ITER_COUNT);
    obj.timeStringNewStringFromString016(ITER_COUNT);
    obj.timeStringNewStringFromString032(ITER_COUNT);
    obj.timeStringNewStringFromString128(ITER_COUNT);
    obj.timeStringNewStringFromString512(ITER_COUNT);
    obj.timeStringGetCharsNoCheck008(ITER_COUNT);
    obj.timeStringGetCharsNoCheck016(ITER_COUNT);
    obj.timeStringGetCharsNoCheck032(ITER_COUNT);
    obj.timeStringGetCharsNoCheck128(ITER_COUNT);
    obj.timeStringGetCharsNoCheck512(ITER_COUNT);

    long after = System.currentTimeMillis();
    System.out.println("benchmarks/micro/StringOps: " + (after - before));
    System.exit(result);
  }
}
