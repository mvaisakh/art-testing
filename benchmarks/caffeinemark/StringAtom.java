/*
 * This software is available under multiple licenses at
 * https://community.cablelabs.com/svn/OCAPRI/trunk/ri/RI_Stack/apps/vm_perf_test/src/com/tvworks/plateval/caffeinemark/StringAtom.java
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
 * Description:     The string test conducts a list of operations on strings and string buffers.
 * Main Focus:      TODO
 *
 */

package benchmarks.caffeinemark;

// CHECKSTYLE.OFF: .*
public class StringAtom
{

    public StringAtom()
    {
        wIterationCount = 50;
    }

    public boolean initialize(int i)
    {
        if(i != 0)
            wIterationCount = i;
        sPattern1 = "one ";
        sPattern2 = "two ";
        sPattern3 = "three ";
        return true;
    }

    public int execute()
    {
        stringbuffer.setLength(0);
        stringbuffer.append("Test");
        for(int j = 0; j < wIterationCount; j++)
            stringbuffer.append(sPattern1).append(sPattern2).append(sPattern3);

        stringbuffer.append("four");
        for(int k = 0; k < wIterationCount; k++)
        {
            int i = stringbuffer.toString().indexOf("four", k * 65);
        }

        return stringbuffer.length();
    }

    public String testName()
    {
        return new String("String");
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

    StringBuffer stringbuffer = new StringBuffer();
    public int wIterationCount;
    public String sPattern1;
    public String sPattern2;
    public String sPattern3;
    // CHECKSTYLE.ON: .*

  private static int PREDEFINED_ITER_COUNT = 500;

  public void timeStringAtom(int iters) {
    initialize(PREDEFINED_ITER_COUNT);
    for (int i = 0; i < iters; i++) {
      execute();
    }
  }

  public boolean verifyStringAtom() {
    initialize(PREDEFINED_ITER_COUNT);
    int expected = 7008;
    int found = execute();

    if (found != expected) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  public static void main(String[] argv) {
    int rc = 0;
    StringAtom obj = new StringAtom();

    final long before = System.currentTimeMillis();
    obj.timeStringAtom(50);
    final long after = System.currentTimeMillis();

    if (!obj.verifyStringAtom()) {
      rc++;
    }
    System.out.println("benchmarks/caffeinemark/StringAtom: " + (after - before));
    System.exit(rc);
  }
}
