/*
 * Copyright (C) 2016 Linaro Limited. All rights received.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package benchmarks.testsimd;

// CHECKSTYLE.OFF: .*
public class TestSIMDMlaInt {
  static final int LENGTH = 256 * 1024;
  static int [] a = new int[LENGTH];
  static int [] b = new int[LENGTH];
  static int [] c = new int[LENGTH];
  static int [] d = new int[LENGTH];

  public static void TestSIMDMlaInit() {
    for (int i = 0; i < LENGTH; i++) {
       a[i] = 3;
       b[i] = 2;
       c[i] = 1;
    }
  }

  public static int vectSumOfMulAdd1(
          int[] a,
          int[] b,
          int[] c,
          int[] d) {
    int total = 0;
    for (int i = 0; i < LENGTH; i++) {
      d[i] = (int)(a[i] * b[i] + c[i]);
      total += d[i];
    }
    return total;
  }

  public static int vectSumOfMulAdd2(
          int[] a,
          int[] b,
          int[] c,
          int[] d) {
    int total = 0;
    for (int i = 0; i < LENGTH; i++) {
      d[i] = (int)(a[i] * b[i] + a[i] * c[i]);
      total += d[i];
    }
    return total;
  }

  public static int vectSumOfMulAdd3(
          int[] a,
          int[] b,
          int[] c,
          int[] d) {
    int total = 0;
    for (int i = 0; i < LENGTH; i++) {
      d[i] = (int)((a[i] * b[i]) + (a[i] * c[i]) + (b[i] * c[i]));
      total += d[i];
    }
    return total;
  }

  public void timeVectSumOfMulAdd1(int iters) {
    int sum = 0;
    TestSIMDMlaInit();
    for (int i = 0; i < iters; i++) {
      sum = vectSumOfMulAdd1(a, b, c, d);
    }
  }

  public void timeVectSumOfMulAdd2(int iters) {
    int sum = 0;
    TestSIMDMlaInit();
    for (int i = 0; i < iters; i++) {
      sum = vectSumOfMulAdd2(a, b, c, d);
    }
  }

  public void timeVectSumOfMulAdd3(int iters) {
    int sum = 0;
    TestSIMDMlaInit();
    for (int i = 0; i < iters; i++) {
      sum = vectSumOfMulAdd3(a, b, c, d);
    }
  }

  public boolean verifySIMDMlaInt() {
    int expected = 7077888;
    int found = 0;
    TestSIMDMlaInit();
    found += vectSumOfMulAdd1(a, b, c, d);
    found += vectSumOfMulAdd2(a, b, c, d);
    found += vectSumOfMulAdd3(a, b, c, d);

    if (found != expected) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }

    return true;
  }
  // CHECKSTYLE.ON: .*

  public static final int ITER_COUNT = 200;

  public static void main(String[] argv) {
    int rc = 0;
    TestSIMDMlaInt obj = new TestSIMDMlaInt();

    long before = System.currentTimeMillis();
    obj.timeVectSumOfMulAdd1(ITER_COUNT);
    long after = System.currentTimeMillis();
    System.out.println("benchmarks/testsimd/TestSIMDMlaInt1: " + (after - before));

    before = System.currentTimeMillis();
    obj.timeVectSumOfMulAdd2(ITER_COUNT);
    after = System.currentTimeMillis();
    System.out.println("benchmarks/testsimd/TestSIMDMlaInt2: " + (after - before));

    before = System.currentTimeMillis();
    obj.timeVectSumOfMulAdd3(ITER_COUNT);
    after = System.currentTimeMillis();
    System.out.println("benchmarks/testsimd/TestSIMDMlaInt3: " + (after - before));

    if (!obj.verifySIMDMlaInt()) {
      rc++;
    }
    System.exit(rc);
  }

}
