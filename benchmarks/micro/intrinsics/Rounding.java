/*
 * Copyright (c) 2016, Linaro Limited. All rights reserved.
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
 * Description:     Simple loops around rounding-related intrinsics.
 * Main Focus:      Rounding-related intrinsics.
 */

package benchmarks.micro.intrinsics;

import java.lang.System;
import java.util.Random;

public class Rounding {

  /* Invoke each intrinsic in question the same no. of times */
  private static final int NUM_INVOKES = 64;

  /* Random pool size.
   * Use a power of two to make the modulo operations below fast. */
  private static final int NUM_RANDS = 1024;

  /* Pre-allocated pool of random numbers. */
  private static final float[] rand_float = new float[NUM_RANDS];
  private static final double[] rand_double = new double[NUM_RANDS];

  // These are written but not read. The computation routines below store their
  // result to these static variables to ensure the computation code is not
  // removed by DCE.
  private static float res_float;
  private static double res_double;

  static {
    Random random = new Random(42L);
    for (int i = 0; i < NUM_RANDS; i++) {
      rand_float[i] = random.nextFloat();
      rand_double[i] = random.nextDouble();
    }
  }

  public void timeFloorFloat(int iterations) {
    float res = 0.0f;
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        res += Math.floor(rand_float[i % NUM_RANDS]);
      }
    }
    res_float = res;
  }

  public void timeFloorDouble(int iterations) {
    double res = 0.0;
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        res += Math.floor(rand_double[i % NUM_RANDS]);
      }
    }
    res_double = res;
  }

  public void timeCeilFloat(int iterations) {
    float res = 0.0f;
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        res += Math.ceil(rand_float[i % NUM_RANDS]);
      }
    }
    res_float = res;
  }

  public void timeCeilDouble(int iterations) {
    double res = 0.0;
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        res += Math.ceil(rand_double[i % NUM_RANDS]);
      }
    }
    res_double = res;
  }

  public static void main(String[] args) {
    Rounding obj = new Rounding();
    long before = System.currentTimeMillis();
    obj.timeFloorFloat(100000);
    obj.timeFloorDouble(100000);
    obj.timeCeilFloat(100000);
    obj.timeCeilDouble(100000);
    long after = System.currentTimeMillis();

    System.out.println("benchmarks/micro/Rounding: " + (after - before));
  }
}
