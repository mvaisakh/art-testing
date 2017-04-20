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

public class TestReduceAdd {
  static final int LENGTH = 256 * 1024;
  static int [] a = new int[LENGTH];
  static int [] b = new int[LENGTH];
  static short [] sa = new short[LENGTH];
  static short [] sb = new short[LENGTH];

  public static void TestReduceAddInit() {
    for (int i = 0; i < LENGTH; i++) {
       a[i] = 2;
       b[i] = 1;
       sa[i] = 2;
       sb[i] = 1;
    }
  }

  // In this case, addv sn vm.4s can't be generated in current jdk (OpenJDK9).
  // hotspot version: changeset 12033:d5d5cd1adeaa
  // The same with following test cases.
  public static int reduceAddInt(int[] a, int[] b) {
    int total = 0;
    for (int i = 0; i < LENGTH; i++) {
      total += a[i];
    }
    return total;
  }

  // In the following two cases, addv sn vm.4s can be generated.
  // The operator can be sub, mul or other operators which can be
  // vectorized easily.
  public static int reduceAddSumofSubInt(int[] a, int[] b) {
    int total = 0;
    for (int i = 0; i < LENGTH; i++) {
      total += (a[i] - b[i]);
    }
    return total;
  }

  public static int reduceAddSumofMulInt(int[] a, int[] b) {
    int total = 0;
    for (int i = 0; i < LENGTH; i++) {
      total += (a[i] * b[i]);
    }
    return total;
  }


  // In the following three cases, addv hn vm.4h can't be generated.
  public static int reduceAddShort(short[] a, short[] b) {
    int total = 0;
    for (int i = 0; i < LENGTH; i++) {
      total += a[i];
    }
    return total;
  }

  public static int reduceAddSumofSubShort(short[] a, short[] b) {
    int total = 0;
    for (int i = 0; i < LENGTH; i++) {
      total += (a[i] - b[i]);
    }
    return total;
  }

  public static int reduceAddSumofMulShort(short[] a, short[] b) {
    int total = 0;
    for (int i = 0; i < LENGTH; i++) {
      total += (a[i] * b[i]);
    }
    return total;
  }

  @Setup
  public void setup()
  {
    TestReduceAddInit();
  }

  @Benchmark
  public void testReduceAddInt() {
    int sum;
    sum = reduceAddInt(a, b);
    sum = reduceAddSumofSubInt(a, b);
    sum = reduceAddSumofMulInt(a, b);
  }

  @Benchmark
  public void testReduceAddShort() {
    int sum;
    sum = reduceAddShort(sa, sb);
    sum = reduceAddSumofSubShort(sa, sb);
    sum = reduceAddSumofMulShort(sa, sb);
  }

}
