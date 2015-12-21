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

public class LockTest {

  public static final int ITERATIONS = 200000;

  public static void main(String[] args) {
    long before = System.currentTimeMillis();
    timeStaticLockWithDepth1(ITERATIONS);
    long after = System.currentTimeMillis();
    System.out.println("static(1)   : " + (after - before));

    before = System.currentTimeMillis();
    timeStaticLockWithDepth2(ITERATIONS);
    after = System.currentTimeMillis();
    System.out.println("static(2)   : " + (after - before));

    before = System.currentTimeMillis();
    timeStaticLockWithDepth20(ITERATIONS);
    after = System.currentTimeMillis();
    System.out.println("static(20) : " + (after - before));

    LockTest l = new LockTest();
    before = System.currentTimeMillis();
    l.timeDynamicLockWithDepth1(ITERATIONS);
    after = System.currentTimeMillis();
    System.out.println("dynamic(1)  : " + (after - before));

    before = System.currentTimeMillis();
    l.timeDynamicLockWithDepth2(ITERATIONS);
    after = System.currentTimeMillis();
    System.out.println("dynamic(2)  : " + (after - before));

    before = System.currentTimeMillis();
    l.timeDynamicLockWithDepth20(ITERATIONS);
    after = System.currentTimeMillis();
    System.out.println("dynamic(20): " + (after - before));
  }

  public static synchronized void timeStaticLockWithDepth1(int iters) {
    for (int i = 0; i < iters; i++) {
      staticLockWithDepth(1);
    }
  }

  @IterationsAnnotation(noWarmup = true)
  public static synchronized void timeStaticLockWithDepth2(int iters) {
    for (int i = 0; i < iters; i++) {
      staticLockWithDepth(2);
    }
  }

  @IterationsAnnotation(noWarmup = true)
  public static synchronized void timeStaticLockWithDepth20(int iters) {
    for (int i = 0; i < iters; i++) {
      staticLockWithDepth(20);
    }
  }

  public synchronized void timeDynamicLockWithDepth1(int iters) {
    for (int i = 0; i < iters; i++) {
      dynamicLockWithDepth(1);
    }
  }

  public synchronized void timeDynamicLockWithDepth2(int iters) {
    for (int i = 0; i < iters; i++) {
      dynamicLockWithDepth(2);
    }
  }

  @IterationsAnnotation(noWarmup = true)
  public synchronized void timeDynamicLockWithDepth20(int iters) {
    for (int i = 0; i < iters; i++) {
      dynamicLockWithDepth(20);
    }
  }

  public static synchronized void staticLockWithDepth(int depth) {
    if (depth == 1) return;
    staticLockWithDepth(depth - 1);
  }

  public synchronized void dynamicLockWithDepth(int depth) {
    if (depth == 1) return;
    dynamicLockWithDepth(depth - 1);
  }
}

