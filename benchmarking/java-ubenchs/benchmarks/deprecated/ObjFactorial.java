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
import java.math.BigInteger;

class MyLong {
    private long value;

    public MyLong(long value) {
        this.value = value;
    }

    public long get() {
        return value;
    }

    public void set(long newValue) {
        value = newValue;
    }

    public int compareTo(MyLong other) {
        if (value > other.value)
            return 1;
        if (value == other.value)
            return 0;
        return -1;
    }
}

public class ObjFactorial {

    public static void timeBigFact(int iters) {
        BigInteger bigOne = new BigInteger("1");
        BigInteger bigInput = new BigInteger("20");
        BigInteger bigResult = bigOne;
        for (int x = 0; x < iters; x++) {
            bigResult = bigOne;
            for (BigInteger i = bigOne; i.compareTo(bigInput) == -1; i = i.add(bigOne)) {
                bigResult = bigResult.multiply(i);
            }
        }
    }

    public static void timeMyFact(int iters) {
        MyLong myInput = new MyLong(20);
        MyLong myResult = new MyLong(1);
        for (int x = 0; x < iters; x++) {
            myResult = new MyLong(1);
            for (MyLong i = new MyLong(1); i.compareTo(myInput) == -1; i.set(i.get() + 1)) {
                myResult.set(myResult.get() * i.get());
            }
        }
    }

    public static void main(String[] args) {

        final int ITERATIONS_BIG = 1000;
        final int ITERATIONS_MY = 100000;

        long before = System.currentTimeMillis();
        timeBigFact(ITERATIONS_BIG);
        long after = System.currentTimeMillis();
        System.out.println("bigFact: " + (after - before));

        before = System.currentTimeMillis();
        timeMyFact(ITERATIONS_MY);
        after = System.currentTimeMillis();

        System.out.println("myFact: " + (after - before));

    }
}
