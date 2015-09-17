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

public class Switch {

  public final static int ITERATIONS_DENSE = 20000000;
  public final static int ITERATIONS_SPARSE = 5000000;
  public final static int ITERATIONS_IFELSE = 5000000;

  public static void main(String[] args) {
    long before = System.currentTimeMillis();
    timeDense(ITERATIONS_DENSE);
    long after = System.currentTimeMillis();
    System.out.println("packed switch: " + (after - before));

    before = System.currentTimeMillis();
    timeSparse(ITERATIONS_SPARSE);
    after = System.currentTimeMillis();
    System.out.println("sparse switch: " + (after - before));

    before = System.currentTimeMillis();
    timeIfelse(ITERATIONS_IFELSE);
    after = System.currentTimeMillis();
    System.out.println("if-else: " + (after - before));
  }

  public static int timeDense(int iters) {
    int sum = 0;

    for (int i = 0; i < iters; i++) {
      switch (i & 0x1f) {
        case 0: sum++; break;
        case 1: sum--; break;
        case 2: sum++; break;
        case 3: sum--; break;
        case 4: sum++; break;
        case 5: sum--; break;
        case 6: sum++; break;
        case 7: sum--; break;
        case 8: sum++; break;
        case 9: sum--; break;
        case 10: sum++; break;
        case 11: sum--; break;
        case 12: sum++; break;
        case 13: sum--; break;
        case 14: sum++; break;
        case 15: sum--; break;
        case 16: sum++; break;
        case 17: sum--; break;
        case 18: sum++; break;
        case 19: sum--; break;
        default: sum += 2;
      }
    }

    return sum;
  }

  public static int timeSparse(int iters) {
    int sum = 0;

    for (int i = 0; i < iters; i++) {
      switch (i & 0x7ff) {
        case 0: sum++; break;
        case 11: sum--; break;
        case 22: sum++; break;
        case 33: sum--; break;
        case 44: sum++; break;
        case 55: sum--; break;
        case 66: sum++; break;
        case 77: sum--; break;
        case 88: sum++; break;
        case 99: sum--; break;
        case 1010: sum++; break;
        case 1111: sum--; break;
        case 1212: sum++; break;
        case 1313: sum--; break;
        case 1414: sum++; break;
        case 1515: sum--; break;
        case 1616: sum++; break;
        case 1717: sum--; break;
        case 1818: sum++; break;
        case 1919: sum--; break;
        default: sum += 2;
      }
    }

    return sum;
  }

  public static int timeIfelse(int iters) {
    int sum = 0;

    for (int i = 0; i < iters; i++) {
      int val = i & 0x7ff;
      if (val == 0) sum++;
      else if (val == 11) sum--;
      else if (val == 22) sum++;
      else if (val == 33) sum--;
      else if (val == 44) sum++;
      else if (val == 55) sum--;
      else if (val == 66) sum++;
      else if (val == 77) sum--;
      else if (val == 88) sum++;
      else if (val == 99) sum--;
      else if (val == 1010) sum++;
      else if (val == 1111) sum--;
      else if (val == 1212) sum++;
      else if (val == 1313) sum--;
      else if (val == 1414) sum++;
      else if (val == 1515) sum--;
      else if (val == 1616) sum++;
      else if (val == 1717) sum--;
      else if (val == 1818) sum++;
      else if (val == 1919) sum--;
      else sum += 2;
    }

    return sum;
  }
}

