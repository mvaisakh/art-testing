/*
 * Copyright (C) 2016 Linaro Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Description:     Tracks performance of accessing array locations using variables and constants
 *                  as indexes.
 * Main Focus:      Memory accesses using arrays.
 *
 */

package benchmarks.micro;

public class ArrayAccess {

  private static final int ITER_COUNT = 1000;

  public static void accessArrayConstants(int[] array) {
    for (int j = 0; j < 100000; j++) {
      array[4]++;
      array[5]++;
    }
  }

  public static void accessArrayVariables(int[] array, int i) {
    for (int j = 0; j < 100000; j++) {
      array[i]++;
      array[i + 1]++;
      array[i + 2]++;
      array[i - 2]++;
      array[i - 1]++;
    }
  }

  public void timeAccessArrayConstants(int iters) {
    int[] array = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    for (int i = 0; i < iters; i++) {
      accessArrayConstants(array);
    }
  }

  public void timeAccessArrayVariables(int iters) {
    int[] array = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    for (int i = 0; i < iters; i++) {
      accessArrayVariables(array, 5);
    }
  }

  public static void main(String[] args) {
    int rc = 0;
    ArrayAccess obj = new ArrayAccess();

    long before = System.currentTimeMillis();
    obj.timeAccessArrayConstants(ITER_COUNT);
    long after = System.currentTimeMillis();
    System.out.println("benchmarks/micro/ArrayAccessConstants: " + (after - before));

    before = System.currentTimeMillis();
    obj.timeAccessArrayVariables(ITER_COUNT);
    after = System.currentTimeMillis();
    System.out.println("benchmarks/micro/ArrayAccessVariables: " + (after - before));

    System.exit(rc);
  }
}
