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

import java.lang.System;
import java.util.ArrayList;
import java.util.Random;

import com.arm.microbench.IterationsAnnotation;

public class InstanceTest {

  public final static int ITERATIONS = 1000;
  public final static int INSTANCES = 1000;

  public static void main(String[] args) {
    long before = System.currentTimeMillis();
    timeCheckInstances(ITERATIONS);
    long after = System.currentTimeMillis();
    System.out.println("instanceof: " + (after - before));
  }

  public static void initializeInstances(ArrayList<Object> list, Random rnd) {
    Object o = null;
    for (int i = 0; i < INSTANCES; i++) {
      switch (rnd.nextInt(4)) {
        case 0: o = new A(); break;
        case 1: o = new B(); break;
        case 2: o = new C(); break;
        case 3: o = new D(); break;
        default: o = null;
      }
      list.add(o);
    }
  }

  @IterationsAnnotation(noWarmup=true, iterations=1000)
  public static void timeCheckInstances(int iters) {
      ArrayList<Object> list = new ArrayList<Object>();
      Random rnd = new Random(123456789);
      initializeInstances(list, rnd);
      for (int i = 0; i < iters; i++) {
          checkInstances(list);
      }
  }

  public static void checkInstances(ArrayList<Object> list) {
    answerA = 0;
    answerB = 0;
    answerC = 0;
    answerD = 0;
    error = 0;

    for (Object o : list) {
      if (o instanceof A) answerA++;
      if (o instanceof B) answerB++;
      if (o instanceof C) answerC++;
      if (o instanceof D) answerD++;
      if (o == null) error++;
    }
  }

  private static int answerA = 0;
  private static int answerB = 0;
  private static int answerC = 0;
  private static int answerD = 0;
  private static int error = 0;
}

class A {
}

class B extends A {
}

class C extends B {
}

class D {
}
