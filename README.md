# ART Performance Tests


## How to Run

You can run the benchmarks with the `run.py` script. The Android environment
must be set up, and the board connected via adb. See `run.py --help` for
details.

For example you can run on a target adb device:
       ./run.py --iterations 5 --mode 64 --target

Or on the host, with no adb device:
      ./run.py --iterations 5

## How to Write a Benchmark

Each set of related benchmarks is implemented as a Java class and kept in the
benchmarks/ folder.

Before contributing, make sure that `test/test.py` passes.

### Rules

1. Test method names start with "time" -- Test launcher will find all timeXXX()
   methods and run them.
2. Verify methods start with "verify" -- all boolean verifyXXX() methods will
   be run to check the benchmark is working correctly.
3. Leave iterations as parameter -- Test launcher will fill it with a value
   to make sure it runs in a reasonable duration.
4. Benchmarks should take between 5 and 10 seconds to run.

### Example

    public class MyBenchmark {
           public static void main(String [] args) {
                  MyBenchmark b = new MyBenchmark();
                  long before = System.currentTimeMillis();
                  b.timeMethod0(1000);
                  b.timeMethod1(1000);
                  long after = System.currentTimeMillis();
                  System.out.println("MyBenchmark: " + (after - before));
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
                      testAddResults[i] = i + i;
                  }
                  return result;
           }

           public boolean verifyTestAdd() {
                  boolean result = // test contents of testAddResults[]
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

### HashMapBench

Benchmark for hash map, which is converted from:
http://browserbench.org/JetStream/sources/hash-map.js

License is Apache 2.0.
