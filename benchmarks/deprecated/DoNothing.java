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

public class DoNothing {
  public static void main(String args[]) {
    System.out.println("DoNothing: " + 0);
  }

  /*
   * Do nothing in the loop to check the loop overhead.
   */
  public static int timeDoNothing(int iters) {
    for (int i = 0; i < iters; i++) {
    }
    return 0;
  }
}
