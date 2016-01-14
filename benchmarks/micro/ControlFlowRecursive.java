/*
 *   Copyright (c) 2015, Linaro Limited
 *
 *   Copyright (c) 2004-2008 Brent Fulgham, 2005-2015 Isaac Gouy
 *   All rights reserved.
 *   Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 *     Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *     Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *     Neither the name of "The Computer Language Benchmarks Game" nor the name
 *     of "The Computer Language Shootout Benchmarks" nor the names of its
 *     contributors may be used to endorse or promote products derived from this
 *     software without specific prior written permission.

 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *   AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *   IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *   ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *   LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *   CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *   SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *   INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *   CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *   ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *   POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * A control flow recursive micro benchmark case.
 * This is converted from:
 *  https://github.com/WebKit/webkit/blob/master/PerformanceTests/SunSpider/tests/sunspider-1.0.2/controlflow-recursive.js
 */

package benchmarks.micro;

public class ControlFlowRecursive {
  private int result = 0;
  private final int expected = 57775;

  private int ack(int m, int n) {
    if (m == 0) {
      return n + 1;
    }
    if (n == 0) {
      return ack(m - 1, 1);
    }
    return ack(m - 1, ack(m, n - 1));
  }

  private int fib(int n) {
    if (n < 2) {
      return 1;
    }
    return fib(n - 2) + fib(n - 1);
  }

  private int tak(int x, int y, int z) {
    if (y >= x) {
      return z;
    }
    return tak(tak(x - 1, y, z), tak(y - 1, z, x), tak(z - 1, x, y));
  }

  public void timeControlFlowRecursive(int iterations) {
    for (int iter = 0; iter < iterations; ++iter) {
      result = 0;
      for (int i = 3; i <= 5; i++) {
        result += ack(3, i);
        result += fib(17 + i);
        result += tak(3 * i + 3, 2 * i + 2, i + 1);
      }
    }
  }
  /**
   * Verify
   **/

  /**
   * Called by the framework to assert the benchmarks have done the right thing.
   **/
  public boolean verify() {
    return result == expected;
  }

  /**
   * *NOT* called by the framework by default, provided for direct use only.
   **/
  public static void main(String[] args) {
    ControlFlowRecursive obj = new ControlFlowRecursive();
    long before = System.currentTimeMillis();
    obj.timeControlFlowRecursive(500);

    long after = System.currentTimeMillis();
    System.out.println("benchmarks/micro/ControlFlowRecursive: " + (after - before));
  }
}
