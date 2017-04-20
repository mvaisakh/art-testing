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
package org.linaro.benchmarks;

import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)

public class TestSIMDLd2St2 {
  static final int VECT_LENGTH = 256 * 1024;
  static final int DIM = 2;
  static final int LENGTH = VECT_LENGTH * DIM;
  static int [] in = new int[LENGTH];
  static int [] out = new int[LENGTH];
  static int [] c = new int[DIM];
  static short [] sin = new short[LENGTH];
  static short [] sout = new short[LENGTH];
  static short [] sc = new short[DIM];

  public static void vect2DInit() {
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

  public static void vect2DAddInt(
          int[] out,
          int[] in,
          int[] c) {
    int c0 = c[0];
    int c1 = c[1];
    for (int i = 0; i < VECT_LENGTH; i++) {
      out[i*DIM] = in[i*DIM] + c0;
      out[i*DIM + 1] = in[i*DIM + 1] + c1;
    }
  }

  public static void vect2DMulInt(
          int[] out,
          int[] in,
          int[] c) {
    int c0 = c[0];
    int c1 = c[1];
    for (int i = 0; i < VECT_LENGTH; i++) {
      out[i*DIM] = in[i*DIM] * c0;
      out[i*DIM + 1] = in[i*DIM + 1] * c1;
    }
  }

  public static void vect2DAddShort(
          short[] out,
          short[] in,
          short[] c) {
    short c0 = c[0];
    short c1 = c[1];
    for (int i = 0; i < VECT_LENGTH; i++) {
      out[i*DIM] = (short)(in[i*DIM] + c0);
      out[i*DIM + 1] = (short)(in[i*DIM + 1] + c1);
    }
  }

  public static void vect2DMulShort(
          short[] out,
          short[] in,
          short[] c) {
    short c0 = c[0];
    short c1 = c[1];
    for (int i = 0; i < VECT_LENGTH; i++) {
      out[i*DIM] = (short)(in[i*DIM] * c0);
      out[i*DIM + 1] = (short)(in[i*DIM + 1] * c1);
    }
  }

  @Setup
  public void setup()
  {
    vect2DInit();
  }

  @Benchmark
  public void testVect2D() {
    vect2DAddInt(out, in, c);
    vect2DMulInt(out, in, c);
    vect2DAddShort(sout, sin, sc);
    vect2DMulShort(sout, sin, sc);
  }

}
