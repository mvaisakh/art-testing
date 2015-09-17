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

import java.lang.System;

public class VolatileTest {

  public final static int ITERATIONS = 1000000;

  public static void main(String[] args) {
    VolatileTest test = new VolatileTest();

    long before = System.currentTimeMillis();
    test.timeLoadStores(ITERATIONS);
    long after = System.currentTimeMillis();
    System.out.println("load-store volatile: " + (after - before));
  }

  public void timeLoadStores(int iters) {
      for (int i = 0; i < iters; i++) {
          loadStores();
      }
  }

  // from the jsr 133 cook book.
  public void loadStores() {
    int i, j;

    i = a;
    j = b;
    i = v;
    // LoadLoad
    j = u;
    // LoadStore
    a = i;
    b = j;
    // StoreStore
    v = i;
    // StoreStore
    u = j;
    // StoreLoad
    i = u;
    // LoadLoad
    // LoadStore
    j = b;
    a = i;
  }

  private int a, b;
  private volatile int u, v;
}

