/*
 *    Copyright 2015 Linaro Ltd.
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

package benchmarks.algorithm;

import java.lang.System;


public class NSieve {
  /* Expected result for the standard benchmark setup */
  private static final int EXPECTED = 14302;
  /* Number of repeats (internal, not benchmark iterations) */
  private static final int NUM_SIEVES = 4;
  /* Array of flags - big enough for all standard test scenarios */
  private static boolean[] flags = new boolean[80001];
  private static int[] results = new int[NUM_SIEVES];

  private int nsieve(int m) {
    for (int i = 2; i <= m; i++) {
      flags[i] = true;
    }

    int count = 0;
    for (int i = 2; i <= m; i++) {
      if (flags[i]) {
        for (int k = i + i; k <= m; k += i) {
          flags[k] = false;
        }
        count++;
      }
    }
    return count;
  }

  /**
   * Find prime numbers in three sizes of pool, four times over
   * Repeat over number of iterations set by framework
   **/
  public void timeNSieveAccess(int iterations) {
    for (int iter = 0; iter < iterations; iter++) {
      for (int i = 0; i < NUM_SIEVES; i++) {
        int sum = 0;
        for (int o = 1; o <= 3; o++) {
          int m = (1 << o) * 10000;
          sum += nsieve(m);
        }
        results[i] = sum;
      }
    }
  }

  /**
   * Called by the framework to assert the benchmarks have done the right thing.
   **/
  public boolean verify() {
    for (int i = 0; i < NUM_SIEVES; i++) {
      if (results[i] != EXPECTED) {
        return false;
      }
    }
    return true;
  }

  /**
   * *NOT* called by the framework by default, provided for direct use only.
   **/
  public static void main(String[] args) {
    NSieve obj = new NSieve();
    long before = System.currentTimeMillis();

    obj.timeNSieveAccess(100);
    long after = System.currentTimeMillis();
    System.out.println("NSieve.NSieveAccess: " + (after - before));
  }
}
