/*
 * Copyright (C) 2017 Linaro Limited. All rights received.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/*
 * Description: Test Array add operations which can be vectorized by
 *              compiler's auto vectorization optimisation.
 * Main Focus: Performance of vectorized add operations on long[] and double[] arrays.
 */

package benchmarks.micro;

public class Simd64Types {
  // NOTE: in this benchmark, each array element is 8-byte long.
  // The array length is kept relatively small on purpose,
  // to make sure all long[] and double[] arrays in this benchmark can fit into
  // most last level caches (e.g. L2 cache) in modern CPUs.
  static final int LENGTH = 4 * 1024;
  static long [] la = new long[LENGTH];
  static long [] lb = new long[LENGTH];
  static long [] lc = new long[LENGTH];
  static double [] da = new double[LENGTH];
  static double [] db = new double[LENGTH];
  static double [] dc = new double[LENGTH];

  public static void init() {
    for (int i = 0; i < LENGTH; i++) {
      la[i] = i + 3L;
      lb[i] = i + 2L;
      lc[i] = i + 1L;
      da[i] = i + 3.0d;
      db[i] = i + 2.0d;
      dc[i] = i + 1.0d;
    }
  }

  public static void vectAddLong() {
    for (int i = 0; i < LENGTH; i++) {
      lc[i] = la[i] + lb[i];
    }
  }

  public static void vectAddDouble() {
    for (int i = 0; i < LENGTH; i++) {
      dc[i] = da[i] + db[i];
    }
  }

  public void timeVectAddLong(int iters) {
    init();
    for (int i = 0; i < iters; i++) {
      vectAddLong();
    }
  }

  public void timeVectAddDouble(int iters) {
    init();
    for (int i = 0; i < iters; i++) {
      vectAddDouble();
    }
  }

  public boolean verifySimd64BitTypes() {
    init();
    vectAddLong();
    vectAddDouble();

    long expected1 = 16793600L;
    long found1 = 0L;
    double expected2 = 16793600D;
    double found2 = 0D;

    for (int i = 0; i < LENGTH; i++) {
      found1 += lc[i];
      found2 += dc[i];
    }

    if (found1 != expected1) {
      System.out.println("ERROR: Expected " + expected1 + " but found " + found1);
      return false;
    }
    if (found2 != expected2) {
      System.out.println("ERROR: Expected " + expected2 + " but found " + found2);
      return false;
    }

    return true;
  }

  public static final int ITER_COUNT = 50000;

  public static void main(String[] argv) {
    int rc = 0;
    Simd64Types obj = new Simd64Types();

    long before = System.currentTimeMillis();
    obj.timeVectAddLong(ITER_COUNT);
    long after = System.currentTimeMillis();
    System.out.println("benchmarks/micro/Simd64Types.VectAddLong: " + (after - before));

    before = System.currentTimeMillis();
    obj.timeVectAddDouble(ITER_COUNT);
    after = System.currentTimeMillis();
    System.out.println("benchmarks/micro/Simd64Types.VectAddDouble: " + (after - before));

    if (!obj.verifySimd64BitTypes()) {
      rc++;
    }
    System.exit(rc);
  }
}
