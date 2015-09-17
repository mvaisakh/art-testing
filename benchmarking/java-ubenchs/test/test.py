#! /usr/bin/env python

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
import os
import subprocess

dir_test = os.path.dirname(os.path.realpath(__file__))
dir_root = os.path.realpath(os.path.join(dir_test, '..'))
dir_build = os.path.join(dir_root, 'build')
dir_build_classes = os.path.join(dir_build, 'classes')


def BuildOptions():
    parser = argparse.ArgumentParser(
        description = "Run tests for je java benchmarks framework.",
        # Print default values.
        formatter_class = argparse.ArgumentDefaultsHelpFormatter)
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
    rc |= TestCommand(["./build.sh", "-H", "-w"], _cwd=dir_root)
    # TODO: Abstract the app name.
    rc |= TestCommand(["java", "org.linaro.bench.RunBench", "BubbleSort"], _cwd=dir_build_classes)
    rc |= TestCommand(["./run.py", "--host", "--iterations=1"], _cwd=dir_root)
    rc |= TestCommand(["java", "org.linaro.bench.RunBench", "Intrinsics.NumberOfLeadingZerosIntegerRandom"], _cwd=dir_build_classes)
    return rc


def TestBenchmarkPackages():
    benchmark_files = []
    # TODO: Automatically test that each benchmark has the correct package.
    return 0


if __name__ == "__main__":
    args = BuildOptions()

    rc = 0
    rc |= TestBenchmarksOnHost()
    rc |= TestBenchmarkPackages()

    if rc != 0:
        print("Tests FAILED.")
    else:
        print("Tests passed.")
