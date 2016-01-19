/*
 * Revised BSD license
 *
 * This is a specific instance of the Open Source Initiative (OSI) BSD license
 * template http://www.opensource.org/licenses/bsd-license.php
 *
 *
 * Copyright © 2004-2008 Brent Fulgham, 2005-2015 Isaac Gouy All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *    Neither the name of "The Computer Language Benchmarks Game" nor the name
 *    of "The Computer Language Shootout Benchmarks" nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * This benchmark has been ported from "The Computer Language Benchmarks Game" suite and modified
 * to fit the benchmarking framework.
 * The original benchmarks printed long strings to the stdout. This print was overrided to do
 * nothing to fit the framework. These action can cause difference in behaviour of the original and
 * changed benchmarks; it hasn't been estimated yet.
 *
 * The original file is `fastaredux/fastaredux.java-3.java` from the archive
 * available at
 * http://benchmarksgame.alioth.debian.org/download/benchmarksgame-sourcecode.zip.
 *
 * The Computer Language Benchmarks Game
 * http://benchmarksgame.alioth.debian.org/
 *
 * modified by Enotus
 *
 */

/*
 * Description:     Generate and write random DNA sequences.
 * Main Focus:      TODO
 *
 */

package benchmarks.benchmarksgame;

import java.io.*;

public class FastaRedux {

    // CHECKSTYLE.OFF: .*
    static final int LINE_LENGTH = 60;
    static final int OUT_BUFFER_SIZE = 256*1024;
    static final int LOOKUP_SIZE = 4*1024;
    static final double LOOKUP_SCALE = LOOKUP_SIZE - 1;

    static final class Freq {
        byte c;
        double p;
        Freq(char cc, double pp) {c = (byte) cc;p = pp;}
    }

    static final String ALU =
            "GGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGG"
            + "GAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGA"
            + "CCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAAT"
            + "ACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCA"
            + "GCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGG"
            + "AGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCC"
            + "AGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAA";
    static final Freq[] IUB = {
        new Freq('a', 0.27),
        new Freq('c', 0.12),
        new Freq('g', 0.12),
        new Freq('t', 0.27),
        new Freq('B', 0.02),
        new Freq('D', 0.02),
        new Freq('H', 0.02),
        new Freq('K', 0.02),
        new Freq('M', 0.02),
        new Freq('N', 0.02),
        new Freq('R', 0.02),
        new Freq('S', 0.02),
        new Freq('V', 0.02),
        new Freq('W', 0.02),
        new Freq('Y', 0.02)};
    static final Freq[] HomoSapiens = {
        new Freq('a', 0.3029549426680),
        new Freq('c', 0.1979883004921),
        new Freq('g', 0.1975473066391),
        new Freq('t', 0.3015094502008)};

    static void sumAndScale(Freq[] a) {
        double p = 0;
        for (int i = 0; i < a.length; i++)
            a[i].p = (p += a[i].p) * LOOKUP_SCALE;
        a[a.length - 1].p = LOOKUP_SCALE;
    }

    static final class Random {
    
        static final int IM = 139968;
        static final int IA = 3877;
        static final int IC = 29573;
        static final double SCALE = LOOKUP_SCALE / IM;
        static int last = 42;

        static double next() {
            return SCALE * (last = (last * IA + IC) % IM);
        }
    }

    static final class Out {
    
        static final byte buf[] = new byte[OUT_BUFFER_SIZE];
        static final int lim = OUT_BUFFER_SIZE - 2*LINE_LENGTH - 1;
        static int ct = 0;
        static OutputStream stream;

        static void checkFlush() throws IOException {
            if (ct >= lim) { stream.write(buf, 0, ct); ct = 0;}
        }
        
        static void close() throws IOException {
            stream.write(buf, 0, ct);ct = 0;
            stream.close();
        }
    }
    
    static final class RandomFasta {

        static final Freq[] lookup=new Freq[LOOKUP_SIZE];
        
        static void makeLookup(Freq[] a) {
            for (int i = 0, j = 0; i < LOOKUP_SIZE; i++) {
                while (a[j].p < i) j++;
                lookup[i] = a[j];
            }
        }

        static void addLine(int bytes) throws IOException{
            Out.checkFlush();
            int lct=Out.ct;
            while(lct<Out.ct+bytes){
                double r = Random.next();
                int ai = (int) r; while (lookup[ai].p < r) ai++;
                Out.buf[lct++] = lookup[ai].c;
            }
            Out.buf[lct++] = (byte)'\n';
            Out.ct=lct;
        }

        static void make(String desc, Freq[] a, int n) throws IOException {
            makeLookup(a);

            System.arraycopy(desc.getBytes(), 0, Out.buf, Out.ct, desc.length());
            Out.ct+=desc.length();
            
            while (n > 0) {
                int bytes = Math.min(LINE_LENGTH, n);
                addLine(bytes);
                n -= bytes;
            }
        }
    }

    static final class RepeatFasta {

        static void make(String desc, byte[] alu, int n) throws IOException {
            System.arraycopy(desc.getBytes(), 0, Out.buf, Out.ct, desc.length());
            Out.ct+=desc.length();

            byte buf[] = new byte[alu.length + LINE_LENGTH];
            for (int i = 0; i < buf.length; i += alu.length)
                System.arraycopy(alu, 0, buf, i, Math.min(alu.length, buf.length - i));

            int pos = 0;
            while (n > 0) {
                int bytes = Math.min(LINE_LENGTH, n);
                Out.checkFlush();
                System.arraycopy(buf, pos, Out.buf, Out.ct, bytes); Out.ct+=bytes;
                Out.buf[Out.ct++] = (byte)'\n';
                pos = (pos + bytes) % alu.length;
                n -= bytes;
            }
        }
    }

    private void old_main() throws IOException {
        int n = 250000;

        RepeatFasta.make(">ONE Homo sapiens alu\n", ALU.getBytes(), n * 2);
        RandomFasta.make(">TWO IUB ambiguity codes\n", IUB, n * 3);
        RandomFasta.make(">THREE Homo sapiens frequency\n", HomoSapiens, n * 5);
    }
    // CHECKSTYLE.ON: .*

  /** Writes to nowhere */
  public class NullOutputStream extends OutputStream {
    @Override
    public void write(int b) throws IOException {
    }
  }

  public void timeFastaRedux(int iters) throws IOException {
    sumAndScale(IUB);
    sumAndScale(HomoSapiens);

    Out.stream = new NullOutputStream();

    for (int i = 0; i < iters; i++) {
      old_main();
    }
  }

  static final int VERIFY_MAGIC_NUMBER = 125;

  public boolean verify() throws IOException {
    // TODO: Test other cases run in `old_main()`.
    RandomFasta.make(">THREE Homo sapiens frequency\n", HomoSapiens, VERIFY_MAGIC_NUMBER);
    return Out.buf[VERIFY_MAGIC_NUMBER - 1] == 97;
  }

  public static void main(String[] args) throws IOException {
    FastaRedux obj = new FastaRedux();

    final long before = System.currentTimeMillis();
    obj.timeFastaRedux(10);
    final long after = System.currentTimeMillis();

    obj.verify();
    System.out.println("benchmarks/benchmarksgame/FastaRedux: " + (after - before));
  }
}
