/*
 * This benchmark has been ported from "The Computer Language Benchmarks Game" suite and slightly
 * modified to fit the benchmarking framework.
 *
 * The original file is `chameneosredux/chameneosredux.java` from the archive
 * available at
 * http://benchmarksgame.alioth.debian.org/download/benchmarksgame-sourcecode.zip.
 * See LICENSE file in the same folder (BSD 3-clause)
 *
 * The Computer Language Benchmarks Game
 * http://benchmarksgame.alioth.debian.org/
 *
 * contributed by Michael Barker
 */

/*
 * Description:     Symmetrical thread rendezvous requests.
 * Main Focus:      TODO
 *
 */

package benchmarks.benchmarksgame;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


/**
 * This implementation uses the java.util.concurrent.atomic library
 * i.e. (compare and set) to avoid locking.  Real threads are used, but
 * are set up as a thread pool and meeting requests are pushed onto a
 * queue that feeds the thread pool.
 */
// CHECKSTYLE.OFF: .*
public final class chameneosredux {

    enum Colour {
        blue,
        red,
        yellow
    }

    private static Colour doCompliment(final Colour c1, final Colour c2) {
        switch (c1) {
        case blue:
            switch (c2) {
            case blue:
                return Colour.blue;
            case red:
                return Colour.yellow;
            case yellow:
                return Colour.red;
            }
        case red:
            switch (c2) {
            case blue:
                return Colour.yellow;
            case red:
                return Colour.red;
            case yellow:
                return Colour.blue;
            }
        case yellow:
            switch (c2) {
            case blue:
                return Colour.red;
            case red:
                return Colour.blue;
            case yellow:
                return Colour.yellow;
            }
        }

        throw new RuntimeException("Error");
    }

    static final class MeetingPlace {

        private final AtomicInteger meetingsLeft;
        private final AtomicReference<Creature> creatureRef = new AtomicReference<Creature>();

        public MeetingPlace(final int meetings) {
            meetingsLeft = new AtomicInteger(meetings);
        }

        public void meet(final Creature incoming) {
            Colour newColour = null;
            Creature first = null;
            Creature next = null;
            do {
                first = creatureRef.get();
                next = incoming;
                if (first != null) {
                    newColour = doCompliment(incoming.colour, first.colour);
                    next = null;
                }
            } while (!creatureRef.compareAndSet(first, next));

            if (first != null) {
                final int meetings = meetingsLeft.decrementAndGet();
                if (meetings >= 0) {
                    first.setColour(incoming.id, newColour);
                    incoming.setColour(first.id, newColour);
                } else {
                    first.complete();
                    incoming.complete();
                }
            }
        }
    }

    static final class Dispatcher implements Runnable {
        private final BlockingQueue<Creature> q;

        public Dispatcher(final BlockingQueue<Creature> q) {
            this.q = q;
        }

        public void run() {
            try {
                while (true) {
                    q.take().run();
                }
            } catch (final InterruptedException e) {
            }
        }
    }

    static final class Creature {

        private final int id;
        private final MeetingPlace place;
        private final BlockingQueue<Creature> q;
        private final CountDownLatch latch;
        private int count = 0;
        private int sameCount = 0;
        private Colour colour;

        public Creature(final MeetingPlace place, final Colour colour,
                        final BlockingQueue<Creature> q, final CountDownLatch latch) {
            this.id = System.identityHashCode(this);
            this.place = place;
            this.latch = latch;
            this.colour = colour;
            this.q = q;
        }

        public void complete() {
            latch.countDown();
        }

        public void setColour(final int id, final Colour newColour) {
            this.colour = newColour;
            count++;
            sameCount += 1 ^ Integer.signum(abs(this.id - id));
            q.add(this);
        }

        private int abs(final int x) {
            final int y = x >> 31;
            return (x ^ y) - y;
        }

        public void run() {
            place.meet(this);
        }

        public int getCount() {
            return count;
        }

        @Override
        public String toString() {
            return String.valueOf(count) + getNumber(sameCount);
        }
    }

    private int run(final int n, final Colour...colours) {
        final int len = colours.length;
        final MeetingPlace place = new MeetingPlace(n);
        final Creature[] creatures = new Creature[len];
        final BlockingQueue<Creature> q = new ArrayBlockingQueue<Creature>(len);
        final CountDownLatch latch = new CountDownLatch(len - 1);

        for (int i = 0; i < len; i++) {
            creatures[i] = new Creature(place, colours[i], q, latch);
        }

        final Thread[] ts = new Thread[len];
        for (int i = 0, h = ts.length; i < h; i++) {
            ts[i] = new Thread(new Dispatcher(q));
            ts[i].setDaemon(true);
            ts[i].start();
        }

        for (final Creature creature : creatures) {
            q.add(creature);
        }

        try {
            latch.await();
            for (final Thread t : ts) {
                t.interrupt();
            }
            for (final Thread t : ts) {
                t.join();
            }
        } catch (final InterruptedException e1) {
            System.err.println("Existing with error: " + e1);
        }

        int total = 0;
        for (final Creature creature : creatures) {
            total += creature.getCount();
        }
        return total;
    }

    private static final String[] NUMBERS = {
        "zero", "one", "two", "three", "four", "five",
        "six", "seven", "eight", "nine"
    };

    private static String getNumber(final int n) {
        final StringBuilder sb = new StringBuilder();
        final String nStr = String.valueOf(n);
        for (int i = 0; i < nStr.length(); i++) {
            sb.append(" ");
            sb.append(NUMBERS[Character.getNumericValue(nStr.charAt(i))]);
        }

        return sb.toString();
    }
    // CHECKSTYLE.ON: .*

  private static final int PREDEFINED_N = 600;

  public void timeChameneosRedux(int iters) {
    for (int i = 0; i < iters; i++) {
      run(PREDEFINED_N, Colour.blue, Colour.red, Colour.yellow);
      run(PREDEFINED_N, Colour.blue, Colour.red, Colour.yellow, Colour.red, Colour.yellow,
                        Colour.blue, Colour.red, Colour.yellow, Colour.red, Colour.blue);
    }
  }

  public boolean verifyChameneosRedux() {
    int expected = 1200;
    int found = run(PREDEFINED_N, Colour.blue, Colour.red, Colour.yellow, Colour.red, Colour.yellow,
                                  Colour.blue, Colour.red, Colour.yellow, Colour.red, Colour.blue);

    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  public static void main(final String[] args) {
    int rc = 0;
    chameneosredux obj = new chameneosredux();

    final long before = System.currentTimeMillis();
    obj.timeChameneosRedux(40);
    final long after = System.currentTimeMillis();

    if (!obj.verifyChameneosRedux()) {
      rc++;
    }
    System.out.println("benchmarks/benchmarksgame/chameneosredux: " + (after - before));
    System.exit(rc);
  }
}
