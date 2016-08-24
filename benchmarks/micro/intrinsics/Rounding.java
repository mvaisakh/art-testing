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
  // The java.util.Random only generates random float/double numbers between 0 and 1.
  // So we implement our own random floating point numbers here,
  // based on a well known quick and dirty approach.
  static final class MyRandom extends Random {
    static int im = 139968;
    static int ia = 3877;
    static int ic = 29573;
    static int seed = 0;

    public double nextDouble() {
      double scale = 1000.0d / im;
      seed = (seed * ia + ic) % im;
      return scale * seed;
    }

    public float nextFloat() {
      float scale = 1000.0f / im;
      seed = (seed * ia + ic) % im;
      return scale * seed;
    }
  }

  /* Invoke each intrinsic in question the same no. of times */
  private static final int NUM_INVOKES = 64;

  /* Random pool size.
   * Use a power of two to make the modulo operations below fast. */
  private static final int NUM_RANDS = 1024;

  /* Pre-allocated pool of random numbers. */
  private static final float[] rand_float = new float[NUM_RANDS];
  private static final double[] rand_double = new double[NUM_RANDS];

  private static final float[] rand_pos_float = new float[NUM_RANDS];
  private static final double[] rand_pos_double = new double[NUM_RANDS];

  private static final float[] rand_neg_float = new float[NUM_RANDS];
  private static final double[] rand_neg_double = new double[NUM_RANDS];

  // These are written but not read. The computation routines below store their
  // result to these static variables to ensure the computation code is not
  // removed by DCE.
  private static float res_float;
  private static double res_double;
  private static int res_int;
  private static long res_long;

  static {
    MyRandom rand = new MyRandom();

    for (int i = 0; i < NUM_RANDS; i++) {
      rand_pos_float[i] = rand.nextFloat();
      rand_pos_double[i] = rand.nextDouble();

      rand_neg_float[i] = rand.nextFloat() * -1f;
      rand_neg_double[i] = rand.nextDouble() * -1f;

      if (rand.nextInt() % 2 == 0) {
        rand_float[i] =  rand_pos_float[i];
        rand_double[i] = rand_pos_double[i];
      } else {
        rand_float[i] =  rand_neg_float[i];
        rand_double[i] = rand_neg_double[i];
      }
    }
  }

  public void timeRoundPositiveFloat(int iterations) {
    int res = 0;
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        res += Math.round(rand_pos_float[i % NUM_RANDS]);
      }
    }
    res_int = res;
  }

  public void timeRoundNegativeFloat(int iterations) {
    int res = 0;
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        res += Math.round(rand_neg_float[i % NUM_RANDS]);
      }
    }
    res_int = res;
  }

  public void timeRoundFloat(int iterations) {
    int res = 0;
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        res += Math.round(rand_float[i % NUM_RANDS]);
      }
    }
    res_int = res;
  }

  public void timeRoundPositiveDouble(int iterations) {
    long res = 0;
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        res += Math.round(rand_pos_double[i % NUM_RANDS]);
      }
    }
    res_double = res;
  }

  public void timeRoundNegativeDouble(int iterations) {
    long res = 0;
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        res += Math.round(rand_neg_double[i % NUM_RANDS]);
      }
    }
    res_long = res;
  }

  public void timeRoundDouble(int iterations) {
    long res = 0;
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        res += Math.round(rand_double[i % NUM_RANDS]);
      }
    }
    res_double = res;
  }

  public void timeFloorPositiveFloat(int iterations) {
    float res = 0.0f;
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        res += Math.floor(rand_pos_float[i % NUM_RANDS]);
      }
    }
    res_float = res;
  }

  public void timeFloorNegativeFloat(int iterations) {
    float res = 0.0f;
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        res += Math.floor(rand_neg_float[i % NUM_RANDS]);
      }
    }
    res_float = res;
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

  public void timeFloorPositiveDouble(int iterations) {
    double res = 0.0;
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        res += Math.floor(rand_pos_double[i % NUM_RANDS]);
      }
    }
    res_double = res;
  }

  public void timeFloorNegativeDouble(int iterations) {
    double res = 0.0;
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        res += Math.floor(rand_neg_double[i % NUM_RANDS]);
      }
    }
    res_double = res;
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

  public void timeCeilPositiveFloat(int iterations) {
    float res = 0.0f;
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        res += Math.ceil(rand_pos_float[i % NUM_RANDS]);
      }
    }
    res_float = res;
  }

  public void timeCeilNegativeFloat(int iterations) {
    float res = 0.0f;
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        res += Math.ceil(rand_neg_float[i % NUM_RANDS]);
      }
    }
    res_float = res;
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

  public void timeCeilPositiveDouble(int iterations) {
    double res = 0.0;
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        res += Math.ceil(rand_pos_double[i % NUM_RANDS]);
      }
    }
    res_double = res;
  }


  public void timeCeilNegativeDouble(int iterations) {
    double res = 0.0;
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        res += Math.ceil(rand_neg_double[i % NUM_RANDS]);
      }
    }
    res_double = res;
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

  private static final int ITER_COUNT = 100000;

  public static void main(String[] args) {
    Rounding obj = new Rounding();
    long before = System.currentTimeMillis();

    obj.timeRoundPositiveFloat(ITER_COUNT);
    obj.timeRoundNegativeFloat(ITER_COUNT);
    obj.timeRoundFloat(ITER_COUNT);

    obj.timeRoundPositiveDouble(ITER_COUNT);
    obj.timeRoundNegativeDouble(ITER_COUNT);
    obj.timeRoundDouble(ITER_COUNT);

    obj.timeFloorPositiveFloat(ITER_COUNT);
    obj.timeFloorNegativeFloat(ITER_COUNT);
    obj.timeFloorFloat(ITER_COUNT);

    obj.timeFloorPositiveDouble(ITER_COUNT);
    obj.timeFloorNegativeDouble(ITER_COUNT);
    obj.timeFloorDouble(ITER_COUNT);

    obj.timeCeilPositiveFloat(ITER_COUNT);
    obj.timeCeilNegativeFloat(ITER_COUNT);
    obj.timeCeilFloat(ITER_COUNT);

    obj.timeCeilPositiveDouble(ITER_COUNT);
    obj.timeCeilNegativeDouble(ITER_COUNT);
    obj.timeCeilDouble(ITER_COUNT);

    obj.timeFloorFloat(ITER_COUNT);
    obj.timeFloorDouble(ITER_COUNT);
    obj.timeCeilFloat(ITER_COUNT);
    obj.timeCeilDouble(ITER_COUNT);

    long after = System.currentTimeMillis();

    System.out.println("benchmarks/micro/intrinsics/Rounding: " + (after - before));
  }
}
