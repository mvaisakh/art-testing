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
public class TestSIMDLd4St4 {
  static final int VECT_LENGTH = 4 * 1024;
  static final int DIM = 4;
  static final int LENGTH = VECT_LENGTH * DIM;
  int[] in;
  int[] out;
  int[] c;
  short[] sin;
  short[] sout;
  short[] sc;

  public void setupArrays() {
    in = new int[LENGTH];
    out = new int[LENGTH];
    c = new int[DIM];
    sin = new short[LENGTH];
    sout = new short[LENGTH];
    sc = new short[DIM];
    for (int i = 0; i < LENGTH; i++) {
      in[i] = i + 3;
      sin[i] = (short)(i + 3);
    }
    for (int i = 0; i < DIM; i++) {
      c[i] = i + 1;
      sc[i] = (short)(i + 1);
    }
  }

  public static void vect4DAddInt(
          int[] out,
          int[] in,
          int[] c) {
    int c0 = c[0];
    int c1 = c[1];
    int c2 = c[2];
    int c3 = c[3];
    for (int i = 0; i < VECT_LENGTH; i++) {
      out[i*DIM] = in[i*DIM] + c0;
      out[i*DIM + 1] = in[i*DIM + 1] + c1;
      out[i*DIM + 2] = in[i*DIM + 2] + c2;
      out[i*DIM + 3] = in[i*DIM + 3] + c3;
    }
  }

  public static void vect4DMulInt(
          int[] out,
          int[] in,
          int[] c) {
    int c0 = c[0];
    int c1 = c[1];
    int c2 = c[2];
    int c3 = c[3];
    for (int i = 0; i < VECT_LENGTH; i++) {
      out[i*DIM] = in[i*DIM] * c0;
      out[i*DIM + 1] = in[i*DIM + 1] * c1;
      out[i*DIM + 2] = in[i*DIM + 2] * c2;
      out[i*DIM + 3] = in[i*DIM + 3] * c3;
    }
  }

  public static void vect4DAddShort(
          short[] out,
          short[] in,
          short[] c) {
    short c0 = c[0];
    short c1 = c[1];
    short c2 = c[2];
    short c3 = c[3];
    for (int i = 0; i < VECT_LENGTH; i++) {
      out[i*DIM] = (short)(in[i*DIM] + c0);
      out[i*DIM + 1] = (short)(in[i*DIM + 1] + c1);
      out[i*DIM + 2] = (short)(in[i*DIM + 2] + c2);
      out[i*DIM + 3] = (short)(in[i*DIM + 3] + c3);
    }
  }

  public static void vect4DMulShort(
          short[] out,
          short[] in,
          short[] c) {
    short c0 = c[0];
    short c1 = c[1];
    short c2 = c[2];
    short c3 = c[3];
    for (int i = 0; i < VECT_LENGTH; i++) {
      out[i*DIM] = (short)(in[i*DIM] * c0);
      out[i*DIM + 1] = (short)(in[i*DIM + 1] * c1);
      out[i*DIM + 2] = (short)(in[i*DIM + 2] * c2);
      out[i*DIM + 3] = (short)(in[i*DIM + 3] * c3);
    }
  }

  public void timeVect4D(int iters) {
    for (int i = 0; i < iters; i++) {
      vect4DAddInt(out, in, c);
      vect4DMulInt(out, in, c);
      vect4DAddShort(sout, sin, sc);
      vect4DMulShort(sout, sin, sc);
    }
  }

  public boolean verifySIMDLd4St4() {
    vect4DAddInt(out, in, c);
    vect4DMulInt(out, in, c);
    vect4DAddShort(sout, sin, sc);
    vect4DMulShort(sout, sin, sc);

    int expected = 447463424;
    int found = 0;
    for (int i = 0; i < LENGTH; i++) {
      found += out[i] + sout[i];
    }

    return expected == found;
  }
  // CHECKSTYLE.ON: .*

  public static final int ITER_COUNT = 40;

  public static void main(String[] argv) {
    TestSIMDLd4St4 obj = new TestSIMDLd4St4();
    obj.setupArrays();

    long before = System.currentTimeMillis();
    obj.timeVect4D(ITER_COUNT);
    long after = System.currentTimeMillis();
    System.out.println("benchmarks/testsimd/TestSIMDLd4St4: " + (after - before));

    if (!obj.verifySIMDLd4St4()) {
      System.out.println("ERROR: verifySIMDLd4St4 failed.");
      System.exit(1);
    }
  }
}
