/*
 * This benchmark has been ported from "The Computer Language Benchmarks Game" suite and
 * significantly modified to fit the benchmarking framework.
 *
 * Originally the benchmark finished with 'System.exit(0)'. A new state of message "-1" was
 * introduced to avoid this.
 *
 * The original file is `threadring/threadring.java-3.java` from the archive
 * available at
 * http://benchmarksgame.alioth.debian.org/download/benchmarksgame-sourcecode.zip.
 * See LICENSE file in the same folder (BSD 3-clause)
 *
 * The Computer Language Benchmarks Game
 * http://benchmarksgame.alioth.debian.org/
 * contributed by Klaus Friedel
 */

/*
 * Description:     Switch from thread to thread passing one token.
 * Main Focus:      TODO
 *
 */

package benchmarks.benchmarksgame;

import java.util.concurrent.locks.LockSupport;

// CHECKSTYLE.OFF: .*
public class threadring {
// CHECKSTYLE.ON: .*
  static final int THREAD_COUNT = 503;

  private static String lastActedThreadName;

  public static class MessageThread extends Thread {
    MessageThread nextThread;
    volatile Integer message;

    public MessageThread(MessageThread nextThread, int name) {
      super("" + name);
      this.nextThread = nextThread;
    }

    public void run() {
      Integer msg = dequeue();
      while (nextThread.enqueue(msg)) {
        msg = dequeue();
        if (msg == -1) {
          break;
        }
      }
    }

    public boolean enqueue(Integer hopsRemaining) {
      if (hopsRemaining == 0) {
        message = -1;
        lastActedThreadName = this.getName();

        // notify all the threads that transmission is over
        LockSupport.unpark(this);
        MessageThread current = nextThread;
        while (current != this) {
          current.message = -1;
          LockSupport.unpark(current);
          current = current.nextThread;
        }

        return false;
      }

      message = hopsRemaining - 1;
      LockSupport.unpark(this);
      return true;
    }

    private Integer dequeue() {
      while (message == null) {
        LockSupport.park();
      }

      Integer msg = message;
      if (msg != -1) {
        message = null;
      }
      return msg;
    }
  }

  public void timeThreadRing(int iters) throws Exception {

    for (int j = 0; j < iters; j++) {
      int hopCount = 1000;

      MessageThread first = null;
      MessageThread last = null;
      for (int i = THREAD_COUNT; i >= 1; i--) {
        first = new MessageThread(first, i);
        if (i == THREAD_COUNT) {
          last = first;
        }
      }
      // close the ring:
      last.nextThread = first;

      // start all Threads
      MessageThread t = first;
      do {
        t.start();
        t = t.nextThread;
      } while (t != first);

      first.enqueue(hopCount);
      first.join(); // wait for System.exit
    }
  }

  public boolean verifyThreadRing() throws Exception {
    timeThreadRing(1);
    String expected = "498";
    String found = lastActedThreadName;

    if (!expected.equals(found)) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  public static void main(String[] args) throws Exception {
    int rc = 0;
    threadring obj = new threadring();

    final long before = System.currentTimeMillis();
    obj.timeThreadRing(3);
    final long after = System.currentTimeMillis();

    if (!obj.verifyThreadRing()) {
      rc++;
    }
    System.out.println("benchmarks/benchmarksgame/threadring: " + (after - before));
    System.exit(rc);
  }
}
