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

import java.util.Random;

public class GCTest {

  public final static int FACTOR = 10;
  public final static int ITERATIONS = 5;
  public final static int LIVE = FACTOR * 10;
  public final static int BORN = LIVE * FACTOR * 100;
  public final static int SIZE = FACTOR;

  public static void main(String[] args) {
    long before = System.currentTimeMillis();
    timeSmash(BORN);
    long after = System.currentTimeMillis();
    System.out.println("GCTest: " + (after - before));
  }

  public GCTest() {
  }

  public static void timeSmash(int iters) {
      GCTest[] list = new GCTest[LIVE];
      Random rnd = new Random(123456789);
      for (int i = 0; i < iters; i++) {
          smash(list, rnd);
      }
  }

  public static void smash(GCTest[] list, Random rnd) {
    for (int i = 0; i < ITERATIONS; i++) {
      int index = rnd.nextInt(list.length);
      int size = rnd.nextInt(SIZE);
      list[index] = new GCTest(facebook, size);
    }
  }

  public static Object facebook = new Object();

  public GCTest(Object ptr, int varSize) {
    pointer = ptr;
    variable = new int[varSize];
  }

  private Object pointer = null;
  private int[] variable = null;
}

