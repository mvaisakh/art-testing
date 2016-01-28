/*
 * This benchmark has been ported from "The Computer Language Benchmarks Game" suite and slightly
 * modified to fit the benchmarking framework.
 *
 * The original file is `knucleotide/knucleotide.java-4.java` from the archive
 * available at
 * http://benchmarksgame.alioth.debian.org/download/benchmarksgame-sourcecode.zip.
 * See LICENSE file in the same folder (BSD 3-clause).
 *
 * The Computer Language Benchmarks Game
 * http://benchmarksgame.alioth.debian.org/
 *
 * contributed by Daryl Griffith
 */

/*
 * Description:     Hashtable update and k-nucleotide strings.
 * Main Focus:      TODO
 *
 */

package benchmarks.benchmarksgame;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

// CHECKSTYLE.OFF: .*
public class knucleotide {

  private byte[] temp = null;
  private byte[] buffer = null;
  private byte[] species = null;
  private ByteArrayInputStream stream = null;

    static Map<Key, Value> MAP;
    static final int[] SEQUENCES1 = {2, 1};
    static final int[] SEQUENCES2 = {18, 12, 6, 4, 3};
    static final String[] SPICIFIC_SEQUENCES = new String[]{"GGT", "GGTA", "GGTATT", "GGTATTTTAATT", "GGTATTTTAATTTATAGT"};
    static final int LINE_LENGTH = 60;
    static final int EOF = -1;
    static byte[] nucleotides;

    public void old_main() {
        {
            int n;
            int i;

            try (LineInputStream in = new LineInputStream(stream)) {
outer:
                for (;;) {
                    n = in.readLine(temp);
                    if (n == EOF) {
                        return;
                    }
                    if (n != LINE_LENGTH) {
                        for (i = 0; i < species.length; i++) {
                            if (temp[i] != species[i]) {
                                continue outer;
                            }
                        }
                        break;
                    }
                }
                i = 0;
                for (;;) {
                    n = in.readLine(temp);
                    if (n == EOF) {
                        break;
                    }
                    for (int j = 0; j < n; i++, j++) {
                        buffer[i] = translate(temp[j]);
                    }
                }
                if (i == buffer.length) {
                    nucleotides = buffer;
                } else {
                    nucleotides = new byte[i];
                    System.arraycopy(buffer, 0, nucleotides, 0, i);
                }
            } catch (IOException e) {

            }
        }
        countSequences(SEQUENCES1);
        {
            List<Entry<Key, Value>> sequence1 = new ArrayList<>();
            List<Entry<Key, Value>> sequence2 = new ArrayList<>();

            for (Entry<Key, Value> entry : MAP.entrySet()) {
                switch (Long.numberOfLeadingZeros(entry.getKey().key)) {
                    case 61:
                        sequence1.add(entry);
                        break;
                    case 59:
                        sequence2.add(entry);
                }
            }
            printSequence(sequence1);
            printSequence(sequence2);
        }
        countSequences(SEQUENCES2);
        {
            Key key = new Key();
        }
    }
        
    static byte translate(byte b) {
        return (byte) ((b >> 1) & 3);
    }

    static void countSequences(int[] sequences) {
        for (int sequence : sequences) {
            updateHashtable(sequence);
        }
    }

    static void updateHashtable(int sequence) {
        int sequenceTop = nucleotides.length - sequence + 1;
        Key key = new Key();
        Value value;
        
        for (int i = 0; i < sequenceTop; i++) {
            key.setHash(i, sequence);
            value = MAP.get(key);
            if (value == null) {
                value = new Value();
                value.count = 1;
                MAP.put(key, value);
                key = new Key();
            } else {
                value.count++;
            }
        }
    }

    static void printSequence(List<Entry<Key, Value>> sequence) {
        int sum = 0;

        Collections.sort(sequence, new Comparator<Entry<Key, Value>>() {

            @Override
            public int compare(Entry<Key, Value> entry1, Entry<Key, Value> entry2) {
                if (entry2.getValue().count != entry1.getValue().count) {
                    return entry2.getValue().count - entry1.getValue().count;
                }
                return entry1.getKey().toString().compareTo(entry2.getKey().toString());
            }
        });
        for (Entry<Key, Value> entry : sequence) {
            sum += entry.getValue().count;
        }
    }

    static class LineInputStream implements Closeable {

        private static final int LF = 10;
        private final ByteBuffer buffer = ByteBuffer.allocate(8192);
        private final InputStream in;

        public LineInputStream(InputStream in) {
            this.in = in;
            buffer.limit(buffer.position());
        }

        public int readLine(byte[] b) throws IOException {
            for (int end = buffer.position(); end < buffer.limit(); end++) {
                if (buffer.get(end) == LF) {
                    if (end - buffer.position() == LINE_LENGTH) {
                        buffer.get(b);
                        buffer.position(buffer.position() + 1);
                        return LINE_LENGTH;
                    } else {
                        int size = end - buffer.position();

                        buffer.get(b, 0, size);
                        buffer.position(buffer.position() + 1);
                        return size;
                    }
                }
            }
            buffer.compact();
            int n = in.read(buffer.array(), buffer.position(), buffer.remaining());

            if (n == EOF) {
                buffer.flip();
                if (buffer.hasRemaining()) {
                    int size = buffer.remaining();

                    buffer.get(b, 0, size);
                    return size;
                } else {
                    return EOF;
                }
            } else {
                buffer.position(buffer.position() + n);
                buffer.flip();
            }
            for (int end = buffer.position(); end < buffer.limit(); end++) {
                if (buffer.get(end) == LF) {
                    if (end - buffer.position() == LINE_LENGTH) {
                        buffer.get(b);
                        buffer.position(buffer.position() + 1);
                        return LINE_LENGTH;
                    } else {
                        int size = end - buffer.position();

                        buffer.get(b, 0, size);
                        buffer.position(buffer.position() + 1);
                        return size;
                    }
                }
            }
            return EOF;
        }

        @Override
        public void close() throws IOException {
            in.close();
        }
    }

    static class Key {

        long key;

        void setHash(int offset, int length) {
            key = 1;
            for (int i = offset + length - 1; i >= offset; i--) {
                key = (key << 2) | nucleotides[i];
            }
        }

        void setHash(String species) {
            key = 1;
            for (int i = species.length() - 1; i >= 0; i--) {
                key = (key << 2) | translate((byte) species.charAt(i));
            }
        }

        @Override
        public int hashCode() {
            return (int) key;
        }

        @Override
        public boolean equals(Object obj) {
            final Key other = (Key) obj;

            return key == other.key;
        }

        @Override
        public String toString() {
            char[] name = new char[(63 - Long.numberOfLeadingZeros(key)) / 2];
            long temp = key;

            for (int i = 0; temp > 1; temp >>= 2, i++) {
                name[i] = (char) (((temp & 3) << 1) | 'A');
                if (name[i] == 'E') {
                    name[i] = 'T';
                }
            }
            return new String(name);
        }
    }

    static class Value {

        int count;
    }
    // CHECKSTYLE.ON: .*

  public void reinitBuffers() {
    MAP = new HashMap<>();
    species = ">TH".getBytes();
  }

  public void timeKnucleotide(int iters) {
    temp = new byte[LINE_LENGTH];
    buffer = new byte[125_000_000];
    stream = new ByteArrayInputStream(fastaStr.getBytes());

    for (int count = 0; count < iters; count++) {
      reinitBuffers();
      old_main();
      stream.reset();
    }
  }

  public boolean verifyKnucleotide() {
    Key key = new Key();
    key.setHash(SPICIFIC_SEQUENCES[2]);

    int expected = 12;
    int found = MAP.get(key).count;
    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }

    return true;
  }

  public static void main(String[] args) {
    int rc = 0;
    knucleotide obj = new knucleotide();

    final long before = System.currentTimeMillis();
    obj.timeKnucleotide(15);
    final long after = System.currentTimeMillis();

    if (!obj.verifyKnucleotide()) {
      rc++;
    }
    System.out.println("benchmarks/benchmarksgame/knucleotide: " + (after - before));
    System.exit(rc);
  }

  private static final String fastaStr = ">ONE Homo sapiens alu\n"
      + "GGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGGGAGGCCGAGGCGGGCGGA\n"
      + "TCACCTGAGGTCAGGAGTTCGAGACCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACT\n"
      + "AAAAATACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCAGCTACTCGGGAG\n"
      + "GCTGAGGCAGGAGAATCGCTTGAACCCGGGAGGCGGAGGTTGCAGTGAGCCGAGATCGCG\n"
      + "CCACTGCACTCCAGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAAGGCCGGGCGCGGT\n"
      + "GGCTCACGCCTGTAATCCCAGCACTTTGGGAGGCCGAGGCGGGCGGATCACCTGAGGTCA\n"
      + "GGAGTTCGAGACCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAATACAAAAA\n"
      + "TTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCAGCTACTCGGGAGGCTGAGGCAGGAG\n"
      + "AATCGCTTGAACCCGGGAGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCCA\n"
      + "GCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAAGGCCGGGCGCGGTGGCTCACGCCTGT\n"
      + "AATCCCAGCACTTTGGGAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGACC\n"
      + "AGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAATACAAAAATTAGCCGGGCGTG\n"
      + "GTGGCGCGCGCCTGTAATCCCAGCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACC\n"
      + "CGGGAGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCCAGCCTGGGCGACAG\n"
      + "AGCGAGACTCCGTCTCAAAAAGGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTT\n"
      + "TGGGAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGACCAGCCTGGCCAACA\n"
      + "TGGTGAAACCCCGTCTCTACTAAAAATACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCT\n"
      + "GTAATCCCAGCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGGAGGCGGAGG\n"
      + "TTGCAGTGAGCCGAGATCGCGCCACTGCACTCCAGCCTGGGCGACAGAGCGAGACTCCGT\n"
      + "CTCAAAAAGGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGGGAGGCCGAGG\n"
      + "CGGGCGGATCACCTGAGGTCAGGAGTTCGAGACCAGCCTGGCCAACATGGTGAAACCCCG\n"
      + "TCTCTACTAAAAATACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCAGCTA\n"
      + "CTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGGAGGCGGAGGTTGCAGTGAGCCG\n"
      + "AGATCGCGCCACTGCACTCCAGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAAGGCCG\n"
      + "GGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGGGAGGCCGAGGCGGGCGGATCACC\n"
      + "TGAGGTCAGGAGTTCGAGACCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAA\n"
      + "TACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCAGCTACTCGGGAGGCTGA\n"
      + "GGCAGGAGAATCGCTTGAACCCGGGAGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACT\n"
      + "GCACTCCAGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAAGGCCGGGCGCGGTGGCTC\n"
      + "ACGCCTGTAATCCCAGCACTTTGGGAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGT\n"
      + "TCGAGACCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAATACAAAAATTAGC\n"
      + "CGGGCGTGGTGGCGCGCGCCTGTAATCCCAGCTACTCGGGAGGCTGAGGCAGGAGAATCG\n"
      + "CTTGAACCCGGGAGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCCAGCCTG\n"
      + "GGCGACAGAGCGAGACTCCGTCTCAAAAAGGCCGGGCGCGGTGGCTCACGCCTGTAATCC\n"
      + "CAGCACTTTGGGAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGACCAGCCT\n"
      + "GGCCAACATGGTGAAACCCCGTCTCTACTAAAAATACAAAAATTAGCCGGGCGTGGTGGC\n"
      + "GCGCGCCTGTAATCCCAGCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGGA\n"
      + "GGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCCAGCCTGGGCGACAGAGCGA\n"
      + "GACTCCGTCTCAAAAAGGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGGGA\n"
      + "GGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGACCAGCCTGGCCAACATGGTG\n"
      + "AAACCCCGTCTCTACTAAAAATACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAAT\n"
      + "CCCAGCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGGAGGCGGAGGTTGCA\n"
      + "GTGAGCCGAGATCGCGCCACTGCACTCCAGCCTGGGCGACAGAGCGAGACTCCGTCTCAA\n"
      + "AAAGGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGGGAGGCCGAGGCGGGC\n"
      + "GGATCACCTGAGGTCAGGAGTTCGAGACCAGCCTGGCCAACATGGTGAAACCCCGTCTCT\n"
      + "ACTAAAAATACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCAGCTACTCGG\n"
      + "GAGGCTGAGGCAGGAGAATCGCTTGAACCCGGGAGGCGGAGGTTGCAGTGAGCCGAGATC\n"
      + "GCGCCACTGCACTCCAGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAAGGCCGGGCGC\n"
      + "GGTGGCTCACGCCTGTAATCCCAGCACTTTGGGAGGCCGAGGCGGGCGGATCACCTGAGG\n"
      + "TCAGGAGTTCGAGACCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAATACAA\n"
      + "AAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCAGCTACTCGGGAGGCTGAGGCAG\n"
      + "GAGAATCGCTTGAACCCGGGAGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACT\n"
      + "CCAGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAAGGCCGGGCGCGGTGGCTCACGCC\n"
      + "TGTAATCCCAGCACTTTGGGAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAG\n"
      + "ACCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAATACAAAAATTAGCCGGGC\n"
      + "GTGGTGGCGCGCGCCTGTAATCCCAGCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGA\n"
      + "ACCCGGGAGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCCAGCCTGGGCGA\n"
      + "CAGAGCGAGACTCCGTCTCAAAAAGGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCA\n"
      + "CTTTGGGAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGACCAGCCTGGCCA\n"
      + "ACATGGTGAAACCCCGTCTCTACTAAAAATACAAAAATTAGCCGGGCGTGGTGGCGCGCG\n"
      + "CCTGTAATCCCAGCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGGAGGCGG\n"
      + "AGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCCAGCCTGGGCGACAGAGCGAGACTC\n"
      + "CGTCTCAAAAAGGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGGGAGGCCG\n"
      + "AGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGACCAGCCTGGCCAACATGGTGAAACC\n"
      + "CCGTCTCTACTAAAAATACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCAG\n"
      + "CTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGGAGGCGGAGGTTGCAGTGAG\n"
      + "CCGAGATCGCGCCACTGCACTCCAGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAAGG\n"
      + "CCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGGGAGGCCGAGGCGGGCGGATC\n"
      + "ACCTGAGGTCAGGAGTTCGAGACCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAA\n"
      + "AAATACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCAGCTACTCGGGAGGC\n"
      + "TGAGGCAGGAGAATCGCTTGAACCCGGGAGGCGGAGGTTGCAGTGAGCCGAGATCGCGCC\n"
      + "ACTGCACTCCAGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAAGGCCGGGCGCGGTGG\n"
      + "CTCACGCCTGTAATCCCAGCACTTTGGGAGGCCGAGGCGGGCGGATCACCTGAGGTCAGG\n"
      + "AGTTCGAGACCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAATACAAAAATT\n"
      + "AGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCAGCTACTCGGGAGGCTGAGGCAGGAGAA\n"
      + "TCGCTTGAACCCGGGAGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCCAGC\n"
      + "CTGGGCGACAGAGCGAGACTCCGTCTCAAAAAGGCCGGGCGCGGTGGCTCACGCCTGTAA\n"
      + "TCCCAGCACTTTGGGAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGACCAG\n"
      + "CCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAATACAAAAATTAGCCGGGCGTGGT\n"
      + "GGCGCGCGCCTGTAATCCCAGCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCG\n"
      + "GGAGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCCAGCCTGGGCGACAGAG\n"
      + "CGAGACTCCGTCTCAAAAAGGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTG\n"
      + "GGAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGACCAGCCTGGCCAACATG\n"
      + "GTGAAACCCCGTCTCTACTAAAAATACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGT\n"
      + "AATCCCAGCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGGAGGCGGAGGTT\n"
      + "GCAGTGAGCCGAGATCGCGCCACTGCACTCCAGCCTGGGCGACAGAGCGAGACTCCGTCT\n"
      + "CAAAAAGGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGGGAGGCCGAGGCG\n"
      + "GGCGGATCACCTGAGGTCAGGAGTTCGAGACCAGCCTGGCCAACATGGTGAAACCCCGTC\n"
      + "TCTACTAAAAATACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCAGCTACT\n"
      + "CGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGGAGGCGGAGGTTGCAGTGAGCCGAG\n"
      + "ATCGCGCCACTGCACTCCAGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAAGGCCGGG\n"
      + "CGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGGGAGGCCGAGGCGGGCGGATCACCTG\n"
      + "AGGTCAGGAGTTCGAGACCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAATA\n"
      + "CAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCAGCTACTCGGGAGGCTGAGG\n"
      + "CAGGAGAATCGCTTGAACCCGGGAGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGC\n"
      + "ACTCCAGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAAGGCCGGGCGCGGTGGCTCAC\n"
      + "GCCTGTAATCCCAGCACTTTGGGAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTC\n"
      + "GAGACCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAATACAAAAATTAGCCG\n"
      + "GGCGTGGTGGCGCGCGCCTGTAATCCCAGCTACTCGGGAGGCTGAGGCAGGAGAATCGCT\n"
      + "TGAACCCGGGAGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCCAGCCTGGG\n"
      + "CGACAGAGCGAGACTCCGTCTCAAAAAGGCCGGGCGCGGTGGCTCACGCCTGTAATCCCA\n"
      + "GCACTTTGGGAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGACCAGCCTGG\n"
      + "CCAACATGGTGAAACCCCGTCTCTACTAAAAATACAAAAATTAGCCGGGCGTGGTGGCGC\n"
      + "GCGCCTGTAATCCCAGCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGGAGG\n"
      + "CGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCCAGCCTGGGCGACAGAGCGAGA\n"
      + "CTCCGTCTCAAAAAGGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGGGAGG\n"
      + "CCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGACCAGCCTGGCCAACATGGTGAA\n"
      + "ACCCCGTCTCTACTAAAAATACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCC\n"
      + "CAGCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGGAGGCGGAGGTTGCAGT\n"
      + "GAGCCGAGATCGCGCCACTGCACTCCAGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAA\n"
      + "AGGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGGGAGGCCGAGGCGGGCGG\n"
      + "ATCACCTGAGGTCAGGAGTTCGAGACCAGCCTGGCCAACATGGTGAAACCCCGTCTCTAC\n"
      + "TAAAAATACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCAGCTACTCGGGA\n"
      + "GGCTGAGGCAGGAGAATCGCTTGAACCCGGGAGGCGGAGGTTGCAGTGAGCCGAGATCGC\n"
      + "GCCACTGCACTCCAGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAAGGCCGGGCGCGG\n"
      + "TGGCTCACGCCTGTAATCCCAGCACTTTGGGAGGCCGAGGCGGGCGGATCACCTGAGGTC\n"
      + "AGGAGTTCGAGACCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAATACAAAA\n"
      + "ATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCAGCTACTCGGGAGGCTGAGGCAGGA\n"
      + "GAATCGCTTGAACCCGGGAGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCC\n"
      + "AGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAAGGCCGGGCGCGGTGGCTCACGCCTG\n"
      + "TAATCCCAGCACTTTGGGAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGAC\n"
      + "CAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAATACAAAAATTAGCCGGGCGT\n"
      + "GGTGGCGCGCGCCTGTAATCCCAGCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAAC\n"
      + "CCGGGAGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCCAGCCTGGGCGACA\n"
      + "GAGCGAGACTCCGTCTCAAAAAGGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACT\n"
      + "TTGGGAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGACCAGCCTGGCCAAC\n"
      + "ATGGTGAAACCCCGTCTCTACTAAAAATACAAAAATTAGCCGGGCGTGGTGGCGCGCGCC\n"
      + "TGTAATCCCAGCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGGAGGCGGAG\n"
      + "GTTGCAGTGAGCCGAGATCGCGCCACTGCACTCCAGCCTGGGCGACAGAGCGAGACTCCG\n"
      + "TCTCAAAAAGGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGGGAGGCCGAG\n"
      + "GCGGGCGGATCACCTGAGGTCAGGAGTTCGAGACCAGCCTGGCCAACATGGTGAAACCCC\n"
      + "GTCTCTACTAAAAATACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCAGCT\n"
      + "ACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGGAGGCGGAGGTTGCAGTGAGCC\n"
      + "GAGATCGCGCCACTGCACTCCAGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAAGGCC\n"
      + "GGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGGGAGGCCGAGGCGGGCGGATCAC\n"
      + "CTGAGGTCAGGAGTTCGAGACCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAA\n"
      + "ATACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCAGCTACTCGGGAGGCTG\n"
      + "AGGCAGGAGAATCGCTTGAACCCGGGAGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCAC\n"
      + "TGCACTCCAGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAAGGCCGGGCGCGGTGGCT\n"
      + "CACGCCTGTAATCCCAGCACTTTGGGAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAG\n"
      + "TTCGAGACCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAATACAAAAATTAG\n"
      + "CCGGGCGTGGTGGCGCGCGCCTGTAATCCCAGCTACTCGGGAGGCTGAGGCAGGAGAATC\n"
      + "GCTTGAACCCGGGAGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCCAGCCT\n"
      + "GGGCGACAGAGCGAGACTCCGTCTCAAAAAGGCCGGGCGCGGTGGCTCACGCCTGTAATC\n"
      + "CCAGCACTTTGGGAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGACCAGCC\n"
      + "TGGCCAACATGGTGAAACCCCGTCTCTACTAAAAATACAAAAATTAGCCGGGCGTGGTGG\n"
      + "CGCGCGCCTGTAATCCCAGCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGG\n"
      + "AGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCCAGCCTGGGCGACAGAGCG\n"
      + "AGACTCCGTCTCAAAAAGGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGGG\n"
      + "AGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGACCAGCCTGGCCAACATGGT\n"
      + "GAAACCCCGTCTCTACTAAAAATACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAA\n"
      + "TCCCAGCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGGAGGCGGAGGTTGC\n"
      + "AGTGAGCCGAGATCGCGCCACTGCACTCCAGCCTGGGCGACAGAGCGAGACTCCGTCTCA\n"
      + "AAAAGGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGGGAGGCCGAGGCGGG\n"
      + "CGGATCACCTGAGGTCAGGAGTTCGAGACCAGCCTGGCCAACATGGTGAAACCCCGTCTC\n"
      + "TACTAAAAATACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCAGCTACTCG\n"
      + "GGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGGAGGCGGAGGTTGCAGTGAGCCGAGAT\n"
      + "CGCGCCACTGCACTCCAGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAAGGCCGGGCG\n"
      + "CGGTGGCTCACGCCTGTAATCCCAGCACTTTGGGAGGCCGAGGCGGGCGGATCACCTGAG\n"
      + "GTCAGGAGTTCGAGACCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAATACA\n"
      + "AAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCAGCTACTCGGGAGGCTGAGGCA\n"
      + "GGAGAATCGCTTGAACCCGGGAGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCAC\n"
      + "TCCAGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAAGGCCGGGCGCGGTGGCTCACGC\n"
      + "CTGTAATCCCAGCACTTTGGGAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGA\n"
      + "GACCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAATACAAAAATTAGCCGGG\n"
      + "CGTGGTGGCGCGCGCCTGTAATCCCAGCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTG\n"
      + "AACCCGGGAGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCCAGCCTGGGCG\n"
      + "ACAGAGCGAGACTCCGTCTCAAAAAGGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGC\n"
      + "ACTTTGGGAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGACCAGCCTGGCC\n"
      + "AACATGGTGAAACCCCGTCTCTACTAAAAATACAAAAATTAGCCGGGCGTGGTGGCGCGC\n"
      + "GCCTGTAATCCCAGCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGGAGGCG\n"
      + "GAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCCAGCCTGGGCGACAGAGCGAGACT\n"
      + "CCGTCTCAAAAAGGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGGGAGGCC\n"
      + "GAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGACCAGCCTGGCCAACATGGTGAAAC\n"
      + "CCCGTCTCTACTAAAAATACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCA\n"
      + "GCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGGAGGCGGAGGTTGCAGTGA\n"
      + "GCCGAGATCGCGCCACTGCACTCCAGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAAG\n"
      + "GCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGGGAGGCCGAGGCGGGCGGAT\n"
      + "CACCTGAGGTCAGGAGTTCGAGACCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTA\n"
      + "AAAATACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCAGCTACTCGGGAGG\n"
      + "CTGAGGCAGGAGAATCGCTTGAACCCGGGAGGCGGAGGTTGCAGTGAGCCGAGATCGCGC\n"
      + "CACTGCACTCCAGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAAGGCCGGGCGCGGTG\n"
      + "GCTCACGCCTGTAATCCCAGCACTTTGGGAGGCCGAGGCGGGCGGATCACCTGAGGTCAG\n"
      + "GAGTTCGAGACCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAATACAAAAAT\n"
      + "TAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCAGCTACTCGGGAGGCTGAGGCAGGAGA\n"
      + "ATCGCTTGAACCCGGGAGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCCAG\n"
      + "CCTGGGCGACAGAGCGAGACTCCGTCTCAAAAAGGCCGGGCGCGGTGGCTCACGCCTGTA\n"
      + "ATCCCAGCACTTTGGGAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGACCA\n"
      + "GCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAATACAAAAATTAGCCGGGCGTGG\n"
      + "TGGCGCGCGCCTGTAATCCCAGCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCC\n"
      + "GGGAGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCCAGCCTGGGCGACAGA\n"
      + "GCGAGACTCCGTCTCAAAAAGGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTT\n"
      + "GGGAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGACCAGCCTGGCCAACAT\n"
      + "GGTGAAACCCCGTCTCTACTAAAAATACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTG\n"
      + "TAATCCCAGCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGGAGGCGGAGGT\n"
      + "TGCAGTGAGCCGAGATCGCGCCACTGCACTCCAGCCTGGGCGACAGAGCGAGACTCCGTC\n"
      + "TCAAAAAGGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGGGAGGCCGAGGC\n"
      + "GGGCGGATCACCTGAGGTCAGGAGTTCGAGACCAGCCTGGCCAACATGGTGAAACCCCGT\n"
      + "CTCTACTAAAAATACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCAGCTAC\n"
      + "TCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGGAGGCGGAGGTTGCAGTGAGCCGA\n"
      + ">TWO IUB ambiguity codes\n"
      + "cttBtatcatatgctaKggNcataaaSatgtaaaDcDRtBggDtctttataattcBgtcg\n"
      + "tactDtDagcctatttSVHtHttKtgtHMaSattgWaHKHttttagacatWatgtRgaaa\n"
      + "NtactMcSMtYtcMgRtacttctWBacgaaatatagScDtttgaagacacatagtVgYgt\n"
      + "cattHWtMMWcStgttaggKtSgaYaaccWStcgBttgcgaMttBYatcWtgacaYcaga\n"
      + "gtaBDtRacttttcWatMttDBcatWtatcttactaBgaYtcttgttttttttYaaScYa\n"
      + "HgtgttNtSatcMtcVaaaStccRcctDaataataStcYtRDSaMtDttgttSagtRRca\n"
      + "tttHatSttMtWgtcgtatSSagactYaaattcaMtWatttaSgYttaRgKaRtccactt\n"
      + "tattRggaMcDaWaWagttttgacatgttctacaaaRaatataataaMttcgDacgaSSt\n"
      + "acaStYRctVaNMtMgtaggcKatcttttattaaaaagVWaHKYagtttttatttaacct\n"
      + "tacgtVtcVaattVMBcttaMtttaStgacttagattWWacVtgWYagWVRctDattBYt\n"
      + "gtttaagaagattattgacVatMaacattVctgtBSgaVtgWWggaKHaatKWcBScSWa\n"
      + "accRVacacaaactaccScattRatatKVtactatatttHttaagtttSKtRtacaaagt\n"
      + "RDttcaaaaWgcacatWaDgtDKacgaacaattacaRNWaatHtttStgttattaaMtgt\n"
      + "tgDcgtMgcatBtgcttcgcgaDWgagctgcgaggggVtaaScNatttacttaatgacag\n"
      + "cccccacatYScaMgtaggtYaNgttctgaMaacNaMRaacaaacaKctacatagYWctg\n"
      + "ttWaaataaaataRattagHacacaagcgKatacBttRttaagtatttccgatctHSaat\n"
      + "actcNttMaagtattMtgRtgaMgcataatHcMtaBSaRattagttgatHtMttaaKagg\n"
      + "YtaaBataSaVatactWtataVWgKgttaaaacagtgcgRatatacatVtHRtVYataSa\n"
      + "KtWaStVcNKHKttactatccctcatgWHatWaRcttactaggatctataDtDHBttata\n"
      + "aaaHgtacVtagaYttYaKcctattcttcttaataNDaaggaaaDYgcggctaaWSctBa\n"
      + "aNtgctggMBaKctaMVKagBaactaWaDaMaccYVtNtaHtVWtKgRtcaaNtYaNacg\n"
      + "gtttNattgVtttctgtBaWgtaattcaagtcaVWtactNggattctttaYtaaagccgc\n"
      + "tcttagHVggaYtgtNcDaVagctctctKgacgtatagYcctRYHDtgBattDaaDgccK\n"
      + "tcHaaStttMcctagtattgcRgWBaVatHaaaataYtgtttagMDMRtaataaggatMt\n"
      + "ttctWgtNtgtgaaaaMaatatRtttMtDgHHtgtcattttcWattRSHcVagaagtacg\n"
      + "ggtaKVattKYagactNaatgtttgKMMgYNtcccgSKttctaStatatNVataYHgtNa\n"
      + "BKRgNacaactgatttcctttaNcgatttctctataScaHtataRagtcRVttacDSDtt\n"
      + "aRtSatacHgtSKacYagttMHtWataggatgactNtatSaNctataVtttRNKtgRacc\n"
      + "tttYtatgttactttttcctttaaacatacaHactMacacggtWataMtBVacRaSaatc\n"
      + "cgtaBVttccagccBcttaRKtgtgcctttttRtgtcagcRttKtaaacKtaaatctcac\n"
      + "aattgcaNtSBaaccgggttattaaBcKatDagttactcttcattVtttHaaggctKKga\n"
      + "tacatcBggScagtVcacattttgaHaDSgHatRMaHWggtatatRgccDttcgtatcga\n"
      + "aacaHtaagttaRatgaVacttagattVKtaaYttaaatcaNatccRttRRaMScNaaaD\n"
      + "gttVHWgtcHaaHgacVaWtgttScactaagSgttatcttagggDtaccagWattWtRtg\n"
      + "ttHWHacgattBtgVcaYatcggttgagKcWtKKcaVtgaYgWctgYggVctgtHgaNcV\n"
      + "taBtWaaYatcDRaaRtSctgaHaYRttagatMatgcatttNattaDttaattgttctaa\n"
      + "ccctcccctagaWBtttHtBccttagaVaatMcBHagaVcWcagBVttcBtaYMccagat\n"
      + "gaaaaHctctaacgttagNWRtcggattNatcRaNHttcagtKttttgWatWttcSaNgg\n"
      + "gaWtactKKMaacatKatacNattgctWtatctaVgagctatgtRaHtYcWcttagccaa\n"
      + "tYttWttaWSSttaHcaaaaagVacVgtaVaRMgattaVcDactttcHHggHRtgNcctt\n"
      + "tYatcatKgctcctctatVcaaaaKaaaagtatatctgMtWtaaaacaStttMtcgactt\n"
      + "taSatcgDataaactaaacaagtaaVctaggaSccaatMVtaaSKNVattttgHccatca\n"
      + "cBVctgcaVatVttRtactgtVcaattHgtaaattaaattttYtatattaaRSgYtgBag\n"
      + "aHSBDgtagcacRHtYcBgtcacttacactaYcgctWtattgSHtSatcataaatataHt\n"
      + "cgtYaaMNgBaatttaRgaMaatatttBtttaaaHHKaatctgatWatYaacttMctctt\n"
      + "ttVctagctDaaagtaVaKaKRtaacBgtatccaaccactHHaagaagaaggaNaaatBW\n"
      + "attccgStaMSaMatBttgcatgRSacgttVVtaaDMtcSgVatWcaSatcttttVatag\n"
      + "ttactttacgatcaccNtaDVgSRcgVcgtgaacgaNtaNatatagtHtMgtHcMtagaa\n"
      + "attBgtataRaaaacaYKgtRccYtatgaagtaataKgtaaMttgaaRVatgcagaKStc\n"
      + "tHNaaatctBBtcttaYaBWHgtVtgacagcaRcataWctcaBcYacYgatDgtDHccta\n"
      + "aagacYRcaggattHaYgtKtaatgcVcaataMYacccatatcacgWDBtgaatcBaata\n"
      + "cKcttRaRtgatgaBDacggtaattaaYtataStgVHDtDctgactcaaatKtacaatgc\n"
      + "gYatBtRaDatHaactgtttatatDttttaaaKVccYcaaccNcBcgHaaVcattHctcg\n"
      + "attaaatBtatgcaaaaatYMctSactHatacgaWacattacMBgHttcgaatVaaaaca\n"
      + "BatatVtctgaaaaWtctRacgBMaatSgRgtgtcgactatcRtattaScctaStagKga\n"
      + "DcWgtYtDDWKRgRtHatRtggtcgaHgggcgtattaMgtcagccaBggWVcWctVaaat\n"
      + "tcgNaatcKWagcNaHtgaaaSaaagctcYctttRVtaaaatNtataaccKtaRgtttaM\n"
      + "tgtKaBtRtNaggaSattHatatWactcagtgtactaKctatttgRYYatKatgtccgtR\n"
      + "tttttatttaatatVgKtttgtatgtNtataRatWYNgtRtHggtaaKaYtKSDcatcKg\n"
      + "taaYatcSRctaVtSMWtVtRWHatttagataDtVggacagVcgKWagBgatBtaaagNc\n"
      + "aRtagcataBggactaacacRctKgttaatcctHgDgttKHHagttgttaatgHBtatHc\n"
      + "DaagtVaBaRccctVgtgDtacRHSctaagagcggWYaBtSaKtHBtaaactYacgNKBa\n"
      + "VYgtaacttagtVttcttaatgtBtatMtMtttaattaatBWccatRtttcatagVgMMt\n"
      + "agctStKctaMactacDNYgKYHgaWcgaHgagattacVgtttgtRaSttaWaVgataat\n"
      + "gtgtYtaStattattMtNgWtgttKaccaatagNYttattcgtatHcWtctaaaNVYKKt\n"
      + "tWtggcDtcgaagtNcagatacgcattaagaccWctgcagcttggNSgaNcHggatgtVt\n"
      + "catNtRaaBNcHVagagaaBtaaSggDaatWaatRccaVgggStctDaacataKttKatt\n"
      + "tggacYtattcSatcttagcaatgaVBMcttDattctYaaRgatgcattttNgVHtKcYR\n"
      + "aatRKctgtaaacRatVSagctgtWacBtKVatctgttttKcgtctaaDcaagtatcSat\n"
      + "aWVgcKKataWaYttcccSaatgaaaacccWgcRctWatNcWtBRttYaattataaNgac\n"
      + "acaatagtttVNtataNaYtaatRaVWKtBatKagtaatataDaNaaaaataMtaagaaS\n"
      + "tccBcaatNgaataWtHaNactgtcDtRcYaaVaaaaaDgtttRatctatgHtgttKtga\n"
      + "aNSgatactttcgagWaaatctKaaDaRttgtggKKagcDgataaattgSaacWaVtaNM\n"
      + "acKtcaDaaatttctRaaVcagNacaScRBatatctRatcctaNatWgRtcDcSaWSgtt\n"
      + "RtKaRtMtKaatgttBHcYaaBtgatSgaSWaScMgatNtctcctatttctYtatMatMt\n"
      + "RRtSaattaMtagaaaaStcgVgRttSVaScagtgDtttatcatcatacRcatatDctta\n"
      + "tcatVRtttataaHtattcYtcaaaatactttgVctagtaaYttagatagtSYacKaaac\n"
      + "gaaKtaaatagataatSatatgaaatSgKtaatVtttatcctgKHaatHattagaaccgt\n"
      + "YaaHactRcggSBNgtgctaaBagBttgtRttaaattYtVRaaaattgtaatVatttctc\n"
      + "ttcatgBcVgtgKgaHaaatattYatagWacNctgaaMcgaattStagWaSgtaaKagtt\n"
      + "ttaagaDgatKcctgtaHtcatggKttVDatcaaggtYcgccagNgtgcVttttagagat\n"
      + "gctaccacggggtNttttaSHaNtatNcctcatSaaVgtactgBHtagcaYggYVKNgta\n"
      + "KBcRttgaWatgaatVtagtcgattYgatgtaatttacDacSctgctaaaStttaWMagD\n"
      + "aaatcaVYctccgggcgaVtaaWtStaKMgDtttcaaMtVgBaatccagNaaatcYRMBg\n"
      + "gttWtaaScKttMWtYataRaDBMaDataatHBcacDaaKDactaMgagttDattaHatH\n"
      + "taYatDtattDcRNStgaatattSDttggtattaaNSYacttcDMgYgBatWtaMagact\n"
      + "VWttctttgYMaYaacRgHWaattgRtaagcattctMKVStatactacHVtatgatcBtV\n"
      + "NataaBttYtSttacKgggWgYDtgaVtYgatDaacattYgatggtRDaVDttNactaSa\n"
      + "MtgNttaacaaSaBStcDctaccacagacgcaHatMataWKYtaYattMcaMtgSttDag\n"
      + "cHacgatcaHttYaKHggagttccgatYcaatgatRaVRcaagatcagtatggScctata\n"
      + "ttaNtagcgacgtgKaaWaactSgagtMYtcttccaKtStaacggMtaagNttattatcg\n"
      + "tctaRcactctctDtaacWYtgaYaSaagaWtNtatttRacatgNaatgttattgWDDcN\n"
      + "aHcctgaaHacSgaataaRaataMHttatMtgaSDSKatatHHaNtacagtccaYatWtc\n"
      + "actaactatKDacSaStcggataHgYatagKtaatKagStaNgtatactatggRHacttg\n"
      + "tattatgtDVagDVaRctacMYattDgtttYgtctatggtKaRSttRccRtaaccttaga\n"
      + "gRatagSaaMaacgcaNtatgaaatcaRaagataatagatactcHaaYKBctccaagaRa\n"
      + "BaStNagataggcgaatgaMtagaatgtcaKttaaatgtaWcaBttaatRcggtgNcaca\n"
      + "aKtttScRtWtgcatagtttWYaagBttDKgcctttatMggNttattBtctagVtacata\n"
      + "aaYttacacaaRttcYtWttgHcaYYtaMgBaBatctNgcDtNttacgacDcgataaSat\n"
      + "YaSttWtcctatKaatgcagHaVaacgctgcatDtgttaSataaaaYSNttatagtaNYt\n"
      + "aDaaaNtggggacttaBggcHgcgtNtaaMcctggtVtaKcgNacNtatVaSWctWtgaW\n"
      + "cggNaBagctctgaYataMgaagatBSttctatacttgtgtKtaattttRagtDtacata\n"
      + "tatatgatNHVgBMtKtaKaNttDHaagatactHaccHtcatttaaagttVaMcNgHata\n"
      + "tKtaNtgYMccttatcaaNagctggacStttcNtggcaVtattactHaSttatgNMVatt\n"
      + "MMDtMactattattgWMSgtHBttStStgatatRaDaagattttctatMtaaaaaggtac\n"
      + "taaVttaSacNaatactgMttgacHaHRttgMacaaaatagttaatatWKRgacDgaRta\n"
      + "tatttattatcYttaWtgtBRtWatgHaaattHataagtVaDtWaVaWtgStcgtMSgaS\n"
      + "RgMKtaaataVacataatgtaSaatttagtcgaaHtaKaatgcacatcggRaggSKctDc\n"
      + "agtcSttcccStYtccRtctctYtcaaKcgagtaMttttcRaYDttgttatctaatcata\n"
      + "NctctgctatcaMatactataggDaHaaSttMtaDtcNatataattctMcStaaBYtaNa\n"
      + "gatgtaatHagagSttgWHVcttatKaYgDctcttggtgttMcRaVgSgggtagacaata\n"
      + "aDtaattSaDaNaHaBctattgNtaccaaRgaVtKNtaaYggHtaKKgHcatctWtctDt\n"
      + "ttctttggSDtNtaStagttataaacaattgcaBaBWggHgcaaaBtYgctaatgaaatW\n"
      + "cDcttHtcMtWWattBHatcatcaaatctKMagtDNatttWaBtHaaaNgMttaaStagt\n"
      + "tctctaatDtcRVaYttgttMtRtgtcaSaaYVgSWDRtaatagctcagDgcWWaaaBaa\n"
      + "RaBctgVgggNgDWStNaNBKcBctaaKtttDcttBaaggBttgaccatgaaaNgttttt\n"
      + "tttatctatgttataccaaDRaaSagtaVtDtcaWatBtacattaWacttaSgtattggD\n"
      + "gKaaatScaattacgWcagKHaaccaYcRcaRttaDttRtttHgaHVggcttBaRgtccc\n"
      + "tDatKaVtKtcRgYtaKttacgtatBtStaagcaattaagaRgBagSaattccSWYttta\n"
      + "ttVaataNctgHgttaaNBgcVYgtRtcccagWNaaaacaDNaBcaaaaRVtcWMgBagM\n"
      + "tttattacgDacttBtactatcattggaaatVccggttRttcatagttVYcatYaSHaHc\n"
      + "ttaaagcNWaHataaaRWtctVtRYtagHtaaaYMataHYtNBctNtKaatattStgaMc\n"
      + "BtRgctaKtgcScSttDgYatcVtggaaKtaagatWccHccgKYctaNNctacaWctttt\n"
      + "gcRtgtVcgaKttcMRHgctaHtVaataaDtatgKDcttatBtDttggNtacttttMtga\n"
      + "acRattaaNagaactcaaaBBVtcDtcgaStaDctgaaaSgttMaDtcgttcaccaaaag\n"
      + "gWtcKcgSMtcDtatgtttStaaBtatagDcatYatWtaaaBacaKgcaDatgRggaaYc\n"
      + "taRtccagattDaWtttggacBaVcHtHtaacDacYgtaatataMagaatgHMatcttat\n"
      + "acgtatttttatattacHactgttataMgStYaattYaccaattgagtcaaattaYtgta\n"
      + "tcatgMcaDcgggtcttDtKgcatgWRtataatatRacacNRBttcHtBgcRttgtgcgt\n"
      + "catacMtttBctatctBaatcattMttMYgattaaVYatgDaatVagtattDacaacDMa\n"
      + "tcMtHcccataagatgBggaccattVWtRtSacatgctcaaggggYtttDtaaNgNtaaB\n"
      + "atggaatgtctRtaBgBtcNYatatNRtagaacMgagSaSDDSaDcctRagtVWSHtVSR\n"
      + "ggaacaBVaccgtttaStagaacaMtactccagtttVctaaRaaHttNcttagcaattta\n"
      + "ttaatRtaaaatctaacDaBttggSagagctacHtaaRWgattcaaBtctRtSHaNtgta\n"
      + "cattVcaHaNaagtataccacaWtaRtaaVKgMYaWgttaKggKMtKcgWatcaDatYtK\n"
      + "SttgtacgaccNctSaattcDcatcttcaaaDKttacHtggttHggRRaRcaWacaMtBW\n"
      + "VHSHgaaMcKattgtaRWttScNattBBatYtaNRgcggaagacHSaattRtttcYgacc\n"
      + "BRccMacccKgatgaacttcgDgHcaaaaaRtatatDtatYVtttttHgSHaSaatagct\n"
      + "NYtaHYaVYttattNtttgaaaYtaKttWtctaNtgagaaaNctNDctaaHgttagDcRt\n"
      + "tatagccBaacgcaRBtRctRtggtaMYYttWtgataatcgaataattattataVaaaaa\n"
      + "ttacNRVYcaaMacNatRttcKatMctgaagactaattataaYgcKcaSYaatMNctcaa\n"
      + "cgtgatttttBacNtgatDccaattattKWWcattttatatatgatBcDtaaaagttgaa\n"
      + "VtaHtaHHtBtataRBgtgDtaataMttRtDgDcttattNtggtctatctaaBcatctaR\n"
      + "atgNacWtaatgaagtcMNaacNgHttatactaWgcNtaStaRgttaaHacccgaYStac\n"
      + "aaaatWggaYaWgaattattcMaactcBKaaaRVNcaNRDcYcgaBctKaacaaaaaSgc\n"
      + "tccYBBHYaVagaatagaaaacagYtctVccaMtcgtttVatcaatttDRtgWctagtac\n"
      + "RttMctgtDctttcKtWttttataaatgVttgBKtgtKWDaWagMtaaagaaattDVtag\n"
      + "gttacatcatttatgtcgMHaVcttaBtVRtcgtaYgBRHatttHgaBcKaYWaatcNSc\n"
      + "tagtaaaaatttacaatcactSWacgtaatgKttWattagttttNaggtctcaagtcact\n"
      + "attcttctaagKggaataMgtttcataagataaaaatagattatDgcBVHWgaBKttDgc\n"
      + "atRHaagcaYcRaattattatgtMatatattgHDtcaDtcaaaHctStattaatHaccga\n"
      + "cNattgatatattttgtgtDtRatagSacaMtcRtcattcccgacacSattgttKaWatt\n"
      + "NHcaacttccgtttSRtgtctgDcgctcaaMagVtBctBMcMcWtgtaacgactctcttR\n"
      + "ggRKSttgYtYatDccagttDgaKccacgVatWcataVaaagaataMgtgataaKYaaat\n"
      + "cHDaacgataYctRtcYatcgcaMgtNttaBttttgatttaRtStgcaacaaaataccVg\n"
      + "aaDgtVgDcStctatatttattaaaaRKDatagaaagaKaaYYcaYSgKStctccSttac\n"
      + "agtcNactttDVttagaaagMHttRaNcSaRaMgBttattggtttaRMggatggcKDgWR\n"
      + "tNaataataWKKacttcKWaaagNaBttaBatMHtccattaacttccccYtcBcYRtaga\n"
      + "ttaagctaaYBDttaNtgaaaccHcaRMtKtaaHMcNBttaNaNcVcgVttWNtDaBatg\n"
      + "ataaVtcWKcttRggWatcattgaRagHgaattNtatttctctattaattaatgaDaaMa\n"
      + "tacgttgggcHaYVaaNaDDttHtcaaHtcVVDgBVagcMacgtgttaaBRNtatRtcag\n"
      + "taagaggtttaagacaVaaggttaWatctccgtVtaDtcDatttccVatgtacNtttccg\n"
      + "tHttatKgScBatgtVgHtYcWagcaKtaMYaaHgtaattaSaHcgcagtWNaatNccNN\n"
      + "YcacgVaagaRacttctcattcccRtgtgtaattagcSttaaStWaMtctNNcSMacatt\n"
      + "ataaactaDgtatWgtagtttaagaaaattgtagtNagtcaataaatttgatMMYactaa\n"
      + "tatcggBWDtVcYttcDHtVttatacYaRgaMaacaStaatcRttttVtagaDtcacWat\n"
      + "ttWtgaaaagaaagNRacDtttStVatBaDNtaactatatcBSMcccaSttccggaMatg\n"
      + "attaaWatKMaBaBatttgataNctgttKtVaagtcagScgaaaDggaWgtgttttKtWt\n"
      + "atttHaatgtagttcactaaKMagttSYBtKtaYgaactcagagRtatagtVtatcaaaW\n"
      + "YagcgNtaDagtacNSaaYDgatBgtcgataacYDtaaactacagWDcYKaagtttatta\n"
      + "gcatcgagttKcatDaattgattatDtcagRtWSKtcgNtMaaaaacaMttKcaWcaaSV\n"
      + "MaaaccagMVtaMaDtMaHaBgaacataBBVtaatVYaNSWcSgNtDNaaKacacBttta\n"
      + "tKtgtttcaaHaMctcagtaacgtcgYtactDcgcctaNgagagcYgatattttaaattt\n"
      + "ccattttacatttDaaRctattttWctttacgtDatYtttcagacgcaaVttagtaaKaa\n"
      + "aRtgVtccataBggacttatttgtttaWNtgttVWtaWNVDaattgtatttBaagcBtaa\n"
      + "BttaaVatcHcaVgacattccNggtcgacKttaaaRtagRtctWagaYggtgMtataatM\n"
      + "tgaaRttattttgWcttNtDRRgMDKacagaaaaggaaaRStcccagtYccVattaNaaK\n"
      + "StNWtgacaVtagaagcttSaaDtcacaacgDYacWDYtgtttKatcVtgcMaDaSKStV\n"
      + "cgtagaaWaKaagtttcHaHgMgMtctataagBtKaaaKKcactggagRRttaagaBaaN\n"
      + "atVVcgRcKSttDaactagtSttSattgttgaaRYatggttVttaataaHttccaagDtg\n"
      + "atNWtaagHtgcYtaactRgcaatgMgtgtRaatRaNaacHKtagactactggaatttcg\n"
      + "ccataacgMctRgatgttaccctaHgtgWaYcactcacYaattcttaBtgacttaaacct\n"
      + "gYgaWatgBttcttVttcgttWttMcNYgtaaaatctYgMgaaattacNgaHgaacDVVM\n"
      + "tttggtHtctaaRgtacagacgHtVtaBMNBgattagcttaRcttacaHcRctgttcaaD\n"
      + "BggttKaacatgKtttYataVaNattccgMcgcgtagtRaVVaattaKaatggttRgaMc\n"
      + "agtatcWBttNtHagctaatctagaaNaaacaYBctatcgcVctBtgcaaagDgttVtga\n"
      + "HtactSNYtaaNccatgtgDacgaVtDcgKaRtacDcttgctaagggcagMDagggtBWR\n"
      + "tttSgccttttttaacgtcHctaVtVDtagatcaNMaVtcVacatHctDWNaataRgcgt\n"
      + "aVHaggtaaaaSgtttMtattDgBtctgatSgtRagagYtctSaKWaataMgattRKtaa\n"
      + "catttYcgtaacacattRWtBtcggtaaatMtaaacBatttctKagtcDtttgcBtKYYB\n"
      + "aKttctVttgttaDtgattttcttccacttgSaaacggaaaNDaattcYNNaWcgaaYat\n"
      + "tttMgcBtcatRtgtaaagatgaWtgaccaYBHgaatagataVVtHtttVgYBtMctaMt\n"
      + "cctgaDcYttgtccaaaRNtacagcMctKaaaggatttacatgtttaaWSaYaKttBtag\n"
      + "DacactagctMtttNaKtctttcNcSattNacttggaacaatDagtattRtgSHaataat\n"
      + "gccVgacccgatactatccctgtRctttgagaSgatcatatcgDcagWaaHSgctYYWta\n"
      + "tHttggttctttatVattatcgactaagtgtagcatVgtgHMtttgtttcgttaKattcM\n"
      + "atttgtttWcaaStNatgtHcaaaDtaagBaKBtRgaBgDtSagtatMtaacYaatYtVc\n"
      + "KatgtgcaacVaaaatactKcRgtaYtgtNgBBNcKtcttaccttKgaRaYcaNKtactt\n"
      + "tgagSBtgtRagaNgcaaaNcacagtVtttHWatgttaNatBgtttaatNgVtctgaata\n"
      + "tcaRtattcttttttttRaaKcRStctcggDgKagattaMaaaKtcaHacttaataataK\n"
      + "taRgDtKVBttttcgtKaggHHcatgttagHggttNctcgtatKKagVagRaaaggaaBt\n"
      + "NatttVKcRttaHctaHtcaaatgtaggHccaBataNaNaggttgcWaatctgatYcaaa\n"
      + "HaatWtaVgaaBttagtaagaKKtaaaKtRHatMaDBtBctagcatWtatttgWttVaaa\n"
      + "ScMNattRactttgtYtttaaaagtaagtMtaMaSttMBtatgaBtttaKtgaatgagYg\n"
      + "tNNacMtcNRacMMHcttWtgtRtctttaacaacattattcYaMagBaacYttMatcttK\n"
      + "cRMtgMNccattaRttNatHaHNaSaaHMacacaVaatacaKaSttHatattMtVatWga\n"
      + "ttttttaYctttKttHgScWaacgHtttcaVaaMgaacagNatcgttaacaaaaagtaca\n"
      + "HBNaattgttKtcttVttaaBtctgctacgBgcWtttcaggacacatMgacatcccagcg\n"
      + "gMgaVKaBattgacttaatgacacacaaaaaatRKaaBctacgtRaDcgtagcVBaacDS\n"
      + "BHaaaaSacatatacagacRNatcttNaaVtaaaataHattagtaaaaSWccgtatWatg\n"
      + "gDttaactattgcccatcttHaSgYataBttBaactattBtcHtgatcaataSttaBtat\n"
      + "KSHYttWggtcYtttBttaataccRgVatStaHaKagaatNtagRMNgtcttYaaSaact\n"
      + "cagDSgagaaYtMttDtMRVgWKWtgMaKtKaDttttgactatacataatcNtatNaHat\n"
      + "tVagacgYgatatatttttgtStWaaatctWaMgagaRttRatacgStgattcttaagaD\n"
      + "taWccaaatRcagcagaaNKagtaaDggcgccBtYtagSBMtactaaataMataBSacRM\n"
      + "gDgattMMgtcHtcaYDtRaDaacggttDaggcMtttatgttaNctaattaVacgaaMMt\n"
      + "aatDccSgtattgaRtWWaccaccgagtactMcgVNgctDctaMScatagcgtcaactat\n"
      + "acRacgHRttgctatttaatgaattataYKttgtaagWgtYttgcHgMtaMattWaWVta\n"
      + "RgcttgYgttBHtYataSccStBtgtagMgtDtggcVaaSBaatagDttgBgtctttctc\n"
      + "attttaNagtHKtaMWcYactVcgcgtatMVtttRacVagDaatcttgctBBcRDgcaac\n"
      + "KttgatSKtYtagBMagaRtcgBattHcBWcaactgatttaatttWDccatttatcgagS\n"
      + "KaWttataHactaHMttaatHtggaHtHagaatgtKtaaRactgtttMatacgatcaagD\n"
      + "gatKaDctataMggtHDtggHacctttRtatcttYattttgacttgaaSaataaatYcgB\n"
      + "aaaaccgNatVBttMacHaKaataagtatKgtcaagactcttaHttcggaattgttDtct\n"
      + "aaccHttttWaaatgaaatataaaWattccYDtKtaaaacggtgaggWVtctattagtga\n"
      + "ctattaagtMgtttaagcatttgSgaaatatccHaaggMaaaattttcWtatKctagDtY\n"
      + "tMcctagagHcactttactatacaaacattaacttaHatcVMYattYgVgtMttaaRtga\n"
      + "aataaDatcaHgtHHatKcDYaatcttMtNcgatYatgSaMaNtcttKcWataScKggta\n"
      + "tcttacgcttWaaagNatgMgHtctttNtaacVtgttcMaaRatccggggactcMtttaY\n"
      + "MtcWRgNctgNccKatcttgYDcMgattNYaRagatHaaHgKctcataRDttacatBatc\n"
      + "cattgDWttatttaWgtcggagaaaaatacaatacSNtgggtttccttacSMaagBatta\n"
      + "caMaNcactMttatgaRBacYcYtcaaaWtagctSaacttWgDMHgaggatgBVgcHaDt\n"
      + "ggaactttggtcNatNgtaKaBcccaNtaagttBaacagtatacDYttcctNgWgcgSMc\n"
      + "acatStctHatgRcNcgtacacaatRttMggaNKKggataaaSaYcMVcMgtaMaHtgat\n"
      + "tYMatYcggtcttcctHtcDccgtgRatcattgcgccgatatMaaYaataaYSggatagc\n"
      + "gcBtNtaaaScaKgttBgagVagttaKagagtatVaactaSacWactSaKatWccaKaaa\n"
      + "atBKgaaKtDMattttgtaaatcRctMatcaaMagMttDgVatggMaaWgttcgaWatga\n"
      + "aatttgRtYtattaWHKcRgctacatKttctaccaaHttRatctaYattaaWatVNccat\n"
      + "NgagtcKttKataStRaatatattcctRWatDctVagttYDgSBaatYgttttgtVaatt\n"
      + "taatagcagMatRaacttBctattgtMagagattaaactaMatVtHtaaatctRgaaaaa\n"
      + "aaatttWacaacaYccYDSaattMatgaccKtaBKWBattgtcaagcHKaagttMMtaat\n"
      + "ttcKcMagNaaKagattggMagaggtaatttYacatcWaaDgatMgKHacMacgcVaaca\n"
      + "DtaDatatYggttBcgtatgWgaSatttgtagaHYRVacaRtctHaaRtatgaactaata\n"
      + "tctSSBgggaaHMWtcaagatKgagtDaSatagttgattVRatNtctMtcSaagaSHaat\n"
      + "aNataataRaaRgattctttaataaagWaRHcYgcatgtWRcttgaaggaMcaataBRaa\n"
      + "ccagStaaacNtttcaatataYtaatatgHaDgcStcWttaacctaRgtYaRtataKtgM\n"
      + "ttttatgactaaaatttacYatcccRWtttHRtattaaatgtttatatttgttYaatMca\n"
      + "RcSVaaDatcgtaYMcatgtagacatgaaattgRtcaaYaaYtRBatKacttataccaNa\n"
      + "aattVaBtctggacaagKaaYaaatatWtMtatcYaaVNtcgHaactBaagKcHgtctac\n"
      + "aatWtaDtSgtaHcataHtactgataNctRgttMtDcDttatHtcgtacatcccaggStt\n"
      + "aBgtcacacWtccNMcNatMVaVgtccDYStatMaccDatggYaRKaaagataRatttHK\n"
      + "tSaaatDgataaacttaHgttgVBtcttVttHgDacgaKatgtatatNYataactctSat\n"
      + "atatattgcHRRYttStggaactHgttttYtttaWtatMcttttctatctDtagVHYgMR\n"
      + "BgtHttcctaatYRttKtaagatggaVRataKDctaMtKBNtMtHNtWtttYcVtattMc\n"
      + "gRaacMcctNSctcatttaaagDcaHtYccSgatgcaatYaaaaDcttcgtaWtaattct\n"
      + "cgttttScttggtaatctttYgtctaactKataHacctMctcttacHtKataacacagcN\n"
      + "RatgKatttttSaaatRYcgDttaMRcgaaattactMtgcgtaagcgttatBtttttaat\n"
      + "taagtNacatHgttcRgacKcBBtVgatKttcgaBaatactDRgtRtgaNacWtcacYtt\n"
      + "aaKcgttctHaKttaNaMgWgWaggtctRgaKgWttSttBtDcNtgtttacaaatYcDRt\n"
      + "gVtgcctattcNtctaaaDMNttttNtggctgagaVctDaacVtWccaagtaacacaNct\n"
      + "gaScattccDHcVBatcgatgtMtaatBgHaatDctMYgagaatgYWKcctaatNaStHa\n"
      + "aaKccgHgcgtYaaYtattgtStgtgcaaRtattaKatattagaWVtcaMtBagttatta\n"
      + "gNaWHcVgcaattttDcMtgtaRHVYtHtctgtaaaaHVtMKacatcgNaatttMatatg\n"
      + "ttgttactagWYtaRacgataKagYNKcattataNaRtgaacKaYgcaaYYacaNccHat\n"
      + "MatDcNgtHttRaWttagaaDcaaaaaatagggtKDtStaDaRtaVtHWKNtgtattVct\n"
      + "SVgRgataDaRaWataBgaagaaKtaataaYgDcaStaNgtaDaaggtattHaRaWMYaY\n"
      + "aWtggttHYgagVtgtgcttttcaaDKcagVcgttagacNaaWtagtaataDttctggtt\n"
      + "VcatcataaagtgKaaaNaMtaBBaattaatWaattgctHaVKaSgDaaVKaHtatatat\n"
      + "HatcatSBagNgHtatcHYMHgttDgtaHtBttWatcgtttaRaattgStKgSKNWKatc\n"
      + "agDtctcagatttctRtYtBatBgHHtKaWtgYBgacVVWaKtacKcDttKMaKaVcggt\n"
      + "gttataagaataaHaatattagtataatMHgttYgaRttagtaRtcaaVatacggtcMcg\n"
      + "agtaaRttacWgactKRYataaaagSattYaWgagatYagKagatgSaagKgttaatMgg\n"
      + "tataatgttWYttatgagaaacctNVataatHcccKtDctcctaatactggctHggaSag\n"
      + "gRtKHaWaattcgSatMatttagaggcYtctaMcgctcataSatatgRagacNaaDagga\n"
      + "VBagaYttKtacNaKgtSYtagttggaWcatcWttaatctatgaVtcgtgtMtatcaYcg\n"
      + "tRccaaYgDctgcMgtgtWgacWtgataacacgcgctBtgttaKtYDtatDcatcagKaV\n"
      + "MctaatcttgVcaaRgcRMtDcgattaHttcaNatgaatMtactacVgtRgatggaWttt\n"
      + "actaaKatgagSaaKggtaNtactVaYtaaKRagaacccacaMtaaMtKtatBcttgtaa\n"
      + "WBtMctaataaVcDaaYtcRHBtcgttNtaaHatttBNgRStVDattBatVtaagttaYa\n"
      + "tVattaagaBcacggtSgtVtatttaRattgatgtaHDKgcaatattKtggcctatgaWD\n"
      + "KRYcggattgRctatNgatacaatMNttctgtcRBYRaaaHctNYattcHtaWcaattct\n"
      + "BtMKtVgYataatMgYtcagcttMDataVtggRtKtgaatgccNcRttcaMtRgattaac\n"
      + "attRcagcctHtWMtgtDRagaKaBtgDttYaaaaKatKgatctVaaYaacWcgcatagB\n"
      + "VtaNtRtYRaggBaaBtgKgttacataagagcatgtRattccacttaccatRaaatgWgD\n"
      + "aMHaYVgVtaSctatcgKaatatattaDgacccYagtgtaYNaaatKcagtBRgagtcca\n"
      + "tgKgaaaccBgaagBtgSttWtacgatWHaYatcgatttRaaNRgcaNaKVacaNtDgat\n"
      + "tgHVaatcDaagcgtatgcNttaDataatcSataaKcaataaHWataBtttatBtcaKtK\n"
      + "tatagttaDgSaYctacaRatNtaWctSaatatttYaKaKtaccWtatcRagacttaYtt\n"
      + "VcKgSDcgagaagatccHtaattctSttatggtKYgtMaHagVaBRatttctgtRgtcta\n"
      + "tgggtaHKgtHacHtSYacgtacacHatacKaaBaVaccaDtatcSaataaHaagagaat\n"
      + "ScagactataaRttagcaaVcaHataKgDacatWccccaagcaBgagWatctaYttgaaa\n"
      + "tctVNcYtttWagHcgcgcDcVaaatgttKcHtNtcaatagtgtNRaactttttcaatgg\n"
      + "WgBcgDtgVgtttctacMtaaataaaRggaaacWaHttaRtNtgctaaRRtVBctYtVta\n"
      + "tDcattDtgaccYatagatYRKatNYKttNgcctagtaWtgaactaMVaacctgaStttc\n"
      + "tgaKVtaaVaRKDttVtVctaDNtataaaDtccccaagtWtcgatcactDgYaBcatcct\n"
      + "MtVtacDaaBtYtMaKNatNtcaNacgDatYcatcgcaRatWBgaacWttKttagYtaat\n"
      + "tcggttgSWttttDWctttacYtatatWtcatDtMgtBttgRtVDggttaacYtacgtac\n"
      + "atgaattgaaWcttMStaDgtatattgaDtcRBcattSgaaVBRgagccaaKtttcDgcg\n"
      + "aSMtatgWattaKttWtgDBMaggBBttBaatWttRtgcNtHcgttttHtKtcWtagHSt\n"
      + "aacagttgatatBtaWSaWggtaataaMttaKacDaatactcBttcaatatHttcBaaSa\n"
      + ">THREE Homo sapiens frequency\n"
      + "aagtccgatgagtttcaatcatgactgcgaggagatccatgcggtgtacctaaacctaca\n"
      + "tcgtatgtatttgctgacgttcattcttgatacataaagatccgatatcggtccactttg\n"
      + "tttaccaaaagccctaccttcgtaacgatggaaatgtgaatgagagtgaaatacacgatg\n"
      + "gggatattgccggtgagtacaagttagaccacacattagaactgacctatattcgtcatc\n"
      + "atagagatggagtatgaattgattctgcgaagtacactggctttacgagtatctagacgc\n"
      + "cgcggtatatctcccgtcaatactatgaaggtatatatatagaggctgaaaattcatgtt\n"
      + "caatcctctttctaagagtgagtgggagccccttctgttgtcggagtaaaaaggcattat\n"
      + "tcctcaaattgtcagaagcaaagtatacgtgatgtttgcttagaacaaaagagttacctt\n"
      + "agggtaggtaaatctcgattcaccgagagaagtgattttggcggtgtgcgattaattctt\n"
      + "ttgatgacagatctcattattttatatagctccctctttgtatttagagtttgcgtaggt\n"
      + "aacctggcaaaaccatatcccggggggagagtgcgctgaacattttatacgatgtgatta\n"
      + "ctcaaaggataaggttcgaggcctctatactcatggaactatcttataattataatggat\n"
      + "cgtggctcattccacctatccaaacttctttgtgatctgatgctacgagtgtgaacaaac\n"
      + "gtacatcttctaaggaatttgggacgtttcatagctcgcatttcattcctgaaaacttaa\n"
      + "atatttttaaaaattgattctactgcgaggaactaaggtgtagacaagcccttagtaacc\n"
      + "ggtggatgtcgcttcagttttatagcaaacattattcaatttcagtcttgactgaaatta\n"
      + "gtttgttagtgttagaggtccatatgtcacatgcatatggtctagatgccattgtacagt\n"
      + "aataccttagattagtattagcggcatgcgtacttggatttcacttgtaagaatgagctt\n"
      + "aggacggtcgcctgtagggctgcaaataggaatacttacaatttttgatgacttgttagc\n"
      + "atatcgctatcacccataaaaaacctgatacttgatgagcgggtgattgagactatgtac\n"
      + "tgatataattcaatagctccaatagatgaaacagctatgcgcctatttatgtcaaataat\n"
      + "cgatgtgatacaagcttagagctgaacgagcgcgagtggaattagcggtgatctctatcc\n"
      + "taaaaagccacgaaatcgatcccagaagctaatacccgaggtgtcaagcttgagttcagt\n"
      + "taaatttgcatctcatgccccacgaagaatgggtagagagtttgaaggtgcttctggatt\n"
      + "ttcctaagtacgtggtaaaaatttgatgtaaatgaacacctcctaatggttgtgttaacc\n"
      + "acaaacccctgggtgaatctgattagccaacccagtgatctgatttcagttgtcaaatct\n"
      + "cttttttataactaccttttgtttccataatttaaccggatctcataatgaacaaacggg\n"
      + "tagaataatggtagcacatagcgagcttgtctattcagaaatatggcctactcagaatgt\n"
      + "attctccaaatcagtgttatgcgaaacgtaattttacgtgtaataatgatgatttcttat\n"
      + "cggttccttgtactacaatactcttgcccaacaaatactaagcataacagcaaaattcga\n"
      + "atccccctccttttaataaatggtttttcaatatagccgattcgtattcgttagtctttc\n"
      + "accaactattaacctggcatctaattaataaaatcaccaaaggactctataatatgacag\n"
      + "tcacttcggcctcttttaagacagttgattattgcaggtccgcaattgatggtgacatgc\n"
      + "acaattagttagaatccgactatggagacaattaacaattgtagtgcccatttggtccag\n"
      + "ttgacttcaaccacgagttataaaggtattttaatttatagtcgatagtaccaacaacaa\n"
      + "gcacaatcataattatgttagaaaacccagggggtaatgctctaaatccagctttaaggc\n"
      + "cagagtgcactatgaaatcgccattgatcattgtgtcattcgctgaacttggtgtctagg\n"
      + "aggtgccgagtgagaatatcagataccttatgaagcaacgattatatctggactagatca\n"
      + "tgatgatcggaataaaacattgaaataagtccttatcaaggagcataaacattttattta\n"
      + "atttatacttcgtaaataaattcagaattttttttcaagacattaatctgagtaaatgac\n"
      + "ggctagaaagggttcctactcgaatcgtagcctacgcatgtgggcagtaacctggcttgc\n"
      + "gtttttactgaaacaaaggttcaccggaaagaaggctgccacttttagcttcttgacgat\n"
      + "ctttagcgtcatatttttagattagtcgaaaaacggaaaacaaacttaacgaagctggtt\n"
      + "gcacggggtaccgagaaaccaaagagcaggacaactccttgatcgggaagaactgaaata\n"
      + "gacagctgtcattttcattggtcaacttatcaatataacgaccaccgtagtgacgcttgc\n"
      + "atgaaaatactgaggatgtaaactatagccagtcaggcccgcgtgttgactaattgatga\n"
      + "agcaaacaaaatagccggtattcgttaaaaggaacgggttgccagctacagatatactct\n"
      + "aggtatatcccaaacaagagacgtcctttggctgttgtaatcggtcataatacttgtcac\n"
      + "ataaacaagatcgctgaattaaacattaaacagttagtgatacacaatcgtggttggggc\n"
      + "tgggatgtgcaataaaaagtcatctatcgtctatcacagagcgacgtaaatttagacaaa\n"
      + "cattattatttcttgacaatggaatcgataagcgttcctctaacttggtatatatatctc\n"
      + "gaccccgggattccagccattcttgtatgaagatttaaccatttaactatgcatagttga\n"
      + "atggtaaggaaaatgatattgactgcaacagattttggatgcaaaaatatttgtgaatta\n"
      + "ttggttatatactggttgtatagcacaatcattaggtcctagaaggcatactcaacctca\n"
      + "gcgagagagctagcatgcataattgtaccgcccatattaatattcctgaaatgatttctt\n"
      + "acattacgcccaatttcagtcatcgaacacccccatcaatttacccgatagagaacgtga\n"
      + "tcatacgcaataccctatgcgaacgtccactctatagcgtctgtatacaatgattattcg\n"
      + "ttccatttacaacgttaagtaatttaaacttacataaggacaaggaaatccgcgaacctc\n"
      + "ctggaatgtatgagttatttatgcagttaacttcgtctcgaccggaactaaaggcgtcgt\n"
      + "acgaatgaaaggccacttttagaagagacctttgtatccattgtggagaatatcataaat\n"
      + "tcaagatggggtgtcatgctattcggtcctaaacattcttaatggctgttctattgttag\n"
      + "tctgatttaaaatggaaccatagcacgaatagttagatagggctcatacccctgtaacga\n"
      + "tctacaaatccttccccgggtgtgtgcgttagcgacggaaagttttacggtttgtgatca\n"
      + "aagaacactcacacgtcagattattacactgatacgaattatttcagtcgacagtaattg\n"
      + "aatagaaacttattaacgccagcacctgacacggtaagtaaggcaggtctgaactgtttg\n"
      + "actgtaaaaaaatggtaatatttttaaaaatcttgatttctatatcaaatgatgtgtagt\n"
      + "tttttctctgttattaaaatcccagtgcgcgaaatttagatcgttacgactcacgtacaa\n"
      + "gatcacacatcacacgcgttagcgaaagcggaatggctaatacagccctacgcaacgtag\n"
      + "tgggatcaacatatggacgaatttatgctcaatgagccaacctcccccgcattgcggttc\n"
      + "attttaaggcctgggtaacatctatcgtttagataatcaaaggaatccgactatgcaatt\n"
      + "gtctgacttcatccgctctcaagtccaatgcaggcgctacgtgtttctttaatcaatacc\n"
      + "atattgaaatcgtaatacgataattgttgctattgactacaggttatgaaaaaacttact\n"
      + "ttgcgggtacatgcatatttttgtaccacattattacgcgatatctctcagtgtactcta\n"
      + "aattaaaccctcttcgaacattttagttcctattcgtaaacacgtgctacgcggcaattt\n"
      + "gccggtcgtagaatggacaactccagttcaactgcatgtaactcatagctcgcgttagta\n"
      + "taaattgactagtagccatgggacaaagtaactagtcagcggaaaagatccctttaaaga\n"
      + "tatatgcaggttgcaagcataaagctcattgctcgaggtgcaccgtggtattccaaaagc\n"
      + "gtctctatcgtatcttctaattttgggccgtgagaatcgaaactactctgatttgctgca\n"
      + "cacgttaggtaatatcgcccattttcccgtataagctccgtacttatacgaactacacga\n"
      + "ccttttaagcattagccgctcatatcgtgattcgtgtacagatgagtctattaaaattac\n"
      + "agacatactccatatctcgctccttgaactttgaataatgcgctaacttgtactatgaat\n"
      + "aggcagaacccaactttcccgtttgcgtcaagcggggaaacgatacatgttgtcagattt\n"
      + "atgattatctagttttagatcacgtttaccgataatcggctgtggtctgagcagtcctac\n"
      + "actgagtatttacttcagcttcatatcggtccgaaaaaaggttgtgaccgaatgtcaaaa\n"
      + "tacggagtacgatgggcatcttttttcgagtcgcggttgcagggcagcaaaaggcttaaa\n"
      + "ccatttttacgatttttactatagcggtcatgaagtgcgaaactgcttgcaaattttcta\n"
      + "cacacattgtggctcttgtccttgaagcttatggcgaaaatttgaaacatagtataccag\n"
      + "ggaaagcgcgaattatttggtgactaatagtccgtgggtttgagccatatacctaacgcc\n"
      + "ataaactacgtggtgctttagatgcaatctaaacagaacagaaagcgtagcgctcatcag\n"
      + "cacagactaactttttcagtttgagtcgccggagggacttcgagacaagaacgcgtcaag\n"
      + "tcgcttgcgcggcacggattcgattgggcggctcaatcttgcctaatttctactattgtc\n"
      + "agctgtacgactgtactaagtgtatagccccaaataaaagaagtatcgatgcgtctttat\n"
      + "gaccaaaggtcttataattgaagcgcacttccgttcatcaaattaaatcctggcttaccc\n"
      + "gattctccggaagtctgacctagagattgacgacggccgcgtattattgagacctcttca\n"
      + "ggattaatcaataacgaagtagttgatctgtttggcgacgtaccttaagccgactccgct\n"
      + "acacgagtttctactaaaccaatgtagccttatgcttagatgaataccgtcctaattaga\n"
      + "tattccggcataacagcagtaaattatctgttcaatggacgaacattgaattgttagtat\n"
      + "tctacacaagtcaggcctcgtaaatattaggtaaggccgtgggataacctacgtgatatg\n"
      + "cttgagcttgcgttgcaagctctcgttaatcattaatttaggtgcgtgagggttaaacac\n"
      + "cagcatattctatatgctagacgtcttccttaaaggatcgtagtattataattaataata\n"
      + "agaaatatggttgacgtctagtcagcgggcatacgctgctctatatactggcattattca\n"
      + "aaacttgacggtaaaaaaacgaattttaaggcgctcacgtcgaatgagccgaactcatgg\n"
      + "gaaccaaaatgtcacagaaaacacctctttattgccaagcatgcaataaaaaaaatgtta\n"
      + "atagtacgtttacgacattttattttataataaagagaaactattacacctattgatatg\n"
      + "ataggacgtaaattaacgagtagcctgcatagaggcaaatgaggtttctacatggtatag\n"
      + "acctgatgctgaaacatcgatgagttttggtcccctcgctcgttgaaatctagtcattta\n"
      + "ctactgtctttcgagctattataccacttcactatgtggtgtttctttgctatgtatggg\n"
      + "gctagtcaaacatgatgactatagctacaactcagagagcgggcgtgttaagagtatctc\n"
      + "atgctagaactgcacgacgaacttgatacaaagtaacaacatttacgattccacaaggtg\n"
      + "actttgaagaaacatagtttaattctctgcttcgatcatttctataaaccggtaccatcg\n"
      + "cagcggatagatgcataacatttctactactccaggcatcttaaaacacacgtagtactt\n"
      + "cactagattaagacacgataagtgtataacttggcagtgggaagcaaggagattggcgaa\n"
      + "ctcctggcatctgttacgttttgttcaggctcggttgttgataatgtccgactcctgcca\n"
      + "tattgaagactcgctcgagggagatcgggattcgttgattataagtacacgtgttccgta\n"
      + "atactatgaggcagtgattcaaaatggcacttctgacttacatgactaggtattattacc\n"
      + "acggaagcgttaaaggcacactcttatggacttaagattgcaagtgccttcttctagcct\n"
      + "gaattcgcgggttcaacacaaactctctttagacatccgttgcctaaaggctgagacgta\n"
      + "ggggcaaccctttaactatgtactaaaaaactagttggtaatttaacaacgtgtccaatc\n"
      + "aagacgatgcaccaacgcggtgcgaaaatcgggttaagcaaacacaaataggaattgtga\n"
      + "taaaccccaccttgagaggtcgcaagaccaacctcgggaacaacggctctaagagaataa\n"
      + "cctaaatccggatgagtagactgtgtaactctctaaagggaagtgaaaaaaagctaagca\n"
      + "tacatttaggtctcctgcattgcattcaattgaatcgtttgtattatgagctgtacagta\n"
      + "gctatatcagctatagttatcccagaggaacaggtaaactagctctgagcgtgaaatccg\n"
      + "gatattagaacccctagatgggattgattctagctaatacaggcttatctggttttacag\n"
      + "ttatctagatgattggtaaggtgaaacgcttggtgccttccaccacttaaacaaaagtat\n"
      + "tgcccgggaagctattttctaggtattataaagtcgagcattaatatcaatttgacagta\n"
      + "aaggtctttcaccagcttcatatgccatagggcccatactcgatttaaattgaacggttt\n"
      + "aacgagtattggaactctcacttataactgagtagctatacgaaaaatctggtccatttc\n"
      + "cagaaatttattatcgatttgctgcttagtacccaggaagtgataacccttgaaggcaca\n"
      + "acactgtaataagttttcctgtcacatctgtaatattcggtcactacgcattcacgacta\n"
      + "aagataattactatactaattaaaagttcaatgttagggccgaatcatagtagaaattct\n"
      + "cgtctagcctaatcggacttacctatgggctgtgaggatttatcagtatgtggacaaaaa\n"
      + "tgctagagataggtatagttaaagtcaccatggtacatctatgtgaggaagtttgtagtt\n"
      + "cgcttctttagtccgggcgtttgggatgacaactactatacgtagagccgtactcaggat\n"
      + "tagatagtgtgaaagagtcaaataaaagggttaatattaatttaacgttgcaaatgtgtt\n"
      + "taggccaaacattaaccgttgtagggatattctaatacaggccttcaccgaaccctaatg\n"
      + "ataatctgtcttaataacattaaatgattgtctccgctacgagctcttagggcctcattt\n"
      + "taaatgactaatgtccaaagaagagactttcccaatttcaatctgtcacgtatagacggc\n"
      + "accttagtgagtcatatcattaagatagaagattatcaggagggaagtttctattatcaa\n"
      + "ccgttacgcaaccataaacttttaaatctcataatggcattgagatcaagagctttcatg\n"
      + "atggtaaagttcgtatgtgatgctggggagctagatatcggtataccacttcggttgtgg\n"
      + "taagcccgagtgggccgttagtaatattaatagacgattatccgacaatgcattcgctga\n"
      + "aataatcttacttaggagaaattaatgctatgagccaaaactatttatgtctgtcacatt\n"
      + "attgactaaagtatctatcgacaaaactgatgtccataagttgtagcagatagtcggtgt\n"
      + "atggtgtcaccaatgaaaacctcgagcgaaaaatgaattatagttatccaatttgagtaa\n"
      + "attgcctattatacagataggcttgtttagtcagataaggttccgcttgaggtgctctaa\n"
      + "cttagcgagagttagaaagcctagtgagaggcattttggtgccaaactccggctcgcatg\n"
      + "agtaggccagagagtcactttctttcgtcgaagaagttggtgaacagccttttgattagt\n"
      + "tgtttgtcttgtggctatgtgctactatataagttagaacgcaaactaatctaatcagca\n"
      + "aagtaaaataggaccttgaacgagacggggtacgccgttgaggctcgagatagtagataa\n"
      + "actagaggaatgtagataaaacattagctagggggtttagttactggattacataggaag\n"
      + "tgcaccatcacggtgtgggggttcgtacgtaaagtcgcatcaatattgtcagtggactta\n"
      + "acaagttcgtgcataatgaaatcctatacggactttgcatatctctaccgactcatctgg\n"
      + "tcgtctatgcgggtaattgtattgctccaagtggatgactattttggcgtcccagcacat\n"
      + "agtaaatgtaaatccttataatagcataagcaattattagactgcgtgaagtcttagtag\n"
      + "ttctcaagctttacgttgtatgtaaataactcacgtaatcagccgtccccaaatcaccat\n"
      + "tgaggtcattgaatgtacggagcactattatcaatgcggtatgcgattttctgagcgatt\n"
      + "attgttaaagacttagcgttgagccccggaacacttgattacagattctttaaggagtta\n"
      + "tccaaatatcattttaaataatagtagtatcgtgctttggacaataaaaaaagacccgtt\n"
      + "ctcttatgttgttttgcgacgtacttctctgatatatacttcaactatgaagattctatt\n"
      + "catcgataacccaggtatatttatatgcccgttcactgcgcagggcaaattatctacgga\n"
      + "caataatgacgtagttggacccggtaagaactaacgcttaatatgattaaggatgtatgc\n"
      + "cagtattatcttattatgtcagagtagaagtttctctgagattttccgtcgttgtggtac\n"
      + "accggatttggctctctttttagaactgagaactcggagtgtgtagtcttgtttccttca\n"
      + "atttatcaatatgcttttataccgccctcatcaactataacaggacgacaagttccgtct\n"
      + "tgctccatcatatactaccgatacaccaatcgtatcaagtttagtatacttgctttctct\n"
      + "cttctacagcttactcgcttgtccgagaagcggttggtgctcataaagttagtagtaaat\n"
      + "gtacaactagtagccagtccttacctgtttttacgactactacggacaccatgagataca\n"
      + "gaagttagtgctacaattataccattacatgctcaatatcgttgtcggccataagatcga\n"
      + "agagtgcatcacgcgtgtgaatacgtaaaatctaccatcccgtcaatgcacaaaaacaca\n"
      + "ctccccttgttgactaacatcttttacaagaggctaaatcattgtccaggatcgaatacc\n"
      + "ttgtgtacaatcgtcacccatcggaagaataccacttttccgatgtagtatgatttacaa\n"
      + "aaaacatctatgtgagtaggccaattgtagtagaatatattcatttgaccgtcattagcc\n"
      + "ttcttcttaggttgtgtacggatagtaggtacataaaccgtcgtgtggcatacgctgcga\n"
      + "tttcatacagctgccaacaccttttttaccaggctagagtcagaaaagttggagccatgt\n"
      + "taaatagttaccatcataaaccactgttgtctactagtctgatcagctttcatgcctgtg\n"
      + "caagcaatatggattctcacgtaatggtaacaactgttgcgttacttaggctggttaatt\n"
      + "tgtcagagtaataaatacatgtcttgttgtgtttcctaatcctcggaaagtacacaagcc\n"
      + "taggaataggaaaagtaaagctcttttattctgatagtgactaactcaggatctaaatac\n"
      + "gcgattatactaaccttcaccaaagctcaaaaatcatctgctggtgaccagttatagaca\n"
      + "gggtaattcaatatttaatgtctcccttaacatttcaccagcatggattgaagatagtat\n"
      + "aaagttttacatggcagtcattgtgtcacggttctatacaaattctgatagttagacggt\n"
      + "atttgaaatgtgcttctagcatggtatcttacacaactgaatgaacgactggagccgttc\n"
      + "gtatactatttgcgagcctcgagaccccgtttcctaatgttaacgaatatagtataatat\n"
      + "aaattgtgatatgaataacacaagtaactacagtttggacaattaattgttctaaactaa\n"
      + "aaatcattcacttcagatggcatagagttatggctactacacatataaagcggtatgtga\n"
      + "aacacccgttttagccggaaaccctctactgctcgggacaatgaatgatttccaaaatat\n"
      + "ggatgtgcagaattgttagtgtgactcaggtccaaatagacactttagtttcgtcaagtc\n"
      + "gttgcaaagtttaaaaccatcgcagcattctttatttggtctacattgagaaatgaaaaa\n"
      + "acgtgacagaaagtctagaagaactgtgaataatgtctattactgattaactagtaagac\n"
      + "attagtgcatctggtccactgaagcacccgcttggcgttaggcaatctctgtgaactgtc\n"
      + "gtggctgttccggtaatgtacgaaagcaagcctataggttgatcgagtcgcttcattaag\n"
      + "gtcaatttcacaatatccgatcacattgtgctaggttcgtcctttaccttgcttagtgct\n"
      + "gcatgtacggggtgtcatgacttgttatcggcagactctttatcccaagaatggataata\n"
      + "tgtacatggaaagtgtccataattaagtcccttcactgtaaagaatgactgccacgtgat\n"
      + "ccatgaggtctacagaaaccgacttacttgctttttgatcaacttaattatggattcata\n"
      + "aagttcagatatcggtacaattggtgtacaatatgaaattaatgaggaaacatggaaatc\n"
      + "tgaatgacagtgatagaaaagatccccatttgcccggtcagttcatgttacaccactcat\n"
      + "tagtactgtaagtgtttcgtcagcattgagatccacgatcatgtgtttatgccttcgaaa\n"
      + "ctggatgtacgacgatcgagacgaagaggtatatataacctaaatactaggtacgttgtt\n"
      + "agagagacgatgaaaattaatcgtcaatacgctggcgaacactgagggggacccaatgct\n"
      + "cttctcggtctaaaaaggaatgtgtcagaaattggtcagttcaaaagtagaccggatctt\n"
      + "tgcggagaacaattcacggaacgtagcgttgggaaatatcctttctaccacacatcggat\n"
      + "tttcgccctctcccattatttattgtgttctcacatagaattattgtttagacatccctc\n"
      + "gttgtatggagagttgcccgagcgtaaaggcataatccatataccgccgggtgagtgacc\n"
      + "tgaaattgtttttagttgggatttcgctatggattagcttacacgaagagattctaatgg\n"
      + "tactataggataattataatgctgcgtggcgcagtacaccgttacaaacgtcgttcgcat\n"
      + "atgtggctaacacggtgaaaatacctacatcgtatttgcaatttcggtcgtttcatagag\n"
      + "cgcattgaattactcaaaaattatatatgttgattatttgattagactgcgtggaaagaa\n"
      + "ggggtactcaagccatttgtaaaagctgcatctcgcttaagtttgagagcttacattagt\n"
      + "ctatttcagtcttctaggaaatgtctgtgtgagtggttgtcgtccataggtcactggcat\n"
      + "atgcgattcatgacatgctaaactaagaaagtagattactattaccggcatgcctaatgc\n"
      + "gattgcactgctatgaaggtgcggacgtcgcgcccatgtagccctgataataccaatact\n"
      + "tacatttggtcagcaattctgacattatacctagcacccataaatttactcagacttgag\n"
      + "gacaggctcttggagtcgatcttctgtttgtatgcatgtgatcatatagatgaataagcg\n"
      + "atgcgactagttagggcatagtatagatctgtgtatacagttcagctgaacgtccgcgag\n"
      + "tggaagtacagctgagatctatcctaaaatgcaaccatatcgttcacacatgatatgaac\n"
      + "ccagggggaaacattgagttcagttaaattggcagcgaatcccccaagaagaaggcggag\n"
      + "tgacgttgaacgggcttatggtttttcagtacttcctccgtataagttgagcgaaatgta\n"
      + "aacagaataatcgttgtgttaacaacattaaaatcgcggaatatgatgagaatacacagt\n"
      + "gtgagcatttcacttgtaaaatatctttggtagaacttactttgctttaaatatgttaaa\n"
      + "ccgatctaataatctacaaaacggtagattttgcctagcacattgcgtccttctctattc\n"
      + "agatagaggcaatactcagaaggttttatccaaagcactgtgttgactaacctaagtttt\n"
      + "agtctaataatcatgattgattataggtgccgtggactacatgactcgtccacaaataat\n"
      + "acttagcagatcagcaattggccaagcacccgacttttatttaatggttgtgcaatagtc\n"
      + "cagattcgtattcgggactctttcaaataatagtttcctggcatctaagtaagaaaagct\n"
      + "cataaggaagcgatattatgacacgctcttccgccgctgttttgaaacttgagtattgct\n"
      + "cgtccgaaattgagggtcacttcaaaatttactgagaagacgaagatcgactaaagttaa\n"
      + "aatgctagtccacagttggtcaagttgaattcatccacgagttatatagctattttaatt\n"
      + "tatagtcgagtgtacaaaaaacatccacaataagatttatcttagaataacaacccccgt\n"
      + "atcatcgaaatcctccgttatggcctgactcctcgagcttatagcatttgtgctggcgct\n"
      + "cttgccaggaacttgctcgcgaggtggtgacgagtgagatgatcagtttcattatgatga\n"
      + "tacgattttatcgcgactagttaatcatcatagcaagtaaaatttgaattatgtcattat\n"
      + "catgctccattaacaggttatttaattgatactgacgaaattttttcacaatgggttttc\n"
      + "tagaatttaatatcagtaattgaagccttcataggggtcctactagtatcctacacgacg\n"
      + "caggtccgcagtatcctggagggacgtgttactgattaaaagggtcaaaggaatgaaggc\n"
      + "tcacaatgttacctgcttcaccatagtgagccgatgagttttacattagtactaaatccc\n"
      + "aaatcatactttacgatgaggcttgctagcgctaaagagaatacatacaccaccacatag\n"
      + "aattgttagcgatgatatcaaatagactcctggaagtgtcagggggaaactgttcaatat\n"
      + "ttcgtccacaggactgaccaggcatggaaaagactgacgttggaaactataccatctcac\n"
      + "gcccgacgcttcactaattgatgatccaaaaaatatagcccggattcctgattagcaaag\n"
      + "ggttcacagagaaagatattatcgacgtatatcccaaaaaacagacgtaatgtgcatctt\n"
      + "cgaatcgggatgaatacttgtatcataaaaatgtgacctctagtatacaggttaatgtta\n"
      + "gtgatacacaatactcgtgggccatgggttctcaaataaaatgtaatattgcgtcgatca\n"
      + "ctcacccacgtatttggtctaattatgttttatttagtgacaatccaatagataaccggt\n"
      + "cctattaagggctatatttttagcgaccacgcgtttaaacaaaggattgtatgtagatgg\n"
      + "taccagtttaattgccagtgggcaatcctaagcaaaatgagattctatcctaaagtttgg\n"
      + "gcttgatataagatttcggatgtatgggttttataatcgttggagagctcaatcatgagc\n"
      + "taatacatggatttcgctacctcaccgagagaccttgcatgaagaattctaaccaaaagt\n"
      + "ttaataggccggattggattgagttaattaagaccttgttcagtcatagtaaaaaccctt\n"
      + "aaattttaccgattgacaaagtgagcagtcgcaataccctatgcgaaacgcctcgatagt\n"
      + "gactaggtatacaaggtttttgagttcctttgaaatagttaactaatttaaaattaatta\n"
      + "acgacatggaaatcacagaacctaatgctttgtaggagttatttatgctgtttactgcct\n"
      + "ctacaaccctaataaagcagtcctaagaatgaaacgcatcttttagttcagaaagtggta\n"
      + "tccagggtggtcaatttaataaattcaacatcgggtctcaggatattcggtcatataatt\n"
      + "tattaagggctcttcgagtcttactctgagtgaaattggaaacagtcatccttttcgttg\n"
      + "tgaggcatcttacaccgctatcgatatacaatgcattccaccgcggtgtcccgtacacaa\n"
      + "ggaaacttgttaccttggggatataagaaaactcacacgtctcattattaaactgagtac\n"
      + "aatttttgcacgagaaagtaatgcaatacaatatgatgaaagccagctaatgaaaaggga\n"
      + "tggaacgcacctcggatctgttgcactggattaaaatccgattatttttaaaaatattca\n"
      + "gtgctagagcatatcaggtctacttttttatctggtatgtaaagcccacggagcgatagt\n"
      + "gagatccttacgactcaacgaaaagttataacataactcccgttagccaaagcccaatcc\n"
      + "cgattactgccctaccctaacgtctgccatctaaatatcgaacttgttatgatcaatgtg\n"
      + "actacctcccaccctttccccttcatttgttccactggggataagctagcgttttcagaa\n"
      + "tcaatgcaataagaatagccaattgtctcacttcatcagagctcttggcaattccaggcg\n"
      + "ctacgtggttctggaatatattcatttttcaaatagtaatacgtttagtgttgctattgt\n"
      + "ctacacgtttggatattacgttatgtgagcggacatcaatagttgtctaactctttagta\n"
      + "agccagagatagcactcttagcgaatggataccatcttccataagtttagttaatagtcc\n"
      + "gaaacaactgcttcgagcatatttgaacctccttgtaggcaaatagcctcttcaaagcaa\n"
      + "tcttactaatagatagagtttgttttaagggactactagaaatgggacaatcttaatagt\n"
      + "atgacctaaactgacatttaaagatatatccaggtggcaagcataaagatcattgcgcca\n"
      + "cctccaccgtgggattacttatcagtcgatatcctatatgctaagtttgcgacggcagaa\n"
      + "tacaaactaagctgagttgatgctaaccttacctatgataccccattggaccggttaaca\n"
      + "gccctacttattccaaataaaagaacttttatgctgtagaagctattatagtgatgcctg\n"
      + "gtaacttcagtatattaaaatgacacacatacgccatatagagctcctggaactttgaat\n"
      + "aatgagcgaacttcgaagttgaagagcaagaaaccatatgtcacggttgcctaaagcccg\n"
      + "gtaaccagacatgtgctatcattgatcattatcgaggttttcataaccttgacccattat\n"
      + "cggctgtgcgcggacaagtacttaaatcactagtttcttcacctgcttatcggtaagaaa\n"
      + "taaggttggcaaagaatcgcataagacggacgtagagccgcagcgttgtgcgagtccagg\n"
      + "tgcatgcgcagcaataggattttaaattttgttccatttttaatttagccgtaaggatgt\n"
      + "ccgtaaatgattgaaaattggattcaatctttgggcctatgctactggaacctgatcgac\n"
      + "aaaatttcaaacatacgttaactccgaaagaccgtatttttgcggctagaatagtcagtc\n"
      + "gcttggagccatataccttaccacttaaacgacgtgctcctgtagttgaaatataaacag\n"
      + "aacacaaagactaccgatcatatcaactgaagatctttgtaactttgaggcgaagcaccc\n"
      + "tcttcgagacaactaagagtaaagtaccgggcgccgcaaggagtcgattgggaccctaaa\n"
      + "tcttgacgaattgctaagaggctcagagctaccactgtaatttctctagagcccataata\n"
      + "aatgaacgatacatccgtaggtagcacctaagggattataatggaagccaaatgcagtta\n"
      + "ataatattatatactggcgtacacgattcgacggatctctcacatagtgattcacgaccc\n"
      + "ccccctttgattgacacagcgtcagcattttgcaagaacgatcttctgcatagggtgcgc\n"
      + "caccgtaaggatgacgtcgaagctacaactgggtataatttaccatgcttccctgatgct\n"
      + "gagtgcaatacactaagaatgagtttttaccccatatcaccagtatttgttctgttattg\n"
      + "cgaagaaatggctatgctgagttggcgactaaagtcacccatcctttttattaggtaacc\n"
      + "ccctcccttaaactaactgatttgctggagctgccctgcatacatatactttatcattta\n"
      + "tggacgtccgtgacgcttattatccaccatagtcgatatgctacacggattcattaatgg\n"
      + "atcgtaggagtttaagttatatttactaagatcggtctcggctactatcccgccttaccc\n"
      + "ggcgctatttacggccatttttaatatattgacggtaattattcctatggtttcgaccgc\n"
      + "acgtccttggacaagaaagaatggcaaaaaaaatgtaaaagaaaaaaaatattgagtccc\n"
      + "taccatcatataaaaaatatgtgatgagtaacttgacgaaatgttagtggttattaaaga\n"
      + "ctatctattacaccttttgttttctgtcgtagtatattaaagtctagaagccttacagga\n"
      + "aaatcagggttatacagccgatactccgcagcatgaatcatcgaggaggtgtcctaccat\n"
      + "cgcgccttgtaatcttgtctgtgtatactgtatttagaccttttatacaaagtaaatatc\n"
      + "tcggctttatgtgattgggaggggcctactcaaacatgatgacttgacctaataatcact\n"
      + "gtgcgggcgtcttatgactagctattccttgaaatccaccaccaaatggttaatatgtaa\n"
      + "aaactttgacgatgaaacaaggtgaatgtgtagttactttgtgtaattagctgcgtcgag\n"
      + "cattgcttgtaaaaccgtcaatcgcacacgttacttccataaaatttctacgaatacacc\n"
      + "cttcttaaaaaaaacgtaggaattcacgagtttaacaaacgataactgtataaagtggaa\n"
      + "gtccgaagaaagcagatgcccgaactactcgaagatgtttcgttttcttaaccatagggg\n"
      + "cttcttaatggcccactacgcacattttgttcaagcccgagagggacatccccattacgg\n"
      + "gagtattactaaaactgttccgtaatacgttcagcaagggatgaaaaaggccactgctca\n"
      + "agttattgacgtgggagtattacatcggaagcctgaatcccacactatgatggtctgtac\n"
      + "aggcctagggactgcgtctagacggtattaccggcttctaatcatacgatcgtgagtctt\n"
      + "aacgggaagtaaggctcacacctaccccaaaccatttatctatgtaagtataaaattgtg\n"
      + "cgtaagtgttcaaagtggacaataaagacgtggcaaaaacccccgcacataagccgcttt\n"
      + "agatttcacaaataccaatgcggttaaaaacatccttgagtcgtacatacaccatactcg\n"
      + "cgttaaacggatataacagaagataataaatccggatgtggagtcggtgtaactatagaa\n"
      + "agccaagtgaaataatgcttaccagtcatttagctatacggctttcatttcatgtcaaga\n"
      + "gggtggagtttgacctgtacagttgatatatcaccgatacttagaactcacctaaagcta\n"
      + "aaattgctcgcagcgtgtaatccgcatattacaaacaatagatgggattcattatacata\n"
      + "agacacgatgatctgctttttcaggttgcgagatgttgcctatcgtcaatcgagtcctgc\n"
      + "cttacaccacttaaacaaaagtattgacagggaacctattttcgaggtattatatagtcc\n"
      + "agcttgaatatcaatttgacagttaacctagtgaaaatcagtaagaggaaatacgccaca\n"
      + "ttctccagtgaaattctacgggttatcgtctagtccaactatcaattataactcacgaga\n"
      + "tataagtaaattctcgtacttggcctgatttttattatactttggatccttagtaaacag\n"
      + "gaagggagaaaccttcaacgaaaaacactggattttgttttactctcaaagctcttatat\n"
      + "gacggaaataccctgtcaagtcttaactttattactagactaatgaaatgggcttggggt\n"
      + "ggccagaatcatagtacaatttagcggatacactattcggactttcctatcggctgtctg\n"
      + "gttggataagtatggggactaataggctagacatacctatacttaaactatacaggcgtc\n"
      + "atctatctctgcaactttggagttccctgatgttctcccgccctttgggttcacatcttc\n"
      + "tataccgacacccctaataacgattagtttgtgggttagagtaaattaatacggttaata\n"
      + "ttaatgtatcgttgaaaagctggtgtcgccaataaggtaaccggctaggcagagtatatg\n"
      + "tcacgaagtataactaccctaatgataagctgtaggaataaaattaatgctgtctctaag\n"
      + "cgaagagatatttccgactctgttttaatgacgaatctcattacttctgacttgcaaatg\n"
      + "ttcaatatggcacggtttcacggcacctttgtgacgcatataatgaacttagaagattat\n"
      + "aacgacggaactttatatgataatccgttacgattaaagaatctgttaaatatcataatg\n"
      + "gcattcagttctagaccgtgcatcatggtaaacttactttctctgcatggcgacatacat\n"
      + "ttcgctattcaaattcgcgtgtggttacacccactcgcacctttggaatattaagagaag\n"
      + "atgatcagaaaatccattcgctcaatttttctgacgtacgtctaatttatcctaggagac\n"
      + "aaatcgttttatgtctctcacatttttgaagaaaggttcgagagacaatactcaggtcct\n"
      + "gaactgctagaagatactcggtggagcgtggcaacaatgaaaaactcgtgacataaatga\n"
      + "atgatacttttccaagttcagttaagtgaatatgtttaacatacccggcttttcgatctt\n"
      + "aagctgacgctggacgtgcgagtaatgtcagtctcttacatacactagtgactccaagtt\n"
      + "tcgtcaaaaacgccccctcccttctcgagcccactcacgctatgtattgacgcgaacttg\n"
      + "ttcgggatcagacttttcaggagttcggtcgcgtgtccctatgtgctaatatataagtta\n"
      + "gatcgcattagatgctaatctgaatacttatagacgaccttcaacgagaacgggtaccac\n"
      + "cttgaggctagagttaggtgtgaaacgacaggtagggacatataaaatttgagtgcggct\n"
      + "ttagttaagggtttaattacctactcaaacatcacgctcgcgcccttcgtacgtaatcga\n"
      + "ccatctagaggctaaggggactgtactaggtagtgattaatgatatcctagacgcacgtg\n"
      + "ccttagatcttcagactctgatggtccgcgatcaccgtaattgtagtcctccaactcgat\n"
      + "cactttgttggcgtcaaagaaattacgatatctaaatacttataatacaataaccaagga\n"
      + "tgagaatgactcatcgcgttggagttatattgcttgaagttctatggaatgaaagcacgt\n"
      + "tatctgccgtcccaatatctccagtgagctaattcattggacggtccactttgatcaatc\n"
      + "cccgaggagatgttcggacactttagtctgtaacacttagcgttgagaccacgaacaatt\n"
      + "gattactcagtcttgaaggtgttttccaaagttcattttaaataagactacgataggcct\n"
      + "ttcctattgatataaactacccggctctgttgttcgtgtgagtcgtacttctctgtgttt\n"
      + "ttctgattatagcaagattcgattcttagtgtaaacagcgatttttatttgacccgtcaa\n"
      + "tgagaagcgcataggatctaagcaaaattatcaagttgtgccacaaggtaagatctttcc\n"
      + "agttattgcaggtaggatgtatcccacgttgatagtatgaggtctgacgtcaactgtcta\n"
      + "ggagagttgaccgcgtgcgggtacaccggatttgcatcgatgttgagaacgcagaactcc\n"
      + "cactgtcgtggcggcgttcctgatatttagcaagaggcgttgataaagccctcatcatct\n"
      + "agatctcgacctcatctgccctcttgctccatcattttctacacagactactttcctatc\n"
      + "tacgttagtataattgctttctatcttagtatcatttagagcttctccgtcaacaggttc\n"
      + "gtgctattaaagttagtacgaaagggacaacttgtagcaacgcatttaatcggttttcga\n"
      + "ctacttcgcacaaaatcagataaagaagtttgtcattctattagacattgaattgcgcaa\n"
      + "ttgacttgtaccacttatgatcgaacactgaatcaagactgtgattaactaaaatagaca\n"
      + "agccactatatcaactaataaaaacgcccctggtggtcgaacatagttgactacaggata\n"
      + "attaattggactggagccattacattctctacaatcgtatcacttcccaagtagacaact\n"
      + "ttgaccttgtagtttcatgtacaaaaaaatgctttcgcaggagcacattggtagttcaat\n"
      + "agtttcatgggaacctcttgagccgtcttctgtgggtgtgttcggatagtaggtactgat\n"
      + "aaagtcgtgtcgctttcgatgagagggaattcaccggaaaacaccttggttaacaggata\n"
      + "gtctatgtaaacttcgagacatgtttaagagttaccagcttaatccacggtgctctacta\n"
      + "gtatcatcagctgtcttgcctcgcctagaaatatgcattctatcgttatcctatcaacgg\n"
      + "ttgccgtactgagcagccttattgtggaagagtaatatataaatgtagtcttgtctttac\n"
      + "gaagcagacgtaagtaataatgacttggaataccaaaactaaacatagtggattatcata\n"
      + "ctcaagaactctccagataaataacagtttttacgatacgtcaccaatgagcttaaagat\n"
      + "taggatcctcaaaactgatacaaacgctaattcatttgttattggatccagtatcagtta\n"
      + "aactgaatggagtgaagattgtagaatgttgttctggcctcgcatggggtctaggtgata\n"
      + "tacaatttctcatacttacacggtagtggaaatctgattctagcttcgtagctgactata\n"
      + "ctcaaggaaccactgctcaaggtaggagactagttccgaccctacagtcaaagtggccga\n"
      + "agcttaaactatagactagttgttaaatgctgatttcaagatatcatctatatacagttt\n"
      + "ggacaattatgtgtgcgaaactaaaattcatgctattcagatggatttcacttatgcctt\n"
      + "agaaacagatattgcccgagctcaatcaacagttttagccggaaacaatcgaagcatagg\n"
      + "gacaatgtatcttttcctaaattgccatgtgcagatttctgagtgtcacgaagcgcataa\n"
      + "tagaatcttgtgttgcctcaactcgttgaaaagtttaaaacaatcgcagcagtctttttg\n"
      + "gggtctactgtgtgtttgcaaaataactgaaagaaacgcttgaacaactctgaagtagct\n"
      + "cgagtactcattaaagtgtaacacattagtgaatatcggccaatgaaccaaacgcttccc\n"
      + "ggtacgctatctctctcatcgggaggcgatgtgcaggttatctacgaaagcatcccttta\n"
      + "cgttgagagtgtcgatgcatgaacctcattgtaacaatagcccagcaaattctcatacgt\n"
      + "gcctcagggtccgggcgtactcctccatggaagggcgcgcatctagtgttataccaactc\n"
      + "gctttttaactactatgctgtagttctacaggcatagtggccagtattttctaacttctc\n"
      + "tggatagatgctctcactcctcatccatcacggcttcagtttacgtcttacttgcttgtt\n"
      + "cagcaacggatggaggcattaagtatcttcactgttccctaaaattgctgttcaatatca\n"
      + "aagtaaggacgatacagggaaagctcaagcacactcattgaatactgccccagttgcaac\n"
      + "ctcacttaatctgacaaaaataatgactactctaagtgttgcggaagcagtctcttccac\n"
      + "gagcttgtctgtatcacttcgtataggcatgtaactcgatagacacgaacaccgagtgag\n"
      + "aaactatattcttgcttccgtgtgtgtgacaccaggtaattgatgcggatataagctgga\n"
      + "gatcactcacgcccacacaaggcgctgctacctctttattccaatgtgtaagaatttgct\n"
      + "aacttcatttctagaccgcagctttgcggtcataatttcacggtacggacccttgggtta\n"
      + "gagacttgataacacacttcgcagtttccaccgcgcacatgttttagtggcttctaacat\n"
      + "agaatttttgttgtgacataaagagtgcgtgggagacttgcccgaccgttaagccataat\n"
      + "caattgaaagccccgtgagtcacatctaattggttgtactgcgcatttagctatccttta\n"
      + "gctgactcgaagagattcgattcctaatataggttaattagatggctgccgcgcgaagta\n"
      + "aaacgtgaaaaacgtagtgcgcagatctgcataactcgcgcttaattacttatgagtagt\n"
      + "tccaagttcgctacgttatgagagagattggaattaagcaaatatgttttatggtgattt\n"
      + "tgggatgagaaggactgctaagtacggctactaaacaaatttctaaaaccgccatctacc\n"
      + "ttatcttggagacatttaagttgtatatgtcactagtctagcttttgtctgtgggacgcg\n"
      + "ttctcggaatgagggaaatgcaagagccgattcatcaaatgcttatctaagaaagtagtg\n"
      + "gactattacaccaagcacgaatgccagggaactgctttcttgctcaggacctcgcgacaa\n"
      + "ggtaccccgcataagtcctagaattacatttggtcagcaatgctgacatttgaccgtgaa\n"
      + "aacataattttaatcagaaggcagctcacccgcttgctctagatcttatctttgtatgaa\n"
      + "tgtcagaatttactgcaatatccgttccgaatagtgagggcttagtatagttctctgtat\n"
      + "acaggtcacatcaaactccccctgtcctagtacagctctgagctttaattaattgcatac\n"
      + "atttccttcaatcatcagatgaaaacaccgcgaatcatgctcttctcgtatagggcaaga\n"
      + "gaagcaacaaacaactagcccgactcacgttcatccgccgtatccttgttcagttcttac\n"
      + "tccgtattaggtcagcgaaatctaatcagaataatcggtcgcgtatcaaaattaaaatcc\n"
      + "cgcttgaggttgacaattaaaacgctgagcagttatcggctattagatagtggggtgaaa\n"
      + "gtaattggctggaattatgttaaaacgtgatattaagctaaaatacgctacttgttgccg\n"
      + "acctaattcagtcattcgatattcagttagagccaagaataacaagcttgtataaattga\n"
      + "acggggtgcactaaacgatgtgttactctaatattcagcttggagtatacctgaaggcga\n"
      + "attcatgtatcggccaataataagacgttgaagatcacaatttggactagcaaaagaagg\n"
      + "tgatttatgcgtggggattgagtccactgtacgagtacggtctctggaaaattataggtt\n"
      + "cagggaatataaggaagtaaagataattaccaagagatttttggtatcgctatgacccag\n"
      + "aggtgttctaacgtctgttttgatccgcagaatttctgcctcaatgcatatttgacggac\n"
      + "ttgaactagagcctctaaagttaaatggcgacgcaactgttcctaaacttcaattattac\n"
      + "tactctttttttcctagggtattgtagaggccagtggacaaaataaatcaaatttaagat\n"
      + "gtttcggacattaacatcccccgtagcatagaaatcatcagttatccaatctctcatcga\n"
      + "gcttttacaatttctgctggcgctatggacagcatatgccgcgagacctccgcaagactc\n"
      + "acttgatcactgtaagtatcttcattagaggttagagcctatagttaagctgctgaccta\n"
      + "gtaaaattggtattttctaattttattgctcaagttaaaggttagtgaagggataatgac\n"
      + "gttatttttgaacaatgggttgtattcaattttatatcacgaatggaacccttcattccc\n"
      + "ggcataatactagacgacacgaacaagctccgatctatcagccaggcacgtgttaaggtt\n"
      + "taattccggcaaaccaatgaagcatcaaaaggtgacctgatgcaacttagggtcacgatg\n"
      + "agtttttcaggactacttattacctattaataagttaacatgagccttcataccccgtaa\n"
      + "gacaatacatactccaccaattagaattctgagccatcttatctttttgtatcatcgaag\n"
      + "ggtatggccgaataggttaattagttactcctaacgtctctacaggcatgcatttgacgc\n"
      + "accttcgaaaatagtcaatctctcgccacacgcgtctagtatgcagcatcaaaaatatag\n"
      + "tccacggtttccggattaccaaacgcggcaaagagaaacattgtatcgacggagataact\n"
      + "taatacagaaggaaggggcatcttcgaatacggatgaataattctatctgtttattctga\n"
      + "catcttgttttcaggttaatcttacgcattcaaatgacgcctgccccatgcgtgcgcaat\n"
      + "tattttctaatattgacgagagcaatctcactccttttgggtctatttatgttttattga\n"
      + "ggcacaagcctatacagaacaggtactattaaggccgtgagtgtgagactcaaaccgtgg\n"
      + "aaacaaaggatgggttgttcttggtacaagttttagtgcatgtgggcaatccttaccaaa\n"
      + "atcagatgctatccttaactttgggctgcatttaagatggcggttggaggcctgtgagaa\n"
      + "tcctgcgtgtcatctttaatgaccgaattcatccatgtagattcagatcacacactcatt\n"
      + "ccttgatgttgtctaaacaaaagttgttgtggacgcattggagggagttaagtaacaact\n"
      + "tgggatcgcatacttataaaaattatatgttaaactttcacaaacgctgaagtccaaagt\n"
      + "aactagcccaaacgcctcgagagtcactaggtattaatggtgtttgagttcctgtgaaat\n"
      + "agtgttcgaaggtaaaatttatgtaccaaatcgaaagaacacttaataaggcttgcttgc\n"
      + "acggaggtatgatgtttactgactctacaaccctaattttccagtacgtacattcattcc\n"
      + "aataggttagttctcaaagtgctatacaggctcctcaattgatgatatgcttcagccgct\n"
      + "ctatggatattagctcattttatttaggaagcccgcttagaggcttactatgagggaaat\n"
      + "gccaaaatgtcatacttttcggtgtgtcccatatgacaccgctttacatagaatttgaat\n"
      + "taaaacgcgctctcccgttcactaccatacttggtaccgtgcgcatattacatatagata\n"
      + "taggatcattttttaaagctgtactaggtttgatcgacaatcttatgctatactatatga\n"
      + "tgtaaccctcataatcaataccgatcgtacgatcctagcataggtggcaagcgattttat\n"
      + "gccgattattgtgttaaatagtctgtgagtgtgattatcagggctacgttggtagagggg\n"
      + "ttgtatagacctcgcacacattgtgacatacttaacaatatacgaaaactgatataataa\n"
      + "atccccttacccaaacaccaatcccgttgaatcaactaccataacgtctcccatataaat\n"
      + "tgcctacttgtttgcataaatctgaatacataacaccattgcaccttcttgtgttccaat\n"
      + "cccgttaagattgccttgtcagatgatatgcaagaacaatagcatttgctagcaattatt\n"
      + "aacagctcttcgaattgcctccacataacgcgggagggtatattttaatttggcaaatac\n"
      + "taagtactgttggcgtcatatgctattaacggttggatattaagttatgtcagccgtaag\n"
      + "caagagtgggcgaaatattttgttacccagtgagagcactcttagagtttggatacaata\n"
      + "ggccatatgttgacttaagaggacgtaactacgccgtacaccattgttcaaccgacttct\n"
      + "tggcaaatagaatcgtattagcaatcttaagaatagagacacgttcgtgttagggtatac\n"
      + "tacaaatccgaaaatcttaagaggatcacctaaactgaaatttatacatatttcaacgtg\n"
      + "gatagatttaacataattcagccacctccaacctgggagtaattttcagtagatttacta\n"
      + "gatgattagtggcccaacgcacttgactatataagatctggggatcctaacctgacctat\n"
      + "gagacaaaattggaaacgttaacagcccttatgtgtacaaagaaaagtaagttgttgctg\n"
      + "ttcaacagatgatagtcatgacgcgtaacttcactatagtaaattgaaacaaatacgcaa\n"
      + "tttagacagaatggtacggtcatgaatgacagtaattcgaagtgctagaccaacttaaaa\n"
      + "taggtaaacgtgcccgaaaccccccttaacagaaagctgctatcatggtgcagtatcgac\n"
      + "gtgttcagaaacttgtaacttttgagcaggtccgagcacatggaagtatatcacgtgttt\n"
      + "ctgaaccggcttatccctaagatatatccgtcgcaaactttcgatttagtcccacgtaga\n"
      + "gcccaagcgttgtgcgactccacgtgcatgcccagaaatacgagtttaaatttggttaca\n"
      + "tggttaattttgaccgaagcatcgcactttatgattgataattggattcaatatgtcgcc\n"
      + "ctatgcgaatgcaacatgatccacaatttggctataagacgtttaatccgtatcacactt\n"
      + "tgtttgcggctagtatagtaacgcccgtgcaccaagagtcagtaacaattataagtactc\n"
      + "cgcaggtacttcaaatataaaaactaatcaaacacgacccatatgatcatctgaagatat\n"
      + "ttggaactttctcgacaaccaccctcgtactcaatacttacactaatcgacaggcacacg\n"
      + "caacgtgtacagtcgcaccatattgagtcaagatttgcttagtggcgatgagcgtacacg\n"
      + "cttatttctctagtcacaattagttatctacgagacatcacgagggagcaaataagcgat\n"
      + "gttatggctacacataggcacgtatgaatatgatataagccagttaaacagtcgaaccat\n"
      + "cgagcaaattctcatgcaccaacccacacgttgaggcacaaagagtaagctgtttgaatg\n"
      + "taacttcttctgctgagcgggccccaacgtaaggatcaactagaagagaaaactcggtat\n"
      + "tagtttaaatgcgtcacggagcatgagtgcatttcactaagaatgtctgtgtaaccaata\n"
      + "taacatctatttgttatctgattgcctacttatggctttgcggtcgtggcgactaatgtc\n"
      + "tccaatccttttgaggtcggtaccaactccctttaaattacgctgtgcaggctcatgcac\n"
      + "tgcatacatatacggtagcaggtagggacctcacgcacccttattataatcaatagtagt\n"
      + "tatcagtcaacgaggcaggaatgctgaggtcgaggtgttggtatattttctatgtgccgt\n"
      + "ctaggcgactatcacgcattaccaggcgagatttaagccaattttgaatatagtcaacgt\n"
      + "aatttttactatgggttccaccgaaacgccttgcacaactaagaatcccataaaatatcg\n"
      + "atatcaaataaaagattgtgtcaataccttcatatatattttttcggttgactaacgtga\n"
      + "actaaggttaggggttttgtatgtctatataggaaacagtttcttttctgtcctacttta\n"
      + "gtaaagtcttcaagccttactccaaaatcacggtgattaagccgttactcagcagcatga\n"
      + "ttctgcctgctcgggtcctaaaatccagccttgtaagagtcgctgtgtattagctaggga\n"
      + "gacctttgttaaaaaggatatatcgcggcgggatgtgagtgcgtggcgcatactcaatct\n"
      + "tcagctcgtgtcattataatatctctcccccacgcttttcactagatatgccgtgtaagc\n"
      + "aaacaccttatgcttaatttcgaaaatattggtacttgaaaaaagctgtaggggtactta\n"
      + "atgtctggtaggagatcaggagagaattgagtgtaaaaccgtaaagccctcacctgactt\n"
      + "catgtaaatggcttagaagactccatgatttaataaatactacgaaggaaagactggatc";
}
