/*
 * Copyright (C) 2016 Linaro Limited. All rights reserved.
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
 * Description:     Exceptions code utilization.
 * Main Focus:
 *
 */

package benchmarks.micro;

public class Exceptions {

  private int[] smallArray = new int[1];
  private static final int ARITHM_DOUBLE_ITERS = 20000;

  /**
   * Three following examples do the same arithmetics on double, but
   *   - arithmDouble:               just does calculations
   *   - arirhmDoubleException:      does calculations but has a try-catch which never fires.
   *   - arirhmDoubleExceptionFires: does calculations but has a try-catch which always fires.
   */
  private int arithmDoubleExceptionFires(int n) {
    double x = 1.0 * (double)n;
    double y = 2.0 * (double)n;
    double z = 3.0 * (double)n;
    double expr = 0.0;
    int a = 0;

    for (int i = 0; i < ARITHM_DOUBLE_ITERS; i++) {
      expr += x + y + z * x + y * y + x * z;
      try {
        a = smallArray[1];
      } catch (Exception e) {
      }
    }

    return (int)expr + a + (int)x + (int)y + (int)z;
  }

  private int arithmDoubleException(int n) {
    double x = 1.0 * (double)n;
    double y = 2.0 * (double)n;
    double z = 3.0 * (double)n;
    double expr = 0.0;
    int a = 0;

    for (int i = 0; i < ARITHM_DOUBLE_ITERS; i++) {
      expr += x + y + z * x + y * y + x * z;
      try {
        a = smallArray[0];
      } catch (Exception e) {
      }
    }

    return (int)expr + a + (int)x + (int)y + (int)z;
  }

  private int arithmDouble(int n) {
    double x = 1.0 * (double)n;
    double y = 2.0 * (double)n;
    double z = 3.0 * (double)n;
    double expr = 0.0;
    int a = 0;

    for (int i = 0; i < ARITHM_DOUBLE_ITERS; i++) {
      expr += x + y + z * x + y * y + x * z;
      a = smallArray[0];
    }

    return (int)expr + a + (int)x + (int)y + (int)z;
  }

  public void timeArithmDoubleException(int iterations) {
    for (int i = 0; i < iterations; i++) {
      arithmDoubleException(i);
    }
  }

  public void timeArithmDoubleExceptionFires(int iterations) {
    for (int i = 0; i < iterations; i++) {
      arithmDoubleExceptionFires(i);
    }
  }

  public void timeArithmDouble(int iterations) {
    for (int i = 0; i < iterations; i++) {
      arithmDouble(i);
    }
  }

  public boolean verify() {
    final int expected = 20600060;
    return (arithmDouble(10) == expected) &&
           (arithmDoubleException(10) == expected) &&
           (arithmDoubleExceptionFires(10) == expected);
  }

  private static final int TIME_FUNCTIONS_ITERS = 1000;

  public static void main(String[] args) {
    int rc = 0;
    Exceptions obj = new Exceptions();
    long before = System.currentTimeMillis();
    obj.timeArithmDoubleExceptionFires(10);
    long after = System.currentTimeMillis();
    System.out.println("benchmarks/micro/Exceptions.arithmDoubleExceptionFires: "
                       + (after - before));

    before = System.currentTimeMillis();
    obj.timeArithmDoubleException(TIME_FUNCTIONS_ITERS);
    after = System.currentTimeMillis();
    System.out.println("benchmarks/micro/Exceptions.arithmDoubleException: " + (after - before));

    before = System.currentTimeMillis();
    obj.timeArithmDouble(TIME_FUNCTIONS_ITERS);
    after = System.currentTimeMillis();
    System.out.println("benchmarks/micro/Exceptions.arithmDouble: " + (after - before));

    if (!obj.verify()) {
      rc++;
    }

    System.exit(rc);
  }
}
