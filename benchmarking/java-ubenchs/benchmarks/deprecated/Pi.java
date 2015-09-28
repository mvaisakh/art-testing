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

// This benchmark generates PI using Leibniz formula.
public class Pi {
  public double piValue;

  public double timeGeneratePi(int terms) {
    this.piValue = 0.0D;
    double sign = 1.0D;
    for (int i = 0; i < terms; ++i) {
      piValue += sign * (1.0D / (2.0D * i + 1.0D));
      sign *= -1;
    }
    return this.piValue;
  }

  public static void main(String args[]) {
    Pi obj = new Pi();
    long before = System.currentTimeMillis();
    obj.timeGeneratePi(10000);
    long after = System.currentTimeMillis();
    System.out.println("Generate Pi: " + (after - before));
  }
}

