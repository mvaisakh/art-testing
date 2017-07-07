/*
 * Copyright (c) 2017, Linaro Limited. All rights reserved.
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
 * Description:     Simple loops around array index bounds check performance.
 * Main Focus:      Test the compiler's ability to perform array bounds check elemination.
 */

package benchmarks.micro;

import java.lang.System;
import java.util.Random;

public class ArrayBoundsCheck {

  private static final int NUM_INVOKES = 500;

  /* Random pool size. */
  private static final int NUM_RANDS = 1024;

  /* Pre-allocated pool of random numbers. */
  private static final long[] static_array = new long[NUM_RANDS];

  // These are written but not read. The computation routines below store their
  // result to these static variables to ensure the computation code is not
  // removed by DCE.
  private static int res_int;
  private static long res_long;

  static {
    Random rand = new Random();

    for (int i = 0; i < NUM_RANDS; i++) {
      static_array[i] = rand.nextLong();
    }
  }

  private void init_array(long[] array) {
    for (int i = 0; i < array.length; ++i) {
      array[i] = i;
    }
  }

  public void timeStaticArray_ModStaticField(int iterations) {
    long res = 0;
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        long a = static_array[i % NUM_RANDS];
        long b = static_array[(i + 1) % NUM_RANDS];
        res += a + b;
      }
    }
    res_long = res;
  }

  public void timeStaticArray_ModLength(int iterations) {
    long res = 0;
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        long a = static_array[i % static_array.length];
        long b = static_array[(i + 1) % static_array.length];
        res += a + b;
      }
    }
    res_long = res;
  }

  public void timeStaticArray_ModConst(int iterations) {
    long res = 0;
    int length = 1024; // Same as NUM_RANDS, except that it's a local const value.
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        long a = static_array[i % length];
        long b = static_array[(i + 1) % length];
        res += a + b;
      }
    }
    res_long = res;
  }

  public void timeLocalArray_ModStaticField(int iterations) {
    long res = 0;
    long[] array = new long[NUM_RANDS];
    init_array(array);
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        long a = array[i % NUM_RANDS];
        long b = array[(i + 1) % NUM_RANDS];
        res += a + b;
      }
    }
    res_long = res;
  }

  public void timeLocalArray_ModLength(int iterations) {
    long res = 0;
    long[] array = new long[NUM_RANDS];
    init_array(array);
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        long a = array[i % array.length];
        long b = array[(i + 1) % array.length];
        res += a + b;
      }
    }
    res_long = res;
  }

  public void timeLocalArray_ModConst(int iterations) {
    long res = 0;
    long[] array = new long[NUM_RANDS];
    init_array(array);
    int length = 1024; // Same as NUM_RANDS, except that it's a local const value.
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        long a = array[i % length];
        long b = array[(i + 1) % length];
        res += a + b;
      }
    }
    res_long = res;
  }

  public static boolean verify() {
    ArrayBoundsCheck obj = new ArrayBoundsCheck();

    obj.timeLocalArray_ModConst(1);
    obj.timeLocalArray_ModLength(1);
    obj.timeLocalArray_ModStaticField(1);
    obj.timeStaticArray_ModConst(1);
    obj.timeStaticArray_ModLength(1);
    obj.timeStaticArray_ModStaticField(1);

    return true;
  }

  private static final int ITER_COUNT = 300000;

  public static void main(String[] args) {
    int rc = 0;
    ArrayBoundsCheck obj = new ArrayBoundsCheck();

    long before = System.currentTimeMillis();
    obj.timeStaticArray_ModStaticField(ITER_COUNT);
    long after = System.currentTimeMillis();
    System.out.println("benchmarks/optimizing/ArrayBoundsCheck/StaticArray_ModField: "
                       + (after - before));

    before = System.currentTimeMillis();
    obj.timeStaticArray_ModLength(ITER_COUNT);
    after = System.currentTimeMillis();
    System.out.println("benchmarks/optimizing/ArrayBoundsCheck/StaticArray_ModLength: "
                       + (after - before));

    before = System.currentTimeMillis();
    obj.timeStaticArray_ModConst(ITER_COUNT);
    after = System.currentTimeMillis();
    System.out.println("benchmarks/optimizing/ArrayBoundsCheck/StaticArray_ModConst: "
                       + (after - before));

    before = System.currentTimeMillis();
    obj.timeLocalArray_ModStaticField(ITER_COUNT);
    after = System.currentTimeMillis();
    System.out.println("benchmarks/optimizing/ArrayBoundsCheck/LocalArray_ModField: "
                       + (after - before));

    before = System.currentTimeMillis();
    obj.timeLocalArray_ModLength(ITER_COUNT);
    after = System.currentTimeMillis();
    System.out.println("benchmarks/optimizing/ArrayBoundsCheck/LocalArray_ModLength: "
                       + (after - before));

    before = System.currentTimeMillis();
    obj.timeLocalArray_ModConst(ITER_COUNT);
    after = System.currentTimeMillis();
    System.out.println("benchmarks/optimizing/ArrayBoundsCheck/LocalArray_ModConst: "
                       + (after - before));

    if (!verify()) {
      rc++;
    }

    System.exit(rc);
  }
}
