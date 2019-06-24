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

import java.util.Arrays;

// CHECKSTYLE.OFF: .*
public class TestSIMDMul {
  static final int BYTE_ARR_LENGTH = 16 * 1024;
  static final int INT_ARR_LENGTH = 4 * 1024;
  static final int SHORT_ARR_LENGTH = 8 * 1024;
  private int[] intInputA;
  private int[] intInputB;
  private int[] intOutput;
  private short[] shortInputA;
  private short[] shortInputB;
  private short[] shortOutput;
  private byte[] byteInputA;
  private byte[] byteInputB;
  private byte[] byteOutput;

  public void setupArrays() {
    intInputA = new int[INT_ARR_LENGTH];
    intInputB = new int[INT_ARR_LENGTH];
    intOutput = new int[INT_ARR_LENGTH];
    shortInputA = new short[SHORT_ARR_LENGTH];
    shortInputB = new short[SHORT_ARR_LENGTH];
    shortOutput = new short[SHORT_ARR_LENGTH];
    byteInputA = new byte[BYTE_ARR_LENGTH];
    byteInputB = new byte[BYTE_ARR_LENGTH];
    byteOutput = new byte[BYTE_ARR_LENGTH];
    for (int i = 0; i < INT_ARR_LENGTH; i++) {
       intInputA[i] = i + 3;
       intInputB[i] = i + 2;
    }

    for (int i = 0; i < SHORT_ARR_LENGTH; i++) {
       shortInputA[i] = (short)(i + 3);
       shortInputB[i] = (short)(i + 2);
    }

    for (int i = 0; i < BYTE_ARR_LENGTH; i++) {
       byteInputA[i] = (byte)(i + 3);
       byteInputB[i] = (byte)(i + 2);
    }
  }

  public void vectMulInt(int[] inA, int[] inB, int[] out) {
    for (int i = 0; i < INT_ARR_LENGTH; i++) {
      out[i] = inA[i] * inB[i];
    }
  }

  public void vectMulShort(short[] inA, short[] inB, short[] out) {
    for (int i = 0; i < SHORT_ARR_LENGTH; i++) {
      out[i] = (short)(inA[i] * inB[i]);
    }
  }

  public void vectMulByte(byte[] inA, byte[] inB, byte[] out) {
    for (int i = 0; i < BYTE_ARR_LENGTH; i++) {
      out[i] = (byte)(inA[i] * inB[i]);
    }
  }

  public void timeVectMulByte(int iters) {
    for (int i = 0; i < iters; i++) {
      vectMulByte(byteInputA, byteInputB, byteOutput);
    }
  }

  public boolean verifyVectMulByte() {
    Arrays.fill(byteOutput, (byte)0);
    timeVectMulByte(1);
    final int hashCode = Arrays.hashCode(byteOutput);
    final int expectedHashCode = -230948863;
    return hashCode == expectedHashCode;
  }

  public void timeVectMulShort(int iters) {
    for (int i = 0; i < iters; i++) {
      vectMulShort(shortInputA, shortInputB, shortOutput);
    }
  }

  public boolean verifyVectMulShort() {
    Arrays.fill(shortOutput, (short)0);
    timeVectMulShort(1);
    final int hashCode = Arrays.hashCode(shortOutput);
    final int expectedHashCode = -1719910399;
    return hashCode == expectedHashCode;
  }

  public void timeVectMulInt(int iters) {
    for (int i = 0; i < iters; i++) {
      vectMulInt(intInputA, intInputB, intOutput);
    }
  }

  public boolean verifyVectMulInt() {
    Arrays.fill(intOutput, 0);
    timeVectMulInt(1);
    final int hashCode = Arrays.hashCode(intOutput);
    final int expectedHashCode = 1287856129;
    return hashCode == expectedHashCode;
  }

  // CHECKSTYLE.ON: .*

  public static final int ITER_COUNT = 300;

  public static void main(String[] argv) {
    TestSIMDMul obj = new TestSIMDMul();
    obj.setupArrays();

    long before = System.currentTimeMillis();
    obj.timeVectMulByte(ITER_COUNT);
    long after = System.currentTimeMillis();
    System.out.println("benchmarks/testsimd/TestSIMDMulByte: " + (after - before));

    before = System.currentTimeMillis();
    obj.timeVectMulShort(ITER_COUNT);
    after = System.currentTimeMillis();
    System.out.println("benchmarks/testsimd/TestSIMDMulShort: " + (after - before));

    before = System.currentTimeMillis();
    obj.timeVectMulInt(ITER_COUNT);
    after = System.currentTimeMillis();
    System.out.println("benchmarks/testsimd/TestSIMDMulInt: " + (after - before));

    int rc = 0;
    if (!obj.verifyVectMulByte()) {
      rc++;
    }

    if (!obj.verifyVectMulShort()) {
      rc++;
    }

    if (!obj.verifyVectMulInt()) {
      rc++;
    }

    if (rc != 0) {
      System.out.println("ERROR: verification failed.");
      System.exit(rc);
    }
  }
}
