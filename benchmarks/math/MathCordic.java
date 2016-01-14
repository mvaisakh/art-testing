/*
 * Copyright (C) 2015 Linaro Limited. All rights received.
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
 */

/* Ported from:
 *  webkit/PerformanceTests/SunSpider/tests/sunspider-1.0.2/math-cordic.js
 *
 * Copyright (C) Rich Moore.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL APPLE INC. OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * Description:     A simple and efficient algorithm used to calculate
 *                  trigonometric cos and sin functions using additions,
 *                  subtractions and bitshifts.
 * Main Focus:      Bitshifts, Floating-Point operations.
 *
 */

package benchmarks.math;

import java.lang.System;

public class MathCordic {
  private static double[] ANGLES;
  private static final double AG_CONST = 0.6072529350;
  private static final double CORDIC_EXPECTED = 10362.570468755888;
  private static final double RAD_CONST = 0.017453;
  private static final double TARGET_CONST = 28.027;
  private static final double X_CONST = 65536.0;

  private static double fixedMul(double x) {
    return x * X_CONST;
  }

  private static double fixedDiv(double x) {
    return x / X_CONST;
  }

  private static double deg2rad(double x) {
    return x * RAD_CONST;
  }

  static {
    ANGLES = new double[] {
        fixedMul(45.0), fixedMul(26.565), fixedMul(14.0362), fixedMul(7.12502),
        fixedMul(3.57633), fixedMul(1.78991), fixedMul(0.895174), fixedMul(0.447614),
        fixedMul(0.223811), fixedMul(0.111906), fixedMul(0.055953), fixedMul(0.027977) };
  }

  private double cordicSinCos(double target) {
    double x;
    double y;
    double targetAngle;
    double currentAngle;

    x = fixedMul(AG_CONST);     /* AG_CONST * cos(0) */
    y = 0.0;                    /* AG_CONST * sin(0) */
    currentAngle = 0.0;
    targetAngle = fixedMul(target);

    for (int i = 0; i < ANGLES.length; i++) {
      double newX;
      if (targetAngle > currentAngle) {
        newX = x - ((long)y >> i);
        y = ((long)x >> i) + y;
        x = newX;
        currentAngle += ANGLES[i];
      } else {
        newX = x + ((long)y >> i);
        y = -((long)x >> i) + y;
        x = newX;
        currentAngle -= ANGLES[i];
      }
    }

    return fixedDiv(x) * fixedDiv(y);
  }

  public void timeMathCordic(int iters) {
    for (int i = 0; i < iters; i++) {
      for (int j = 0; j < 25000; j++) {
        cordicSinCos(TARGET_CONST);
      }
    }
  }

  public boolean verify() {
    double cordicOutput = 0.0;
    for (int i = 0; i < 25000; i++) {
      cordicOutput += cordicSinCos(TARGET_CONST);
    }
    return cordicOutput == CORDIC_EXPECTED;
  }

  public static void main(String[] argv) {
    MathCordic obj = new MathCordic();
    final long before = System.currentTimeMillis();
    obj.timeMathCordic(175);
    final long after = System.currentTimeMillis();
    obj.verify();
    System.out.println("benchmarks/math/MathCordic: " + (after - before));
  }
}
