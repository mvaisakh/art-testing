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

public class TestSIMDCopy {
  static final int LENGTH = 1024 * 256;
  static int [] a = new int[LENGTH];
  static int [] c = new int[LENGTH];
  static short [] sa = new short[LENGTH];
  static short [] sc = new short[LENGTH];
  static byte [] ba = new byte[LENGTH];
  static byte [] bc = new byte[LENGTH];

  public static void TestSIMDCopyInit() {
    for (int i = 0; i < LENGTH; i++) {
       a[i] = i + 3;
       c[i] = i + 1;
       sa[i] = (short)(i + 3);
       sc[i] = (short)(i + 1);
       ba[i] = (byte)(i + 3);
       bc[i] = (byte)(i + 1);
    }
  }

  public static void vectCopyInt() {
    for (int i = 0; i < LENGTH; i++) {
      c[i] = a[i];
    }
  }

  public static void vectCopyShort() {
    for (int i = 0; i < LENGTH; i++) {
      sc[i] = sa[i];
    }
  }

  public static void vectCopyByte() {
    for (int i = 0; i < LENGTH; i++) {
      bc[i] = ba[i];
    }
  }

  @Setup
  public void setup()
  {
    TestSIMDCopyInit();
  }

  @Benchmark
  public void testVectCopyByte() {
    vectCopyByte();
  }

  @Benchmark
  public void testVectCopyShort() {
    vectCopyShort();
  }

  @Benchmark
  public void testVectCopyInt() {
    vectCopyInt();
  }

}
