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
public class TestSIMDAdd {
  static final int INT_ARR_LENGTH = 4 * 1024;
  static final int SHORT_ARR_LENGTH = 8 * 1024;
  static final int BYTE_ARR_LENGTH = 16 * 1024;
  int[] intInputA;
  int[] intInputB;
  int[] intOutput;
  short[] shortInputA;
  short[] shortInputB;
  short[] shortOutput;
  byte[] byteInputA;
  byte[] byteInputB;
  byte[] byteOutput;

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

  public static void vectAddInt(int[] inA, int[] inB, int[] out) {
    for (int i = 0; i < INT_ARR_LENGTH; i++) {
      out[i] = inA[i] + inB[i];
    }
  }

  public static void vectAddShort(short[] inA, short[] inB, short[] out) {
    for (int i = 0; i < SHORT_ARR_LENGTH; i++) {
      out[i] = (short)(inA[i] + inB[i]);
    }
  }

  public static void vectAddByte(byte[] inA, byte[] inB, byte[] out) {
    for (int i = 0; i < BYTE_ARR_LENGTH; i++) {
      out[i] = (byte)(inA[i] + inB[i]);
    }
  }

  public void timeVectAddByte(int iters) {
    for (int i = 0; i < iters; i++) {
      vectAddByte(byteInputA, byteInputB, byteOutput);
    }
  }

  public boolean verifyVectAddByte() {
    Arrays.fill(byteOutput, (byte)0);
    timeVectAddByte(1);
    final int hashCode = Arrays.hashCode(byteOutput);
    final int expectedHashCode = 2038185985;
    return hashCode == expectedHashCode;
  }

  public void timeVectAddShort(int iters) {
    for (int i = 0; i < iters; i++) {
      vectAddShort(shortInputA, shortInputB, shortOutput);
    }
  }

  public boolean verifyVectAddShort() {
    Arrays.fill(shortOutput, (short)0);
    timeVectAddShort(1);
    final int hashCode = Arrays.hashCode(shortOutput);
    final int expectedHashCode = 1019617281;
    return hashCode == expectedHashCode;
  }

  public void timeVectAddInt(int iters) {
    for (int i = 0; i < iters; i++) {
      vectAddInt(intInputA, intInputB, intOutput);
    }
  }

  public boolean verifyVectAddInt() {
    Arrays.fill(intOutput, 0);
    timeVectAddInt(1);
    final int hashCode = Arrays.hashCode(intOutput);
    final int expectedHashCode = 509808641;
    return hashCode == expectedHashCode;
  }

  public int verifySIMDAdd() {
    int rc = 0;
    if (!verifyVectAddByte()) {
      ++rc;
    }

    if (!verifyVectAddShort()) {
      ++rc;
    }

    if (!verifyVectAddInt()) {
      ++rc;
    }

    return rc;
  }
  // CHECKSTYLE.ON: .*

  public static final int ITER_COUNT = 300;

  public static void main(String[] argv) {
    TestSIMDAdd obj = new TestSIMDAdd();
    obj.setupArrays();

    long before = System.currentTimeMillis();
    obj.timeVectAddByte(ITER_COUNT);
    long after = System.currentTimeMillis();
    System.out.println("benchmarks/testsimd/TestSIMDAddByte: " + (after - before));

    before = System.currentTimeMillis();
    obj.timeVectAddShort(ITER_COUNT);
    after = System.currentTimeMillis();
    System.out.println("benchmarks/testsimd/TestSIMDAddShort: " + (after - before));

    before = System.currentTimeMillis();
    obj.timeVectAddInt(ITER_COUNT);
    after = System.currentTimeMillis();
    System.out.println("benchmarks/testsimd/TestSIMDAddInt: " + (after - before));

    int rc = obj.verifySIMDAdd();
    if (rc != 0) {
      System.out.println("ERROR: verifySIMDAdd failed.");
      System.exit(rc);
    }
  }
}
