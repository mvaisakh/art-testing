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

import java.lang.reflect.Method;

public class MultiplyAdd {
    public final static int ITERATIONS = 100000000;
    public final static int VALUE = 500;

    public static void main(String[] args) {
        long start, end = 0;
        MultiplyAdd test = new MultiplyAdd();

        Method[] methods = test.getClass().getDeclaredMethods();
        // sort methods by name
        for (int i = 0; i < methods.length; i++) {
            for (int j = 0; j < methods.length - i - 1; j++) {
                if (methods[j].getName().compareTo(methods[j + 1].getName()) > 0) {
                    Method tmp = methods[j];
                    methods[j] = methods[j + 1];
                    methods[j + 1] = tmp;
                }
            }
        }

        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            if (m.getName().startsWith("time")) {
                start = System.currentTimeMillis();
                end = start - 1;
                try {
                    Object o = m.invoke(test, ITERATIONS);
                    end = System.currentTimeMillis();
                } catch (Exception e) {
                    System.err.println("Invoke method: " + m.toGenericString());
                }
                System.out.println(m.getDeclaringClass().getName() + "."
                        + m.getName().substring(4) + ": " + (end - start));
            }
        }
    }

    public int timeSimpleMaddw(int iters) {
        int result = 0;
        for (int i = 0; i < iters; i++) {
            result += i * i;
        }
        return result;
    }

    public long timeSimpleMaddx(int iters) {
        long result = 0;
        for (int i = 0; i < iters; i++) {
            long tmp = i;
            result += tmp * tmp;
        }
        return result;
    }

    public int timeSimpleMsubw(int iters) {
        int result = 0;
        for (int i = 0; i < iters; i++) {
            result -= i * i;
        }
        return result;
    }

    public long timeSimpleMsubx(int iters) {
        long result = 0;
        for (int i = 0; i < iters; i++) {
            long tmp = i;
            result += tmp * tmp;
        }
        return result;
    }

    public int timeMaddwBack2back(int iters) {
        int result = 0;
        int tmp1 = 0;
        int tmp2 = 0;
        for (int i = 0; i < iters; i++) {
            tmp1 += i * i;
            tmp2 += tmp1 * tmp1;
            result += tmp2 * tmp2;
        }
        return result;
    }

    public long timeMaddxBack2back(int iters) {
        long result = 0;
        long tmp1 = 0;
        long tmp2 = 0;
        for (int i = 0; i < iters; i++) {
            long a = i;
            tmp1 += a * a;
            tmp2 += tmp1 * tmp1;
            result += tmp2 * tmp2;
        }
        return result;
    }

    public int timeMaddwInterleave(int iters) {
        int result = 0;
        int tmp1 = 0;
        int tmp2 = 0;
        for (int i = 0; i < iters; i++) {
            tmp1 += i * i;
            tmp2 = tmp1 + tmp1;
            result += tmp2 * tmp2;
        }
        return result;
    }

    public long timeMaddxInterleave(int iters) {
        long result = 0;
        long tmp1 = 0;
        long tmp2 = 0;
        for (int i = 0; i < iters; i++) {
            long a = i;
            tmp1 += a * a;
            tmp2 += tmp1 + tmp1;
            result += tmp2 * tmp2;
        }
        return result;
    }

    public long timeMixed(int iters) {
        long result = 0;
        long tmp = 0;
        long lt = 12;
        int a = 0;
        for (int i = 0; i < iters; i++) {
            a = (i - 2) + i * i;
            a = 1 + a * a;
            tmp = a;
            result -= tmp * tmp;
            tmp++;
            result += tmp * tmp + (tmp >> 2) * tmp + (tmp >> 2) * lt;
        }
        return result;
    }
}
