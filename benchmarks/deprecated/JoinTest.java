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

import java.lang.InterruptedException;
import java.lang.System;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.Random;

import org.linaro.bench.IterationsAnnotation;

public class JoinTest extends Thread {

  public static final int THREAD_NUMBER = 50;
  public static final int MAX_PAUSE = 1000;

  @IterationsAnnotation(noWarmup = true, iterations = 1)
  public static void timeJoinOfFiftyThreads(int iters) {
    for (int i = 0; i < iters; i++) {
      Random rnd = new Random(123456789);
      ArrayList<Thread> list = new ArrayList<Thread>();
      for (int x = 0; x < THREAD_NUMBER; x++) {
        list.add(new JoinTest(rnd));
      }
      for (Thread t : list) {
        t.start();
      }

      for (Thread t : list) {
        try {
          t.join();
        } catch (InterruptedException ie) {
          ie.printStackTrace();
          System.out.println("While joining");
        }
      }
    }
  }

  public static void main(String args[]) {

    long before = System.currentTimeMillis();
    timeJoinOfFiftyThreads(1);
    long after = System.currentTimeMillis();
    System.out.println("join of " + THREAD_NUMBER + ": " + (after - before));
  }

  public JoinTest() {
  }

  public JoinTest(Random rnd) {
    dummy = 0;
    firstPause = (long) rnd.nextInt(MAX_PAUSE);
    secondPause = (long) rnd.nextInt(MAX_PAUSE);
  }

  public void run() {
    try {
      sleep(firstPause);
    } catch (InterruptedException ie) {
      ie.printStackTrace();
      System.out.println("While first pause");
    }

    synchronized (lock) {
      dummy++;
    }

    try {
      sleep(secondPause);
    } catch (InterruptedException ie) {
      ie.printStackTrace();
      System.out.println("While second pause");
    }
  }

  public int dummy() {
    return dummy;
  }

  private int dummy;
  private long firstPause;
  private long secondPause;
  private static Object lock = new Object();
}
