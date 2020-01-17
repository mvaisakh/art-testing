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

import java.util.Arrays;

public class Simd64Types {
  // NOTE: in this benchmark, each array element is 8-byte long.
  // The array length is kept relatively small on purpose,
  // to make sure all long[] and double[] arrays in this benchmark can fit into
  // first level caches (e.g. L1 cache) in modern CPUs.
  static final int LENGTH = 3 * 1024;
  long[] la;
  long[] lb;
  long[] lc;
  double[] da;
  double[] db;
  double[] dc;

  // The method is automatically called by the Benchmark framework.
  public void setupArrays() {
    la = new long[LENGTH];
    lb = new long[LENGTH];
    lc = new long[LENGTH];
    da = new double[LENGTH];
    db = new double[LENGTH];
    dc = new double[LENGTH];

    for (int i = 0; i < LENGTH; i++) {
      la[i] = i + 3L;
      lb[i] = i + 2L;
      da[i] = i + 3.0d;
      db[i] = i + 2.0d;
    }
  }

  public static void vectAddLong(long[] la, long[] lb, long[] lc) {
    for (int i = 0; i < LENGTH; i++) {
      lc[i] = la[i] + lb[i];
    }
  }

  public static void vectAddDouble(double[] da, double[] db, double[] dc) {
    for (int i = 0; i < LENGTH; i++) {
      dc[i] = da[i] + db[i];
    }
  }

  public void timeVectAddLong(int iters) {
    for (int i = 0; i < iters; i++) {
      vectAddLong(la, lb, lc);
    }
  }

  public boolean verifyVectAddLong() {
    Arrays.fill(lc, 0);
    timeVectAddLong(1);
    final int hashCode = Arrays.hashCode(lc);
    final int expectedHashCode = 1992969217;
    return hashCode == expectedHashCode;
  }

  public void timeVectAddDouble(int iters) {
    for (int i = 0; i < iters; i++) {
      vectAddDouble(da, db, dc);
    }
  }

  public boolean verifyVectAddDouble() {
    Arrays.fill(dc, 0.0);
    timeVectAddDouble(1);
    final int hashCode = Arrays.hashCode(dc);
    final int expectedHashCode = 1937416705;
    return hashCode == expectedHashCode;
  }

  public int verifySimd64BitTypes() {
    int rc = 0;
    if (!verifyVectAddLong()) {
      ++rc;
    }

    if (!verifyVectAddDouble()) {
      ++rc;
    }

    return rc;
  }

  public static final int ITER_COUNT = 50000;

  public static void main(String[] argv) {
    Simd64Types obj = new Simd64Types();
    obj.setupArrays();

    long before = System.currentTimeMillis();
    obj.timeVectAddLong(ITER_COUNT);
    long after = System.currentTimeMillis();
    System.out.println("benchmarks/micro/Simd64Types.VectAddLong: " + (after - before));

    before = System.currentTimeMillis();
    obj.timeVectAddDouble(ITER_COUNT);
    after = System.currentTimeMillis();
    System.out.println("benchmarks/micro/Simd64Types.VectAddDouble: " + (after - before));
    System.exit(obj.verifySimd64BitTypes());
  }
}
