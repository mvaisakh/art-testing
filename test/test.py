#! /usr/bin/env python3

#    Copyright 2015 ARM Limited
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

import argparse
import multiprocessing
import os
import subprocess
import sys

dir_test = os.path.dirname(os.path.realpath(__file__))
dir_root = os.path.realpath(os.path.join(dir_test, '..'))
dir_tools = os.path.join(dir_root,'tools')
sys.path.insert(0, dir_tools)
import lint
import utils


def BuildOptions():
    parser = argparse.ArgumentParser(
        description = "Run tests for je java benchmarks framework.",
        # Print default values.
        formatter_class = argparse.ArgumentDefaultsHelpFormatter)
    parser.add_argument('--jobs', '-j', metavar='N', type=int, nargs='?',
                        default=multiprocessing.cpu_count(),
                        help='Test using N jobs.')
    return parser.parse_args()


def TestCommand(command, _cwd=None):
    printable_command = ' '.join(command)
    if _cwd is not None:
        printable_command = "cd " + _cwd + " && " + printable_command
    print("Testing: " + printable_command)
    p = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, cwd=_cwd)
    out, err = p.communicate()
    rc = p.poll()
    if rc != 0:
        print("FAILED: " + printable_command)
        print(out)
    return rc


def TestBenchmarksOnHost():
    rc = 0
    # Test standard usage of the top-level scripts.
    rc |= TestCommand(["./build.sh"], _cwd=utils.dir_root)
    rc |= TestCommand(["./run.py"], _cwd=utils.dir_root)
    rc |= TestCommand(["./run.py", "--dont-auto-calibrate"], _cwd=utils.dir_root)
    # Test executing from a different path than the root.
    non_root_path = os.path.join(utils.dir_root, "test", "foo", "bar")
    rc |= TestCommand(["mkdir", "-p", non_root_path])
    rc |= TestCommand([os.path.join(utils.dir_root, "build.sh")], _cwd=non_root_path)
    rc |= TestCommand([os.path.join(utils.dir_root, "run.py")], _cwd=non_root_path)
    rc |= TestCommand(["rm", "-rf", non_root_path])
    # TODO: Abstract the app name.
    rc |= TestCommand(["java", "org.linaro.bench.RunBench", "benchmarks/micro/Intrinsics.NumberOfLeadingZerosIntegerRandom"], _cwd=utils.dir_build_java_classes)
    rc |= TestCommand(["java", "org.linaro.bench.RunBench", "benchmarks/algorithm/CryptoMD5"], _cwd=utils.dir_build_java_classes)
    return rc


def TestBenchmarkPackages():
    benchmark_files = []
    # TODO: Automatically test that each benchmark has the correct package.
    return 0


def TestLint(jobs = 1):
    return lint.LintFiles(lint.GetJavaFiles(), jobs)


def TestCompareScript():
    rc = 0
    rc |= TestCommand(["./run.py", "--output-pkl=/tmp/res1"], _cwd=utils.dir_root)
    rc |= TestCommand(["./run.py", "--output-pkl=/tmp/res2"], _cwd=utils.dir_root)
    rc |= TestCommand(["./compare.py", "/tmp/res1", "/tmp/res2"], _cwd=utils.dir_root)
    rc |= TestCommand(["./compare.py", "--significant-changes", "/tmp/res1", "/tmp/res2"], _cwd=utils.dir_root)
    rc |= TestCommand(["./compare.py", "--order-by-diff", "/tmp/res1", "/tmp/res2"], _cwd=utils.dir_root)
    return rc


if __name__ == "__main__":
    args = BuildOptions()

    rc = 0
    rc |= TestBenchmarksOnHost()
    rc |= TestBenchmarkPackages()
    rc |= TestLint(args.jobs)
    rc |= TestCompareScript()

    if rc != 0:
        print("Tests FAILED.")
    else:
        print("Tests passed.")
