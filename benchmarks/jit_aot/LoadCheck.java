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
 * This benchmark is inspired by benchmarksgame/fastaredux.java.
 * Original benchmark implements a Random class which is a random double generator,
 * and is heavily used by another class in its inner calculation.
 *
 * This behavior can be very unfriendly to ART AOT because:
 * Random is a user defined class. Currently in AOT mode,
 * ART cannot assume this class is always loaded,
 * thus ART AOT compiler has to generate LoadClass check before invoking Random.next() method.
 * Since Random.next() is called inside a loop of a hot function addLine(),
 * the LoadClass check overhead causes this benchmark runs twice slower
 * in AOT mode compared to JIT mode.
 *
 * In interpreter + JIT mode, JIT compiler can assume that a user defined class is always loaded
 * during previous interpretation stage, thus it avoid generating LoadClass check.
 *
 * This benchmark exposes such difference between ART JIT and AOT mode.
 */

package benchmarks.jit_aot;

public class LoadCheck {
  static final class Random {
    static final int IM = 139968;
    static final int IA = 3877;
    static final int IC = 29573;
    static final double LOOKUP_SCALE = 4 * 1024 - 1;
    static final double SCALE = LOOKUP_SCALE / IM;
    static int last = 42;

    static double next() {
      return SCALE * (last = (last * IA + IC) % IM);
    }
  }

  static final int IM = 139968;
  static final int IA = 3877;
  static final int IC = 29573;
  static final double LOOKUP_SCALE = 4 * 1024 - 1;
  static final double SCALE = LOOKUP_SCALE / IM;
  static int last = 42;

  public double sum;

  static double nextRandDouble() {
    return SCALE * (last = (last * IA + IC) % IM);
  }

  public void randomSumInvokeStaticMethod(int val) {
    sum = (double)val;
    for (int i = 0; i < 10000; i++) {
      sum += nextRandDouble();
    }
  }

  public void randomSumInvokeUserClass(int val) {
    sum = (double)val;
    for (int i = 0; i < 10000; i++) {
      sum += Random.next();
    }
  }

  private static final int loop_size = 10000;

  public void timeRandomSumInvokeStaticMethod(int iters) {
    for (int i = 0; i < iters; i++) {
      randomSumInvokeStaticMethod(loop_size);
    }
  }

  public void timeRandomSumInvokeUserClass(int iters) {
    for (int i = 0; i < iters; i++) {
      randomSumInvokeUserClass(loop_size);
    }
  }

  public boolean verify() {
    return true;
  }

  public static void main(String[] args) {
    int rc = 0;
    LoadCheck obj = new LoadCheck();

    final long before = System.currentTimeMillis();
    obj.timeRandomSumInvokeStaticMethod(10000);
    obj.timeRandomSumInvokeUserClass(10000);
    final long after = System.currentTimeMillis();
    if (!obj.verify()) {
      rc++;
    }
    System.out.println("benchmarks/jit_aot/LoadCheck: " + (after - before));

    System.exit(rc);
  }
}
