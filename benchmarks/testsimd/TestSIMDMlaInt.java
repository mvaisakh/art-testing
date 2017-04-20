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
package org.linaro.benchmarks;

import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)

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

  @Setup
  public void setup()
  {
    TestSIMDMlaInit();
  }

  @Benchmark
  public void testVectSumOfMulAdd1() {
    int sum = 0;
    sum = vectSumOfMulAdd1(a, b, c, d);
  }

  @Benchmark
  public void testVectSumOfMulAdd2() {
    int sum = 0;
    sum = vectSumOfMulAdd2(a, b, c, d);
  }

  @Benchmark
  public void testVectSumOfMulAdd3() {
    int sum = 0;
    sum = vectSumOfMulAdd3(a, b, c, d);
  }

}
