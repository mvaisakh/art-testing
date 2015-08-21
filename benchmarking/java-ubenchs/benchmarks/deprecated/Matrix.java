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

public class Matrix {

    public static void main(String[] args) {
        Matrix m = new Matrix();
        long before = System.currentTimeMillis();
        m.timeMatrixMultiply(50000);
        long after = System.currentTimeMillis();
        System.out.println("matrix int: " + (after - before));
    }

    public int timeMatrixMultiply(int iters) {
        int result = 0;
        for (int i = 0; i < iters; ++i) {
            int[][] test1 = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
            int[][] res = multiply(test1, test1);
            result += res[0][0];
        }
        return result;
    }

    public static int[][] multiply(int[][] A, int[][] B) {
        int mA = A.length;
        int nA = A[0].length;
        int mB = B.length;
        int nB = A[0].length;

        if (nA != mB)
            throw new RuntimeException("Illegal matrix dimensions.");

        int[][] result = new int[mA][nB];

        for (int i = 0; i < mA; i++)
            for (int j = 0; j < nB; j++)
                for (int k = 0; k < nA; k++)
                    result[i][j] += (A[i][k] * B[k][j]);

        return result;
    }
}
