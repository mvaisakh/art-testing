/*
 * This software is available under multiple licenses at
 * https://community.cablelabs.com/svn/OCAPRI/trunk/ri/RI_Stack/apps/vm_perf_test/src/com/tvworks/plateval/caffeinemark/LoopAtom.java
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
 //        .Redistributions of source code must retain the above copyright notice, this list
 //             of conditions and the following disclaimer.
 //        .Redistributions in binary form must reproduce the above copyright notice, this list of
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
 * Description:     The loop test uses sorting and sequence generation as to measure compiler
 *                  optimization of loops.
 * Main Focus:      TODO
 *
 */

package benchmarks.caffeinemark;

// CHECKSTYLE.OFF: .*
public class LoopAtom
{
    public boolean initialize(int i)
    {
        if(i != 0)
            FIBCOUNT = i;
        fibs = new int[FIBCOUNT];
        return true;
    }

    public int execute()
    {
        fibs[0] = 1;
        for(int i = 1; i < FIBCOUNT; i++)
            fibs[i] = fibs[i - 1] + i;

        int j1 = 0;
        int k1 = 0;
        for(int j = 0; j < FIBCOUNT; j++)
        {
            for(int k = 1; k < FIBCOUNT; k++)
            {
                int l = FIBCOUNT + dummy;
                j1 += l;
                k1 += 2;
                if(fibs[k - 1] < fibs[k])
                {
                    int i1 = fibs[k - 1];
                    fibs[k - 1] = fibs[k];
                    fibs[k] = i1;
                }
            }

        }

        sum1 = j1;
        sum2 = k1;
        return fibs[0];
    }

    public String testName()
    {
        return new String("Loop");
    }

    public LoopAtom()
    {
        dummy = 12;
        FIBCOUNT = 64;
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
        return 2692;
    }

    public void setRemote()
    {
    }

    public int dummy;
    public int FIBCOUNT;
    public int sum1;
    public int sum2;
    public int fibs[];
    // CHECKSTYLE.ON: .*

  private static int PREDEFINED_FIB_COUNT = 1000;

  public void timeLoopAtom(int iters) {
    initialize(PREDEFINED_FIB_COUNT);
    for (int i = 0; i < iters; i++) {
      execute();
    }
  }

  public boolean verifyLoopAtom() {
    initialize(PREDEFINED_FIB_COUNT);
    int expected = 499501;
    int found = execute();

    if (found != expected) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  public static void main(String[] argv) {
    int rc = 0;
    LoopAtom obj = new LoopAtom();

    final long before = System.currentTimeMillis();
    obj.timeLoopAtom(120);
    final long after = System.currentTimeMillis();

    if (!obj.verifyLoopAtom()) {
      rc++;
    }
    System.out.println("benchmarks/caffeinemark/LoopAtom: " + (after - before));
    System.exit(rc);
  }
}
