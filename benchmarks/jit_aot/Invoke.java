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
 * This benchmark is inspired by benchmarks/caffeinemark.
 * Original benchmark implements a recursion method which calls itself with invoke-virtual.
 *
 * This behavior can be different on JIT and AOT mode, because:
 * - JIT mode can optimize invoke-virtual with inline cache mechanism.
 * - AOT mode has no such optimization.
 *
 * This benchmark exposes such difference between ART JIT and AOT mode.
 */

package benchmarks.jit_aot;

public class Invoke {

  public int recursionInvokeVirtual(int i) {
    if (i == 0) {
      return 0;
    } else {
      return i + recursionInvokeVirtual(i - 1);
    }
  }

  public static int recursionInvokeStatic(int i) {
    if (i == 0) {
      return 0;
    } else {
      return i + recursionInvokeStatic(i - 1);
    }
  }

  public final int recursionInvokeFinal(int i) {
    if (i == 0) {
      return 0;
    } else {
      return i + recursionInvokeFinal(i - 1);
    }
  }

  private int recursionInvokePrivate(int i) {
    if (i == 0) {
      return 0;
    } else {
      return i + recursionInvokePrivate(i - 1);
    }
  }

  private static final int recursion_depth = 1000;

  public void timeRecursionInvokeVirtual(int iters) {
    for (int i = 0; i < iters; i++) {
      recursionInvokeVirtual(recursion_depth);
    }
  }

  public void timeRecursionInvokeStatic(int iters) {
    for (int i = 0; i < iters; i++) {
      recursionInvokeStatic(recursion_depth);
    }
  }

  public void timeRecursionInvokeFinal(int iters) {
    for (int i = 0; i < iters; i++) {
      recursionInvokeFinal(recursion_depth);
    }
  }

  public void timeRecursionInvokePrivate(int iters) {
    for (int i = 0; i < iters; i++) {
      recursionInvokePrivate(recursion_depth);
    }
  }

  public boolean verify() {
    return true;
  }

  public static void main(String[] args) {
    int rc = 0;
    Invoke obj = new Invoke();

    final long before = System.currentTimeMillis();
    obj.timeRecursionInvokeVirtual(1000);
    obj.timeRecursionInvokeStatic(1000);
    obj.timeRecursionInvokeFinal(1000);
    obj.timeRecursionInvokePrivate(1000);
    final long after = System.currentTimeMillis();
    if (!obj.verify()) {
      rc++;
    }
    System.out.println("benchmarks/jit_aot/Invoke: " + (after - before));

    System.exit(rc);
  }
}
