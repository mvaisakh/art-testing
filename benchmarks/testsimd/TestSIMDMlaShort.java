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

import java.util.Arrays;

// CHECKSTYLE.OFF: .*
public class TestSIMDMlaShort {
  static final int LENGTH = 8 * 1024;
  private short[] a;
  private short[] b;
  private short[] c;
  private short[] output;

  public void setupArrays() {
    a = new short[LENGTH];
    b = new short[LENGTH];
    c = new short[LENGTH];
    output = new short[LENGTH];
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
          short[] output) {
    int total = 0;
    for (int i = 0; i < LENGTH; i++) {
      output[i] = (short)(a[i] * b[i] + c[i]);
      total += output[i];
    }
    return total;
  }

  public static int vectSumOfMulAdd2(
          short[] a,
          short[] b,
          short[] c,
          short[] output) {
    int total = 0;
    for (int i = 0; i < LENGTH; i++) {
      output[i] = (short)(a[i] * b[i] + a[i] * c[i]);
      total += output[i];
    }
    return total;
  }

  public static int vectSumOfMulAdd3(
          short[] a,
          short[] b,
          short[] c,
          short[] output) {
    int total = 0;
    for (int i = 0; i < LENGTH; i++) {
      output[i] = (short)((a[i] * b[i]) + (a[i] * c[i]) + (b[i] * c[i]));
      total += output[i];
    }
    return total;
  }

  public static void vectMulAdd1(
          short[] a,
          short[] b,
          short[] c,
          short[] output) {
    for (int i = 0; i < LENGTH; i++) {
      output[i] = (short)(a[i] * b[i] + c[i]);
    }
  }

  public static void vectMulAdd2(
          short[] a,
          short[] b,
          short[] c,
          short[] output) {
    for (int i = 0; i < LENGTH; i++) {
      output[i] = (short)(a[i] * b[i] + a[i] * c[i]);
    }
  }

  public static void vectMulAdd3(
          short[] a,
          short[] b,
          short[] c,
          short[] output) {
    for (int i = 0; i < LENGTH; i++) {
      output[i] = (short)((a[i] * b[i]) + (a[i] * c[i]) + (b[i] * c[i]));
    }
  }

  public void timeVectSumOfMulAdd1(int iters) {
    int sum = 0;
    for (int i = 0; i < iters; i++) {
      sum = vectSumOfMulAdd1(a, b, c, output);
    }
  }

  public boolean verifyVectSumOfMulAdd1() {
    Arrays.fill(output, (short)0);
    timeVectSumOfMulAdd1(1);
    final int hashCode = Arrays.hashCode(output);
    final int expectedHashCode = 1435107329;
    return hashCode == expectedHashCode;
  }

  public void timeVectSumOfMulAdd2(int iters) {
    int sum = 0;
    for (int i = 0; i < iters; i++) {
      sum = vectSumOfMulAdd2(a, b, c, output);
    }
  }

  public boolean verifyVectSumOfMulAdd2() {
    Arrays.fill(output, (short)0);
    timeVectSumOfMulAdd2(1);
    final int hashCode = Arrays.hashCode(output);
    final int expectedHashCode = 235798529;
    return hashCode == expectedHashCode;
  }

  public void timeVectSumOfMulAdd3(int iters) {
    int sum = 0;
    for (int i = 0; i < iters; i++) {
      sum = vectSumOfMulAdd3(a, b, c, output);
    }
  }

  public boolean verifyVectSumOfMulAdd3() {
    Arrays.fill(output, (short)0);
    timeVectSumOfMulAdd3(1);
    final int hashCode = Arrays.hashCode(output);
    final int expectedHashCode = -963510271;
    return hashCode == expectedHashCode;
  }

  public void timeVectMulAdd1(int iters) {
    for (int i = 0; i < iters; i++) {
      vectMulAdd1(a, b, c, output);
    }
  }

  public boolean verifyVectMulAdd1() {
    Arrays.fill(output, (short)0);
    timeVectMulAdd1(1);
    final int hashCode = Arrays.hashCode(output);
    final int expectedHashCode = 1435107329;
    return hashCode == expectedHashCode;
  }

  public void timeVectMulAdd2(int iters) {
    for (int i = 0; i < iters; i++) {
      vectMulAdd2(a, b, c, output);
    }
  }

  public boolean verifyVectMulAdd2() {
    Arrays.fill(output, (short)0);
    timeVectMulAdd2(1);
    final int hashCode = Arrays.hashCode(output);
    final int expectedHashCode = 235798529;
    return hashCode == expectedHashCode;
  }

  public void timeVectMulAdd3(int iters) {
    for (int i = 0; i < iters; i++) {
      vectMulAdd3(a, b, c, output);
    }
  }

  public boolean verifyVectMulAdd3() {
    Arrays.fill(output, (short)0);
    timeVectMulAdd3(1);
    final int hashCode = Arrays.hashCode(output);
    final int expectedHashCode = -963510271;
    return hashCode == expectedHashCode;
  }

  public int verifySIMDMlaShort() {
    int rc = 0;

    if (!verifyVectSumOfMulAdd1()) {
      ++rc;
    }

    if (!verifyVectSumOfMulAdd2()) {
      ++rc;
    }

    if (!verifyVectSumOfMulAdd3()) {
      ++rc;
    }

    if (!verifyVectMulAdd1()) {
      ++rc;
    }

    if (!verifyVectMulAdd2()) {
      ++rc;
    }

    if (!verifyVectMulAdd2()) {
      ++rc;
    }

    return rc;
  }
  // CHECKSTYLE.ON: .*

  public static final int ITER_COUNT = 200;

  public static void main(String[] argv) {
    TestSIMDMlaShort obj = new TestSIMDMlaShort();
    obj.setupArrays();

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

    int rc = obj.verifySIMDMlaShort();
    if (rc != 0) {
      System.out.println("ERROR: verifySIMDMlaShort failed.");
      System.exit(rc);
    }
  }
}
