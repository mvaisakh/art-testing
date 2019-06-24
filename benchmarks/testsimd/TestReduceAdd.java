/*
 * Copyright (C) 2016 Linaro Limited. All rights received.
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

package benchmarks.testsimd;

public class TestReduceAdd {
  static final int LENGTH = 8 * 1024;
  int[] a;
  int[] b;
  short[] sa;
  short[] sb;

  public void setupArrays() {
    a = new int[LENGTH];
    b = new int[LENGTH];
    sa = new short[LENGTH];
    sb = new short[LENGTH];
    for (int i = 0; i < LENGTH; i++) {
      a[i] = 2;
      b[i] = 1;
      sa[i] = 2;
      sb[i] = 1;
    }
  }

  // In this case, addv sn vm.4s can't be generated in current jdk (OpenJDK9).
  // hotspot version: changeset 12033:d5d5cd1adeaa
  // The same with following test cases.
  public static int reduceAddInt(int[] a) {
    int total = 0;
    for (int i = 0; i < LENGTH; i++) {
      total += a[i];
    }
    return total;
  }

  // In the following two cases, addv sn vm.4s can be generated.
  // The operator can be sub, mul or other operators which can be
  // vectorized easily.
  public static int reduceAddSumofSubInt(int[] a, int[] b) {
    int total = 0;
    for (int i = 0; i < LENGTH; i++) {
      total += (a[i] - b[i]);
    }
    return total;
  }

  public static int reduceAddSumofMulInt(int[] a, int[] b) {
    int total = 0;
    for (int i = 0; i < LENGTH; i++) {
      total += (a[i] * b[i]);
    }
    return total;
  }


  // In the following three cases, addv hn vm.4h can't be generated.
  public static int reduceAddShort(short[] a) {
    int total = 0;
    for (int i = 0; i < LENGTH; i++) {
      total += a[i];
    }
    return total;
  }

  public static int reduceAddSumofSubShort(short[] a, short[] b) {
    int total = 0;
    for (int i = 0; i < LENGTH; i++) {
      total += (a[i] - b[i]);
    }
    return total;
  }

  public static int reduceAddSumofMulShort(short[] a, short[] b) {
    int total = 0;
    for (int i = 0; i < LENGTH; i++) {
      total += (a[i] * b[i]);
    }
    return total;
  }

  int sum;

  public void timeReduceAddInt(int iters) {
    int sum = 0;
    for (int i = 0; i < iters; i++) {
      sum = reduceAddInt(a);
      sum += reduceAddSumofSubInt(a, b);
      sum += reduceAddSumofMulInt(a, b);
    }
    this.sum = sum;
  }

  public void timeReduceAddShort(int iters) {
    int sum = 0;
    for (int i = 0; i < iters; i++) {
      sum = reduceAddShort(sa);
      sum += reduceAddSumofSubShort(sa, sb);
      sum += reduceAddSumofMulShort(sa, sb);
    }
    this.sum = sum;
  }

  public boolean verifyReduceAdd() {
    int expected = 81920;
    int found = 0;
    found  = reduceAddInt(a);
    found += reduceAddSumofSubInt(a, b);
    found += reduceAddSumofMulInt(a, b);
    found += reduceAddShort(sa);
    found += reduceAddSumofSubShort(sa, sb);
    found += reduceAddSumofMulShort(sa, sb);

    return found == expected;
  }

  public static final int ITER_COUNT = 150;

  public static void main(String[] argv) {
    TestReduceAdd obj = new TestReduceAdd();
    obj.setupArrays();

    long before = System.currentTimeMillis();
    obj.timeReduceAddInt(ITER_COUNT);
    long after = System.currentTimeMillis();
    System.out.println("benchmarks/testsimd/TestReduceAddInt: " + (after - before));

    before = System.currentTimeMillis();
    obj.timeReduceAddShort(ITER_COUNT);
    after = System.currentTimeMillis();
    System.out.println("benchmarks/testsimd/TestReduceAddShort: " + (after - before));

    if (!obj.verifyReduceAdd()) {
      System.out.println("ERROR: verifyReduceAdd failed.");
      System.exit(1);
    }
  }
}
