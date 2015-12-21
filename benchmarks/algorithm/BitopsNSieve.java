/*
 * Copyright (C) 2015 Linaro Limited. All rights received.
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
 */

/* Ported from:
 *  webkit/PerformanceTests/SunSpider/tests/sunspider-1.0.2/bitops-nsieve-bits.js
 *
 * The Great Computer Language Shootout
 * http://benchmarksgame.alioth.debian.org/
 *
 * Contributed by Ian Osgood
 */

/*
 * Description:     Bitops prime number sieve, used for finding prime numbers.
 * Main Focus:      Bitwise operations.
 *
 */

package benchmarks.algorithm;

import java.lang.Integer;
import java.lang.System;

public class BitopsNSieve {
  private static final int BITOPS_NSIEVE_EXPECTED = -1431657430;

  // As per the original, this function is not used.
  private String pad(int n, int width) {
    String s = Integer.toString(n);
    while (s.length() < width) {
      s = ' ' + s;
    }
    return s;
  }

  private void primes(int[] isPrime, int n) {
    // As per the original, this variable can be optimised out.
    int count = 0;
    final int m = 10000 << n;
    final int size = m + 31 >> 5;

    for (int i = 0; i < size; i++) {
      isPrime[i] = 0xffffffff;
    }

    for (int i = 2; i < m; i++) {
      if ((isPrime[i >> 5] & 1 << (i & 31)) != 0) {
        for (int j = i + 1; j < m; j += i) {
          isPrime[j >> 5] &= ~ (1 << (j & 31));
        }
        count++;
      }
    }
  }

  private int[] sieve() {
    int[] isPrime = null;
    // As per the original, this loop will have just one iteration.
    for (int i = 4; i <= 4; i++) {
      isPrime = new int[(10000 << i) + 31 >> 5];
      primes(isPrime, i);
    }
    return isPrime;
  }

  public void timeBitopsNSieve(int iters) {
    for (int i = 0; i < iters; i++) {
      sieve();
    }
  }

  public boolean verify() {
    int output = 0;
    int[] result = sieve();

    for (int i = 0; i < result.length; i++) {
      output += result[i];
    }
    return output == BITOPS_NSIEVE_EXPECTED;
  }

  public static void main(String[] argv) {
    BitopsNSieve obj = new BitopsNSieve();
    final long before = System.currentTimeMillis();
    obj.timeBitopsNSieve(189);
    final long after = System.currentTimeMillis();
    obj.verify();
    System.out.println("BitopsNSieve: " + (after - before));
  }
}
