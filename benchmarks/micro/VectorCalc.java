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
 * Description:     Some generic vector operations.
 * Main Focus:      Boundary checks in loops
 * Secondary Focus: Array access, load/store.
 *
 */

package benchmarks.micro;

public class VectorCalc {

  public static final int ARRAY_SIZE = 500;
  public static final int COEFFICIENT = 2;
  public static final int MAIN_ITERATIONS = 100000;
  public int array1[] = new int[ARRAY_SIZE];
  public int array2[] = new int[ARRAY_SIZE];
  public int array3[] = new int[ARRAY_SIZE];

  // index : loop induction variable + loop invariable
  public void setArrayValues(int array[], int start, int length, int startValue, int step) {
    for (int i = 0, value = startValue; i < length; ++i, value += step) {
      array[i + start] = value;
    }
  }

  // index : loop induction variable
  // multiple arrays
  public void copyArray(int dst[], int src[], int start, int length) {
    for (int i = start, end = start + length; i < end; ++i) {
      dst[i] = src[i];
    }
  }

  // index : loop induction variable + loop invariable
  // multiple arrays
  public void scaleProduct(int dst[], int src[], int start, int length, int coefficient) {
    for (int i = 0; i < length; ++i) {
      dst[i + start] = coefficient * src[i + start];
    }
  }

  // index-1 : loop induction variable
  // index-2 : loop invariable
  // multiple arrays
  public void dotProduct(int src1[], int src2[], int start, int length, int output[],
                         int outputOffset) {
    for (int i = start, end = start + length; i < end; ++i) {
      output[outputOffset] += src1[i] * src2[i];
    }
  }

  public void timeRun(int iterations) {
    for (int i = 0; i < iterations; i++) {
      setArrayValues(array1, 0, ARRAY_SIZE, 0, 1);
      copyArray(array2, array1, 0, ARRAY_SIZE);
      scaleProduct(array3, array1, 0, ARRAY_SIZE, COEFFICIENT);
      dotProduct(array2, array3, 0, ARRAY_SIZE, array1, 0);
    }
  }

  public boolean verify() {
    return array1[0] == COEFFICIENT * (ARRAY_SIZE - 1) * ARRAY_SIZE * (2 * ARRAY_SIZE - 1) / 6;
  }

  public static void main(String args[]) {
    VectorCalc obj = new VectorCalc();
    long before = System.currentTimeMillis();
    obj.timeRun(MAIN_ITERATIONS);
    long after = System.currentTimeMillis();
    System.out.println("VectorCalculation: " + (after - before));
  }

}
