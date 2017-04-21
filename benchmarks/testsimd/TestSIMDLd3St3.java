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
public class TestSIMDLd3St3 {
  static final int VECT_LENGTH = 256 * 1024;
  static final int DIM = 3;
  static final int LENGTH = VECT_LENGTH * DIM;
  static int [] in = new int[LENGTH];
  static int [] out = new int[LENGTH];
  static int [] c = new int[DIM];
  static short [] sin = new short[LENGTH];
  static short [] sout = new short[LENGTH];
  static short [] sc = new short[DIM];

  public static void vect3DInit() {
    int i;
    for (i = 0; i < LENGTH; i++) {
      in[i] = i + 3;
      sin[i] = (short)(i + 3);
    }
    for (i = 0; i < DIM; i++) {
      c[i] = i + 1;
      sc[i] = (short)(i + 1);
    }
  }

  public static void vect3DAddInt(
          int[] out,
          int[] in,
          int[] c) {
    int c0 = c[0];
    int c1 = c[1];
    int c2 = c[2];
    for (int i = 0; i < VECT_LENGTH; i++) {
      out[i*DIM] = in[i*DIM] + c0;
      out[i*DIM + 1] = in[i*DIM + 1] + c1;
      out[i*DIM + 2] = in[i*DIM + 2] + c2;
    }
  }

  public static void vect3DMulInt(
          int[] out,
          int[] in,
          int[] c) {
    int c0 = c[0];
    int c1 = c[1];
    int c2 = c[2];
    for (int i = 0; i < VECT_LENGTH; i++) {
      out[i*DIM] = in[i*DIM] * c0;
      out[i*DIM + 1] = in[i*DIM + 1] * c1;
      out[i*DIM + 2] = in[i*DIM + 2] * c2;
    }
  }

  public static void vect3DAddShort(
          short[] out,
          short[] in,
          short[] c) {
    short c0 = c[0];
    short c1 = c[1];
    short c2 = c[2];
    for (int i = 0; i < VECT_LENGTH; i++) {
      out[i*DIM] = (short)(in[i*DIM] + c0);
      out[i*DIM + 1] = (short)(in[i*DIM + 1] + c1);
      out[i*DIM + 2] = (short)(in[i*DIM + 2] + c2);
    }
  }

  public static void vect3DMulShort(
          short[] out,
          short[] in,
          short[] c) {
    short c0 = c[0];
    short c1 = c[1];
    short c2 = c[2];
    for (int i = 0; i < VECT_LENGTH; i++) {
      out[i*DIM] = (short)(in[i*DIM] * c0);
      out[i*DIM + 1] = (short)(in[i*DIM + 1] * c1);
      out[i*DIM + 2] = (short)(in[i*DIM + 2] * c2);
    }
  }

  public void timeVect3D(int iters) {
    vect3DInit();
    for (int i = 0; i < iters; i++) {
      vect3DAddInt(out, in, c);
      vect3DMulInt(out, in, c);
      vect3DAddShort(sout, sin, sc);
      vect3DMulShort(sout, sin, sc);
    }
  }

  public boolean verifySIMDLd3St3() {
    vect3DInit();
    vect3DAddInt(out, in, c);
    vect3DMulInt(out, in, c);
    vect3DAddShort(sout, sin, sc);
    vect3DMulShort(sout, sin, sc);

    int expected = 786432;
    int found = 0;
    for (int i = 0; i < LENGTH; i++) {
      found += in[i] + out[i] + sin[i] + sout[i];
    }
    for (int i = 0; i < DIM; i++) {
      found += c[i] + sc[i];
    }

    return true;
  }
  // CHECKSTYLE.ON: .*

  public static final int ITER_COUNT = 40;

  public static void main(String[] argv) {
    int rc = 0;
    TestSIMDLd3St3 obj = new TestSIMDLd3St3();

    long before = System.currentTimeMillis();
    obj.timeVect3D(ITER_COUNT);
    long after = System.currentTimeMillis();
    System.out.println("benchmarks/testsimd/TestSIMDLd3St3: " + (after - before));

    if (!obj.verifySIMDLd3St3()) {
      rc++;
    }
    System.exit(rc);
  }

}
