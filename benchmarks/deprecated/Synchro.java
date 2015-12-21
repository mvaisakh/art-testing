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

public class Synchro {

  public static final int ITERATIONS = 100000;

  public static void main(String[] args) {
    Synchro dummy = new Synchro();
    long before = System.currentTimeMillis();
    dummy.timeInc(ITERATIONS);
    long after = System.currentTimeMillis();
    System.out.println("Synchro: " + (after - before));
  }

  private Object lock1;
  private Object lock2;
  private Object lock3;
  private int value;

  public Synchro() {
    lock1 = new Object();
    lock2 = new Object();
    lock3 = new Object();
    value = 0;
  }

  public void timeInc(int iters) {
    for (int i = 0; i < iters; i++) {
      inc();
    }
  }

  public void inc() {
    synchronized (lock1) {
      synchronized (lock2) {
        synchronized (lock1) {
          synchronized (lock2) {
            synchronized (lock3) {
              value++;
            }
          }
        }
      }
    }
  }

  public int value() {
    return value;
  }
}

