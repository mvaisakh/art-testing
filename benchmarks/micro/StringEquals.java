/*
 * Copyright (C) 2015 Linaro Limited. All rights reserved.
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

/*
 * Description:     Tracks performance of String.equals when working with
 *                  cache-limiting amounts of character data. Some prefetch
 *                  hint investigations have shown a performance gain here.
 * Main Focus:      Comparison of many, very large (re: L1 cache) strings.
 * Secondary Focus:
 *
 */

/*
 * TODO:            Test various sizes of strings.
 * TODO:            Benchmark other cases, like comparing strings of different
 *                  sizes together (probably the most command case), or similar
 *                  strings that have a run of equal characters to begin with.
 */

package benchmarks.micro;

import java.lang.StringBuilder;
import java.util.Random;

public class StringEquals {
  private static Random rnd = new Random(0);
  private static final int NUM_Equals = 1024;
  private static final int STR_Length = 512;
  private static String[] randomStrings = new String[NUM_Equals];
  private static Boolean[] equalsResults = new Boolean[NUM_Equals - 1];

  static {
    generateRandomStrings();
  }

  private static void generateRandomStrings() {
    for (int i = 0; i < NUM_Equals; i++) {
      StringBuilder sb = new StringBuilder();
      for (int j = 0; j < STR_Length; j++) {
        sb.append(Character.valueOf((char)(rnd.nextInt(25) + 65)));
      }
      randomStrings[i] = sb.toString();
    }
  }

  public void timeEquals(int iterations) {
    for (int i = 0; i < iterations; i++) {
      for (int j = 0; j < NUM_Equals - 1; j++) {
        equalsResults[j] = randomStrings[j].equals(randomStrings[j + 1]);
      }
    }
  }

  public static void main(String[] args) {
    StringEquals eq = new StringEquals();
    long before = System.currentTimeMillis();
    eq.timeEquals(1500);
    eq.timeEquals(1500);
    long after = System.currentTimeMillis();
    System.out.println("benchmarks/micro/StringEquals: " + (after - before));
  }
}
