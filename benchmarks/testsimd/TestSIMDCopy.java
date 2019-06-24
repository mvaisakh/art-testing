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

// CHECKSTYLE.OFF: .*
public class TestSIMDCopy {
  static final int INT_ARR_LENGTH = 4 * 1024;
  static final int SHORT_ARR_LENGTH = 8 * 1024;
  static final int BYTE_ARR_LENGTH = 16 * 1024;
  int[] intInput;
  int[] intOutput;
  short[] shortInput;
  short[] shortOutput;
  byte[] byteInput;
  byte[] byteOutput;

  public void setupArrays() {
    intInput = new int[INT_ARR_LENGTH];
    intOutput = new int[INT_ARR_LENGTH];
    shortInput = new short[SHORT_ARR_LENGTH];
    shortOutput = new short[SHORT_ARR_LENGTH];
    byteInput = new byte[BYTE_ARR_LENGTH];
    byteOutput = new byte[BYTE_ARR_LENGTH];

    for (int i = 0; i < INT_ARR_LENGTH; i++) {
       intInput[i] = i + 3;
    }
    for (int i = 0; i < SHORT_ARR_LENGTH; i++) {
       shortInput[i] = (short)(i + 3);
    }
    for (int i = 0; i < BYTE_ARR_LENGTH; i++) {
       byteInput[i] = (byte)(i + 3);
    }
  }

  public static void vectCopyInt(int[] in, int[] out) {
    for (int i = 0; i < INT_ARR_LENGTH; i++) {
      out[i] = in[i];
    }
  }

  public static void vectCopyShort(short[] in, short[] out) {
    for (int i = 0; i < SHORT_ARR_LENGTH; i++) {
      out[i] = in[i];
    }
  }

  public static void vectCopyByte(byte[] in, byte[] out) {
    for (int i = 0; i < BYTE_ARR_LENGTH; i++) {
      out[i] = in[i];
    }
  }

  public void timeVectCopyByte(int iters) {
    for (int i = 0; i < iters; i++) {
      vectCopyByte(byteInput, byteOutput);
    }
  }

  public void timeVectCopyShort(int iters) {
    for (int i = 0; i < iters; i++) {
      vectCopyShort(shortInput, shortOutput);
    }
  }

  public void timeVectCopyInt(int iters) {
    for (int i = 0; i < iters; i++) {
      vectCopyInt(intInput, intOutput);
    }
  }

  public boolean verifySIMDCopy() {
    vectCopyByte(byteInput, byteOutput);
    vectCopyShort(shortInput, shortOutput);
    vectCopyInt(intInput, intOutput);

    int expected = 41965568;
    int found = 0;
    for (int v : intOutput) {
      found += v;
    }
    for (short v : shortOutput) {
      found += v;
    }
    for (byte v : byteOutput) {
      found += v;
    }

    return found == expected;
  }
  // CHECKSTYLE.ON: .*

  public static final int ITER_COUNT = 600;

  public static void main(String[] argv) {
    TestSIMDCopy obj = new TestSIMDCopy();
    obj.setupArrays();

    long before = System.currentTimeMillis();
    obj.timeVectCopyByte(ITER_COUNT);
    long after = System.currentTimeMillis();
    System.out.println("benchmarks/testsimd/TestSIMDCopyByte: " + (after - before));

    before = System.currentTimeMillis();
    obj.timeVectCopyShort(ITER_COUNT);
    after = System.currentTimeMillis();
    System.out.println("benchmarks/testsimd/TestSIMDCopyShort: " + (after - before));

    before = System.currentTimeMillis();
    obj.timeVectCopyInt(ITER_COUNT);
    after = System.currentTimeMillis();
    System.out.println("benchmarks/testsimd/TestSIMDCopyInt: " + (after - before));

    if (!obj.verifySIMDCopy()) {
      System.out.println("ERROR: verifySIMDCopy failed.");
      System.exit(1);
    }
  }
}
