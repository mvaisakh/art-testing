/*
 * Copyright (c) 2015, ARM Limited. All rights reserved.
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

package benchmarks.micro.intrinsics;

import java.lang.System;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

public class Intrinsics {

  /* Invoke each intrinsic in question the same no. of times */
  private static final int NUM_INVOKES = 64;

  /* Random pool size.
   * Use a power of two to make the modulo operations below fast. */
  private static final int NUM_RANDS = 1024;

  /* Pre-allocated pool of random integers from [0, Integer.MAX_VALUE) */
  private static final int[] rand = new int[NUM_RANDS];

  static {
    // Allocate a pool of random integers to use in benchmarks that
    // want to try and force branch mispredicts without the overhead
    // of calling random.nextInt in times code. Same seed every time.
    Random random = new Random(42L);
    for (int i = 0; i < NUM_RANDS; i++) {
      rand[i] = random.nextInt(Integer.MAX_VALUE);
    }
  }

  /**
   * NumberOfLeadingZeros.
   **/

  private static int[] resultsNumberOfLeadingZerosInteger = new int[NUM_INVOKES];
  private static int[] resultsNumberOfLeadingZerosLong = new int[NUM_INVOKES];
  private static int[] resultsNumberOfLeadingZerosIntegerRandom = new int[NUM_INVOKES];
  private static int[] resultsNumberOfLeadingZerosLongRandom = new int[NUM_INVOKES];

  public void timeNumberOfLeadingZerosInteger(int iterations) {
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        resultsNumberOfLeadingZerosInteger[i] =
          Integer.numberOfLeadingZeros(0x80000000 >>> i);
      }
    }
  }

  public void timeNumberOfLeadingZerosLong(int iterations) {
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        resultsNumberOfLeadingZerosLong[i] =
          Long.numberOfLeadingZeros(0x8000000000000000L >>> i);
      }
    }
  }

  public void timeNumberOfLeadingZerosIntegerRandom(int iterations) {
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        resultsNumberOfLeadingZerosIntegerRandom[i] =
          Integer.numberOfLeadingZeros(rand[i % NUM_RANDS] >>> rand[i % NUM_RANDS]);
      }
    }
  }

  public void timeNumberOfLeadingZerosLongRandom(int iterations) {
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        resultsNumberOfLeadingZerosLongRandom[i] =
          Long.numberOfLeadingZeros(1 << (rand[i % NUM_RANDS] % Long.SIZE));
      }
    }
  }

  /**
   * NumberOfTrailingZeros.
   **/

  private static int[] resultsNumberOfTrailingZerosInteger = new int[NUM_INVOKES];
  private static int[] resultsNumberOfTrailingZerosLong = new int[NUM_INVOKES];
  private static int[] resultsNumberOfTrailingZerosIntegerRandom = new int[NUM_INVOKES];
  private static int[] resultsNumberOfTrailingZerosLongRandom = new int[NUM_INVOKES];

  public void timeNumberOfTrailingZerosInteger(int iterations) {
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        resultsNumberOfTrailingZerosInteger[i] =
          Integer.numberOfTrailingZeros(0x80000000 >>> i);
      }
    }
  }

  public void timeNumberOfTrailingZerosLong(int iterations) {
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        resultsNumberOfTrailingZerosLong[i] =
          Long.numberOfTrailingZeros(0x8000000000000000L >>> i);
      }
    }
  }

  public void timeNumberOfTrailingZerosIntegerRandom(int iterations) {
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        resultsNumberOfTrailingZerosIntegerRandom[i] =
          Integer.numberOfTrailingZeros(rand[i % NUM_RANDS] >>> rand[i % NUM_RANDS]);
      }
    }
  }

  public void timeNumberOfTrailingZerosLongRandom(int iterations) {
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        resultsNumberOfTrailingZerosLongRandom[i] =
          Long.numberOfTrailingZeros(1 << (rand[i % NUM_RANDS] % Long.SIZE));
      }
    }
  }

  /**
   * BitCount.
   **/

  // both Integer and Long's bitCount() return int type.
  private static int[] resultsBitCountInteger = new int[NUM_INVOKES];
  private static int[] resultsBitCountLong = new int[NUM_INVOKES];
  private static int[] resultsBitCountIntegerRandom = new int[NUM_INVOKES];
  private static int[] resultsBitCountLongRandom = new int[NUM_INVOKES];

  public void timeBitCountInteger(int iterations) {
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        resultsBitCountInteger[i] = Integer.bitCount(0x1234abcd);
      }
    }
  }

  public void timeBitCountLong(int iterations) {
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        resultsBitCountLong[i] = Long.bitCount(0x1234abcd1234abcdL);
      }
    }
  }

  public void timeBitCountIntegerRandom(int iterations) {
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        resultsBitCountIntegerRandom[i] = Integer.bitCount(rand[i % NUM_RANDS]);
      }
    }
  }

  public void timeBitCountLongRandom(int iterations) {
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        resultsBitCountLongRandom[i] =
          Long.bitCount((long)rand[i % NUM_RANDS] << 32 + rand[i % NUM_RANDS]);
      }
    }
  }

  /**
   * RotateRight.
   **/

  private static int[] resultsRotateRightInteger = new int[NUM_INVOKES];
  private static int[] resultsRotateRightIntegerConstant = new int[NUM_INVOKES];
  private static long[] resultsRotateRightLong = new long[NUM_INVOKES];
  private static long[] resultsRotateRightLongConstant = new long[NUM_INVOKES];

  public void timeRotateRightInteger(int iterations) {
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        resultsRotateRightInteger[i] = Integer.rotateRight(0xFF0000DD, i);
      }
    }
  }

  public void timeRotateRightIntegerConstant(int iterations) {
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        resultsRotateRightIntegerConstant[i] = Integer.rotateRight(0xFF0000DD, 16);
      }
    }
  }

  public void timeRotateRightLong(int iterations) {
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        resultsRotateRightLong[i] = Long.rotateRight(0xBBAAAADDFF0000DDL, i);
      }
    }
  }

  public void timeRotateRightLongConstant(int iterations) {
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        resultsRotateRightLongConstant[i] = Long.rotateRight(0xBBAAAADDFF0000DDL, 48);
      }
    }
  }

  /**
   * RotateLeft.
   **/

  private static int[] resultsRotateLeftInteger = new int[NUM_INVOKES];
  private static int[] resultsRotateLeftIntegerConstant = new int[NUM_INVOKES];
  private static long[] resultsRotateLeftLong = new long[NUM_INVOKES];
  private static long[] resultsRotateLeftLongConstant = new long[NUM_INVOKES];

  public void timeRotateLeftInteger(int iterations) {
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        resultsRotateLeftInteger[i] = Integer.rotateLeft(0xFF0000DD, i);
      }
    }
  }

  public void timeRotateLeftIntegerConstant(int iterations) {
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        resultsRotateLeftIntegerConstant[i] = Integer.rotateLeft(0xFF0000DD, 16);
      }
    }
  }

  public void timeRotateLeftLong(int iterations) {
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        resultsRotateLeftLong[i] = Long.rotateLeft(0xBBAAAADDFF0000DDL, i);
      }
    }
  }

  public void timeRotateLeftLongConstant(int iterations) {
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        resultsRotateLeftLongConstant[i] = Long.rotateLeft(0xBBAAAADDFF0000DDL, 48);
      }
    }
  }

  /**
   * RotateRandom.
   **/

  private static int[] resultsRotateRandomInteger = new int[NUM_INVOKES];
  private static long[] resultsRotateRandomLong = new long[NUM_INVOKES];

  public void timeRotateRandomInteger(int iterations) {
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        resultsRotateRandomInteger[i] = (rand[i % NUM_RANDS] % 2 > 0)
          ? Integer.rotateLeft(0xFF0000DD, rand[i % NUM_RANDS])
          : Integer.rotateRight(0xFF0000DD, rand[i % NUM_RANDS]);
      }
    }
  }

  public void timeRotateRandomLong(int iterations) {
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 0; i < NUM_INVOKES; ++i) {
        resultsRotateRandomLong[i] = (rand[i % NUM_RANDS] % 2 > 0)
          ? Long.rotateLeft(0xBBAAAADDFF0000DDL, rand[i % NUM_RANDS])
          : Long.rotateRight(0xBBAAAADDFF0000DDL, rand[i % NUM_RANDS]);
      }
    }
  }

  /**
   * Verify.
   **/

  /**
   * Called by the framework to assert the benchmarks have done the right thing.
   **/
  public boolean verify() {
    return true;
  }

  /**
   * *NOT* called by the framework by default, provided for direct use only.
   **/
  public static void main(String[] args) {
    Intrinsics obj = new Intrinsics();
    long before = System.currentTimeMillis();
    obj.timeNumberOfLeadingZerosInteger(100000);
    obj.timeNumberOfLeadingZerosIntegerRandom(100000);
    obj.timeNumberOfLeadingZerosLong(100000);
    obj.timeNumberOfLeadingZerosLongRandom(100000);

    obj.timeNumberOfTrailingZerosInteger(100000);
    obj.timeNumberOfTrailingZerosIntegerRandom(100000);
    obj.timeNumberOfTrailingZerosLong(100000);
    obj.timeNumberOfTrailingZerosLongRandom(100000);

    obj.timeRotateRightInteger(100000);
    obj.timeRotateRightLong(100000);
    obj.timeRotateLeftInteger(100000);
    obj.timeRotateLeftLong(100000);

    obj.timeRotateRandomInteger(100000);
    obj.timeRotateRandomLong(100000);

    obj.timeBitCountInteger(100000);
    obj.timeBitCountLong(100000);
    obj.timeBitCountIntegerRandom(100000);
    obj.timeBitCountLongRandom(100000);

    long after = System.currentTimeMillis();
    System.out.println("benchmarks/micro/intrinsics/Intrinsics: " + (after - before));
  }
}
