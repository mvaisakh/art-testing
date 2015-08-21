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

public class FactorialLong {
  public static void main(String[] args) {
    long before = System.currentTimeMillis();
    long result = 0;
    for (int i = 0; i < 10000; ++i)
      result += timeFactorial(i);
    long after = System.currentTimeMillis();
    System.out.println("factorial long: " + (after - before));
  }

  public static long timeFactorial(int n) {
    long result = 1;
    for (int i = 1; i < n; ++i)
      result *= i;
    return result;
  }
}
