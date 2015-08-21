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

import java.lang.System;

public class ControlFlow {

  public final static int ITERATIONS = 100000;
  public final static int VALUE = 500;

  public static void main(String[] args) {
    long before = System.currentTimeMillis();
    int sum = timeForUp(ITERATIONS);
    long after = System.currentTimeMillis();
    System.out.println("forUp(" + VALUE + "): " + (after - before));

    before = System.currentTimeMillis();
    sum = timeForDown(ITERATIONS);
    after = System.currentTimeMillis();
    System.out.println("forDown(" + VALUE + "): " + (after - before));

    before = System.currentTimeMillis();
    sum = timeWhileUp(ITERATIONS);
    after = System.currentTimeMillis();
    System.out.println("whileUp(" + VALUE + "): " + (after - before));

    before = System.currentTimeMillis();
    sum = timeWhileDown(ITERATIONS);
    after = System.currentTimeMillis();
    System.out.println("whileDown(" + VALUE + "): " + (after - before));

    before = System.currentTimeMillis();
    sum = timeDoWhileUp(ITERATIONS);
    after = System.currentTimeMillis();
    System.out.println("doWhileUp(" + VALUE + "): " + (after - before));

    before = System.currentTimeMillis();
    sum = timeDoWhileDown(ITERATIONS);
    after = System.currentTimeMillis();
    System.out.println("doWhileDown(" + VALUE + "): " + (after - before));
  }

  public static int timeForUp(int iters) {
    int sum = 0;

    for (int i = 0; i < iters; i++) {
      sum = 0;
      for (int j = 0; j < VALUE; j++) {
        sum += j;
      }
    }

    return sum;
  }

  public static int timeForDown(int iters) {
    int sum = 0;

    for (int i = 0; i < iters; i++) {
      sum = 0;
      for (int j = VALUE - 1; j >= 0; j--) {
        sum += j;
      }
    }

    return sum;
  }

  public static int timeWhileUp(int iters) {
    int sum = 0;

    for (int i = 0; i < iters; i++) {
      sum = 0;
      int j = -1;
      while (++j < VALUE) {
        sum += j;
      }
    }

    return sum;
  }

  public static int timeWhileDown(int iters) {
    int sum = 0;

    for (int i = 0; i < iters; i++) {
      sum = 0;
      int j = VALUE - 1;
      while (j >= 0) {
        sum += j--;
      }
    }

    return sum;
  }

  public static int timeDoWhileUp(int iters) {
    int sum = 0;

    for (int i = 0; i < iters; i++) {
      sum = 0;
      int j = 0;
      do {
        sum += j++;
      } while (j < VALUE);
    }

    return sum;
  }

  public static int timeDoWhileDown(int iters) {
    int sum = 0;

    for (int i = 0; i < iters; i++) {
      sum = 0;
      int j = VALUE - 1;
      do {
        sum += j--;
      } while (j >= 0);
    }

    return sum;
  }
}

