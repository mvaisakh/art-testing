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

public class MatrixDouble {

  public static void main(String[] args) {
    long before = System.currentTimeMillis();
    timeMultiply(5000);
    long after = System.currentTimeMillis();
    System.out.println("matrix double: " + (after - before));
  }

  public static void timeMultiply(int iters) {
    for (int i = 0; i < iters; i++) {
      double[][] test1 = { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } };
      double[][] res = multiply(test1, test1);
    }
  }

  public static double[][] multiply(double[][] A, double[][] B) {
    int mA = A.length;
    int nA = A[0].length;
    int mB = B.length;
    int nB = A[0].length;

    if (nA != mB)
      throw new RuntimeException("Illegal matrix dimensions.");

    double[][] result = new double[mA][nB];

    for (int i = 0; i < mA; i++)
      for (int j = 0; j < nB; j++)
        for (int k = 0; k < nA; k++)
          result[i][j] += (A[i][k] * B[k][j]);

    return result;
  }

}
