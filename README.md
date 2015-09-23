# ART Performance Tests


## How to Run

You can run the benchmarks with the `run.py` script. The Android environment
must be set up, and the board connected via adb. See `run.py --help` for
details.

For example you can run:
       ./run.py --iterations 5 --mode 64

Or on the host, with no adb device:
      ./run.py --iterations 5 --host

## How to Write a Benchmark

Each set of related benchmarks is implemented as a Java class and kept in the
benchmarks/ folder.

### Rules

1. Test method names start with "time" -- Test launcher will find all timeXXX()
   methods and run them.
2. Leave iterations as parameter -- Test launcher will fill it with a value
   to make sure it runs in a reasonable duration.
3. Benchmarks should take between 5 and 10 seconds to run.

### Example

    public class MyBenchmark {
           public static void main(String [] args) {
                  // No need
           }

    //                  +----> test method prefix should be "time..."
    //                  |
    // ignored <---+    |              +-------> No need to set iterations. Test
                   |    |              |         framework will try to fill a
                   |    |              |         reasonable value automatically.
    //             |    |              |
           public int timeTestAdd(int iters) {
                  int result = 0;
                  for (int i = 0; i < iters; i++) {
                      // test code
                  }
                  return result;
           }

    // If you want to fill iterations with your own value. Write a method like:

    //    Don't warm up test <-----+               +---------> Your choice
    //                             |               |
           @IterationsAnnotation(noWarmup=true, iterations=600)
           public long timeSfib(int iters) {
              long sum = 0;
              for (int i = 0; i < iters; i++) {
                  sum += sfib(20);
              }
              return sum;
           }
    }

    // Please refer to existing benchmarks for further examples.


## Test Suite Details

TODO: Detail all benchmarks here, especially what they are intended to achieve.

### e.g. Raytrace

Description, License (if any), Main Focus, Secondary Focus, Additional Comments

### Control Flow Recursive

Control flow recursive is ported from:
https://github.com/WebKit/webkit/blob/master/PerformanceTests/SunSpider/tests/sunspider-1.0.2/controlflow-recursive.js

License is Revised BSD licence:
http://benchmarksgame.alioth.debian.org/license.html
