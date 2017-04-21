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
public class TestSIMDMlaShort {
  static final int LENGTH = 256 * 1024;
  static short [] a = new short[LENGTH];
  static short [] b = new short[LENGTH];
  static short [] c = new short[LENGTH];
  static short [] d = new short[LENGTH];

  public static void TestSIMDMlaInit() {
    for (int i = 0; i < LENGTH; i++) {
       a[i] = 3;
       b[i] = 2;
       c[i] = 1;
    }
  }

  public static int vectSumOfMulAdd1(
          short[] a,
          short[] b,
          short[] c,
          short[] d) {
    int total = 0;
    for (int i = 0; i < LENGTH; i++) {
      d[i] = (short)(a[i] * b[i] + c[i]);
      total += d[i];
    }
    return total;
  }

  public static int vectSumOfMulAdd2(
          short[] a,
          short[] b,
          short[] c,
          short[] d) {
    int total = 0;
    for (int i = 0; i < LENGTH; i++) {
      d[i] = (short)(a[i] * b[i] + a[i] * c[i]);
      total += d[i];
    }
    return total;
  }

  public static int vectSumOfMulAdd3(
          short[] a,
          short[] b,
          short[] c,
          short[] d) {
    int total = 0;
    for (int i = 0; i < LENGTH; i++) {
      d[i] = (short)((a[i] * b[i]) + (a[i] * c[i]) + (b[i] * c[i]));
      total += d[i];
    }
    return total;
  }

  public static void vectMulAdd1(
          short[] a,
          short[] b,
          short[] c,
          short[] d) {
    for (int i = 0; i < LENGTH; i++) {
      d[i] = (short)(a[i] * b[i] + c[i]);
    }
  }

  public static void vectMulAdd2(
          short[] a,
          short[] b,
          short[] c,
          short[] d) {
    for (int i = 0; i < LENGTH; i++) {
      d[i] = (short)(a[i] * b[i] + a[i] * c[i]);
    }
  }

  public static void vectMulAdd3(
          short[] a,
          short[] b,
          short[] c,
          short[] d) {
    for (int i = 0; i < LENGTH; i++) {
      d[i] = (short)((a[i] * b[i]) + (a[i] * c[i]) + (b[i] * c[i]));
    }
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

  public void timeVectMulAdd1(int iters) {
    TestSIMDMlaInit();
    for (int i = 0; i < iters; i++) {
      vectMulAdd1(a, b, c, d);
    }
  }

  public void timeVectMulAdd2(int iters) {
    TestSIMDMlaInit();
    for (int i = 0; i < iters; i++) {
      vectMulAdd2(a, b, c, d);
    }
  }

  public void timeVectMulAdd3(int iters) {
    TestSIMDMlaInit();
    for (int i = 0; i < iters; i++) {
      vectMulAdd3(a, b, c, d);
    }
  }

  public boolean verifySIMDMlaShort() {
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
    TestSIMDMlaShort obj = new TestSIMDMlaShort();

    long before = System.currentTimeMillis();
    obj.timeVectSumOfMulAdd1(ITER_COUNT);
    long after = System.currentTimeMillis();
    System.out.println("benchmarks/testsimd/TestSIMDMlaShort1: " + (after - before));

    before = System.currentTimeMillis();
    obj.timeVectSumOfMulAdd2(ITER_COUNT);
    after = System.currentTimeMillis();
    System.out.println("benchmarks/testsimd/TestSIMDMlaShort2: " + (after - before));

    before = System.currentTimeMillis();
    obj.timeVectSumOfMulAdd3(ITER_COUNT);
    after = System.currentTimeMillis();
    System.out.println("benchmarks/testsimd/TestSIMDMlaShort3: " + (after - before));

    before = System.currentTimeMillis();
    obj.timeVectMulAdd1(ITER_COUNT);
    after = System.currentTimeMillis();
    System.out.println("benchmarks/testsimd/TestSIMDMlaShortSimple1: " + (after - before));

    before = System.currentTimeMillis();
    obj.timeVectMulAdd2(ITER_COUNT);
    after = System.currentTimeMillis();
    System.out.println("benchmarks/testsimd/TestSIMDMlaShortSimple2: " + (after - before));

    before = System.currentTimeMillis();
    obj.timeVectMulAdd3(ITER_COUNT);
    after = System.currentTimeMillis();
    System.out.println("benchmarks/testsimd/TestSIMDMlaShortSimple3: " + (after - before));

    if (!obj.verifySIMDMlaShort()) {
      rc++;
    }
    System.exit(rc);
  }

}
