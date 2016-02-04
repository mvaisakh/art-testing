/*
 * This software is available under multiple licenses at
 * https://community.cablelabs.com/svn/OCAPRI/trunk/ri/RI_Stack/apps/vm_perf_test/src/com/tvworks/plateval/caffeinemark/SieveAtom.java
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
 * Description:     The classic sieve of eratosthenes finds prime numbers.
 * Main Focus:      TODO
 *
 */

package benchmarks.caffeinemark;

// CHECKSTYLE.OFF: .*
public class SieveAtom
{

    public SieveAtom()
    {
        wMaxCandidate = 512;
    }

    public boolean initialize(int i)
    {
        if(i != 0)
            wMaxCandidate = i;
        wPrimes = new int[wMaxCandidate];
        return true;
    }

    public int execute()
    {
        int j = 1;
        boolean flag1 = false;
        wPrimes[0] = 1;
        wPrimes[1] = 2;
        j = 2;
        for(int i = 3; i < wMaxCandidate; i++)
        {
            int k = 1;
            boolean flag;
            for(flag = true; k < j && flag; k++)
                if(wPrimes[k] > 0 && wPrimes[k] <= i / 2 && i % wPrimes[k] == 0)
                    flag = false;

            if(flag)
            {
                j++;
                wPrimes[j - 1] = i;
            }
        }

        return j;
    }

    public String testName()
    {
        return new String("Sieve");
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
        return 2771;
    }

    public void setRemote()
    {
    }

    public int wPrimes[];
    public int wMaxCandidate;
    // CHECKSTYLE.ON: .*

  private static int PREDEFINED_MAX_CANDIDATE = 80000;

  public void timeSieveAtom(int iters) {
    initialize(PREDEFINED_MAX_CANDIDATE);
    for (int i = 0; i < iters; i++) {
      execute();
    }
  }

  public boolean verifySieveAtom() {
    initialize(PREDEFINED_MAX_CANDIDATE);
    int expected = 7838;
    int found = execute();

    if (found != expected) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  public static void main(String[] argv) {
    int rc = 0;
    SieveAtom obj = new SieveAtom();

    final long before = System.currentTimeMillis();
    obj.timeSieveAtom(5);
    final long after = System.currentTimeMillis();

    if (!obj.verifySieveAtom()) {
      rc++;
    }
    System.out.println("benchmarks/caffeinemark/SieveAtom: " + (after - before));
    System.exit(rc);
  }
}
