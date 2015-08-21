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

// This benchmark performs various loop operations.
public class Loop {
  public int LOOP_COUNT = 500;
  public int VALUE1 = 50;
  public int VALUE2 = 90;
  public int result;

  public Loop() {
  }

  public void timeInductionVar(int iterations) {
    for (int iter = 0; iter < iterations; ++iter) {
      int result1 = 0;
      int result2 = 0;
      for (int i = 0; i < LOOP_COUNT; i++) {
        int temp = this.VALUE1 + this.VALUE2;
        result1 += temp;
        result2 += 10;
      }
      this.result = result1 + result2;
    }
    return;
  }

  public static void main(String[] args) {
    long before;
    long after;
    Loop obj = new Loop();

    before = System.currentTimeMillis();
    obj.timeInductionVar(100);
    after = System.currentTimeMillis();
    System.out.println("Loop Induction variable : " + (after - before));
  }
}

