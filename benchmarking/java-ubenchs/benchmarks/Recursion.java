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

// This benchmark measures time taken for recursive method calls.
public class Recursion {
  private int MAX_VAL = 64;
  private long result;

  public Recursion() {
  }

  public int SumSeriesA(int num) {
    if (num == 0) {
      return num;
    } else {
      return num + SumSeriesA(num - 1);
    }
  }

  public void timeSumSeriesA(int iterations) {
    for (int iter = 0; iter < iterations; ++iter) {
      this.result = 0;
      for (int i = 0; i < MAX_VAL; i++) {
        this.result += SumSeriesA(i);
      }
    }
    return;
  }

  public int SumSeriesB(int num) {
    if (num == 0) {
      return num;
    } else if ((num & 0x1) != 0) {
      return num + 1 + SumSeriesB(num - 1);
    } else {
      return num + SumSeriesB(num - 1);
    }
  }

  public void timeSumSeriesB(int iterations) {
    for (int iter = 0; iter < iterations; ++iter) {
      this.result = 0;
      for (int i = 0; i < MAX_VAL; i++) {
        this.result += SumSeriesB(i);
      }
    }
    return;
  }

  public static void main(String[] args) {
    long before;
    long after;
    Recursion obj = new Recursion();

    before = System.currentTimeMillis();
    obj.timeSumSeriesA(10);
    after = System.currentTimeMillis();
    System.out.println("Recursion Sum Series A : " + (after - before));

    before = System.currentTimeMillis();
    obj.timeSumSeriesB(10);
    after = System.currentTimeMillis();
    System.out.println("Recursion Sum Series B : " + (after - before));
  }
}

