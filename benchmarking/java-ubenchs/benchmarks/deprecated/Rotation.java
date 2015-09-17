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

import java.lang.System;

// This benchmark rotates three dimensional points.
public class Rotation {
  public final int NUM_POINTS = 50;
  public final int MAX_DEGREES = 90;
  public double[][] point;
  public double[][] coeff;

  public Rotation() {
    point = new double[3][NUM_POINTS];
    coeff = new double[3][3];
  }

  public void timeRotate(int iterations) {
    for (int iter = 0; iter < iterations; ++iter) {
      for (int degree = 0; degree < MAX_DEGREES; degree += 5) {
        double sin_value = Math.sin(degree * Math.PI / 180.0D);
        double cos_value = Math.cos(degree * Math.PI / 180.0D);

        coeff[0][0] = cos_value;
        coeff[1][0] = sin_value;
        coeff[2][0] = 0.0D;

        coeff[0][1] = -sin_value;
        coeff[1][1] = cos_value;
        coeff[2][1] = 0.0D;

        coeff[0][2] = 0.0D;
        coeff[1][2] = 0.0D;
        coeff[2][2] = 1.0D;

        for (int pt = 0; pt < NUM_POINTS; ++pt) {
          point[0][pt] = 0.0D;
          point[1][pt] = 0.0D;
          point[2][pt] = 0.0D;

          for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
              point[i][pt] += coeff[j][i] * point[i][pt];
            }
          }
        }
      }
    }
    return;
  }

  public static void main(String[] args) {
    long before;
    long after;
    Rotation obj = new Rotation();

    before = System.currentTimeMillis();
    obj.timeRotate(10);
    after = System.currentTimeMillis();
    System.out.println("Rotate 3D points : " + (after - before));
  }
}

