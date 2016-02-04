/*
 * This software is available under multiple licenses at
 * https://community.cablelabs.com/svn/OCAPRI/trunk/ri/RI_Stack/apps/vm_perf_test/src/com/tvworks/plateval/caffeinemark/MethodAtom.java
 * and we redistribute it under the BSD 2-clause License
 */

 // COPYRIGHT_BEGIN
 //  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 //
 //  Copyright (C) 2008-2013, Cable Television Laboratories, Inc.
 //
 //  This software is available under multiple licenses:
 //
 //  (1) BSD 2-clause
 //   Redistribution and use in source and binary forms, with or without modification, are
 //   permitted provided that the following conditions are met:
 //        ·Redistributions of source code must retain the above copyright notice, this list
 //             of conditions and the following disclaimer.
 //        ·Redistributions in binary form must reproduce the above copyright notice, this list of
 //             conditions and the following disclaimer in the documentation and/or other materials
 //             provided with the distribution.
 //   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 //   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 //   TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 //   PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 //   HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 //   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 //   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 //   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 //   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 //   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 //   THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 //
 //  (2) GPL Version 2
 //   This program is free software; you can redistribute it and/or modify
 //   it under the terms of the GNU General Public License as published by
 //   the Free Software Foundation, version 2. This program is distributed
 //   in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 //   even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 //   PURPOSE. See the GNU General Public License for more details.
 //
 //   You should have received a copy of the GNU General Public License along
 //   with this program.If not, see<http:www.gnu.org/licenses/>.
 //
 //  (3)CableLabs License
 //   If you or the company you represent has a separate agreement with CableLabs
 //   concerning the use of this code, your rights and obligations with respect
 //   to this code shall be as set forth therein. No license is granted hereunder
 //   for any other purpose.
 //
 //   Please contact CableLabs if you need additional information or
 //   have any questions.
 //
 //       CableLabs
 //       858 Coal Creek Cir
 //       Louisville, CO 80027-9750
 //       303 661-9100
 // COPYRIGHT_END

/*
 * Description:     The Method test executes recursive function calls to see how well the VM handles
 *                  method calls.
 * Main Focus:      TODO
 *
 */

package benchmarks.caffeinemark;

// CHECKSTYLE.OFF: .*
public class MethodAtom
{

    public MethodAtom()
    {
        wIterationCount = 100;
    }

    public int arithmeticSeries(int i)
    {
        if(i == 0)
            return 0;
        else
            return i + arithmeticSeries(i - 1);
    }

    public boolean initialize(int i)
    {
        if(i != 0)
            wIterationCount = i;
        return true;
    }

    public int execute()
    {
        int j = 0;
        for(int i = 0; i < wIterationCount; i++)
        {
            int k = arithmeticSeries(i);
            int l = notInlineableSeries(i);
            if(k > l)
                j += l;
            else
                j += k;
        }

        return j;
    }

    public String testName()
    {
        return new String("Method");
    }

    public void setLocal()
    {
    }

    public int cleanUp()
    {
        return 0;
    }

    public int defaultMagnification()
    {
        return 3728;
    }

    public int notInlineableSeries(int i)
    {
        int j = i;
        depthCount++;
        if(i == 0)
            return j;
        if((j & 1) != 0)
            j += notInlineableSeries(i - 1);
        else
            j += 1 + notInlineableSeries(i - 1);
        return j;
    }

    public void setRemote()
    {
    }

    public int depthCount;
    public int wIterationCount;
    // CHECKSTYLE.ON: .*

  private static int PREDEFINED_ITER_COUNT = 4000;

  public void timeMethodAtom(int iters) {
    initialize(PREDEFINED_ITER_COUNT);
    for (int i = 0; i < iters; i++) {
      execute();
    }
  }

  public boolean verifyMethodAtom() {
    initialize(PREDEFINED_ITER_COUNT);
    int expected = 2076731408;
    int found = execute();

    if (found != expected) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  public static void main(String[] argv) {
    int rc = 0;
    MethodAtom obj = new MethodAtom();

    final long before = System.currentTimeMillis();
    obj.timeMethodAtom(5);
    final long after = System.currentTimeMillis();

    if (!obj.verifyMethodAtom()) {
      rc++;
    }
    System.out.println("benchmarks/caffeinemark/MethodAtom: " + (after - before));
    System.exit(rc);
  }

}
