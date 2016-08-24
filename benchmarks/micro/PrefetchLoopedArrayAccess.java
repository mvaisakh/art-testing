/*
 * Copyright (C) 2015 Linaro Limited. All rights reserved.
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
 * Description:     Tracks performance of looped array access to Java Objects.
 *                  BitSet is an arbitrary choice. 1024 pool and update list
 *                  sizes showed benefits when prefetching 8 references ahead
 *                  during looped array access. Other benchmarks for different
 *                  and mixed Object sizes would be beneficial.
 * Main Focus:      Looped array access to semi-random memory access patterns.
 * Secondary Focus:
 *
 */

package benchmarks.micro;

import java.util.BitSet;
import java.util.Random;

public class PrefetchLoopedArrayAccess {

  private static final Random rnd = new Random(0);
  private static final int POOL_Size = 1024;
  private static final int UPDATE_Size = 1024;

  private BitSet[] bits;
  private BitSet[] updateList;

  {
    initBitSets();
    initUpdateList();
  }

  private void initUpdateList() {
    updateList = new BitSet[UPDATE_Size];
    for (int i = 0; i < UPDATE_Size; i++) {
      updateList[i] = bits[rnd.nextInt(POOL_Size)];
    }
  }

  private void initBitSets() {
    bits = new BitSet[POOL_Size];
    for (int i = 0; i < POOL_Size; i++) {
      bits[i] = new BitSet();
    }
  }

  private void updateBitSets() {
    for (int i = 0; i < UPDATE_Size; i++) {
      updateList[i].set(7);
    }
  }

  public void timeRun(int iterations) {
    for (int i = 0; i < iterations; i++) {
      updateBitSets();
    }
  }

  public static void main(String[] args) {
    PrefetchLoopedArrayAccess obj = new PrefetchLoopedArrayAccess();
    long before = System.currentTimeMillis();
    obj.timeRun(22000);
    long after = System.currentTimeMillis();
    System.out.println("benchmarks/micro/PrefetchLoopedArrayAccess: " + (after - before));
  }

}
