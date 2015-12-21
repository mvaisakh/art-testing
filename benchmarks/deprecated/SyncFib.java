/*
 *    Copyright 2015 ARM Limited
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

package benchmarks.deprecated;

import java.lang.System;

import org.linaro.bench.IterationsAnnotation;

public class SyncFib {

  public static final int ITERATIONS_SFIB = 300;
  public static final int ITERATIONS_AFIB = 500;

  public static void main(String[] args) {
    long sum = 0;
    long before = System.currentTimeMillis();
    timeSfib(ITERATIONS_SFIB);
    long after = System.currentTimeMillis();
    System.out.println("sfib: " + (after - before));
    sum = 0;
    before = System.currentTimeMillis();
    timeAfib(ITERATIONS_AFIB);
    after = System.currentTimeMillis();
    System.out.println("afib: " + (after - before));
  }

  @IterationsAnnotation(noWarmup = true, iterations = 600)
  public static long timeSfib(int iters) {
    long sum = 0;
    for (int i = 0; i < iters; i++) {
      sum += sfib(20);
    }
    return sum;
  }

  @IterationsAnnotation(noWarmup = true, iterations = 1000)
  public static long timeAfib(int iters) {
    long sum = 0;
    for (int i = 0; i < iters; i++) {
      sum += afib(20);
    }
    return sum;
  }

  static synchronized int sfib(int n) {
    if (n < 2) return 1;
    return sfib(n - 1) + sfib(n - 2);
  }

  static int afib(int n) {
    if (n < 2) return 1;
    return afib(n - 1) + afib(n - 2);
  }
}
