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
        description = "Run tests for the java benchmarks framework.",
        # Print default values.
        formatter_class = argparse.ArgumentDefaultsHelpFormatter)
    parser.add_argument('--target', '-t',
                        nargs='?', default=None, const=utils.adb_default_target_string,
                        help='Also run on target adb device.')
    parser.add_argument('--no-host-tests',
                        default=False, action='store_true',
                        help='Do not run tests on host.')
    parser.add_argument('--jobs', '-j', metavar='N', type=int, nargs='?',
                        default=multiprocessing.cpu_count(),
                        help='Test using N jobs.')
    return parser.parse_args()


def TestCommand(command, _cwd=None):
    escape_wildcards = lambda x: x if not '*' in x else '"' + x + '"'
    printable_command = ' '.join(list(map(escape_wildcards, command)))
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


# A `target` set to `None` indicates that the tests should be run on host.
def TestBenchmarksCommon(target):
    rc = 0
    run_py = os.path.join(".", "tools", "benchmarks", "run.py")
    build_target_args = []
    run_target_args = []
    if target:
        build_target_args = ['-t']
        run_target_args = ['--target']
        if target != utils.adb_default_target_string:
            run_target_args.append(target)

    # Test standard usage of the top-level scripts.
    rc |= TestCommand(["./build.sh"] + build_target_args,
                      _cwd=utils.dir_root)
    # Two full runs of `run.py`, with and without auto-calibration. Later runs
    # can filter benchmarks to reduce the duration of the tests.
    rc |= TestCommand([run_py] + run_target_args, _cwd=utils.dir_root)
    rc |= TestCommand([run_py] + run_target_args + ["--dont-auto-calibrate"], _cwd=utils.dir_root)
    # Test executing from a different path than the root.
    non_root_path = os.path.join(utils.dir_root, "test", "foobar")
    rc |= TestCommand(["mkdir", "-p", non_root_path])
    rc |= TestCommand([os.path.join(utils.dir_root, "build.sh")] + build_target_args, _cwd=non_root_path)
    rc |= TestCommand([os.path.join(utils.dir_root, run_py)] + run_target_args +
                       # Reduce the duration of the tests.
                       ["--filter", "benchmarks/algorithm/NSieve"],
                      _cwd=non_root_path)
    # Test that the `--output-*` option work even when a path prefix is not specified.
    rc |= TestCommand([os.path.join(utils.dir_root, run_py)] + run_target_args +
                       # Reduce the duration of the tests.
                       ["--filter", "benchmarks/algorithm/CryptoMD5",
                       "--output-pkl=no_path_prefix.pkl"],
                      _cwd=non_root_path)
    rc |= TestCommand(["rm", "-rf", non_root_path])
    return rc


def TestBenchmarksOnHost():
    return TestBenchmarksCommon(None)


def TestBenchmarksOnTarget(target):
    return TestBenchmarksCommon(target)


def TestBenchmarksCompareScript():
    rc = 0
    run_py = os.path.join(".", "tools", "benchmarks", "run.py")
    compare_py = os.path.join(".", "tools", "benchmarks", "compare.py")
    benchmarks_filter = ["--filter", "benchmarks/algorithm/*"]
    rc |= TestCommand([run_py, "--output-pkl=/tmp/res1"] + benchmarks_filter, _cwd=utils.dir_root)
    rc |= TestCommand([run_py, "--output-pkl=/tmp/res2"] + benchmarks_filter, _cwd=utils.dir_root)
    rc |= TestCommand([compare_py, "/tmp/res1", "/tmp/res2"], _cwd=utils.dir_root)
    rc |= TestCommand([compare_py, "--significant-changes", "/tmp/res1", "/tmp/res2"], _cwd=utils.dir_root)
    rc |= TestCommand([compare_py, "--order-by-diff", "/tmp/res1", "/tmp/res2"], _cwd=utils.dir_root)
    return rc


def TestBenchmarkPackages():
    benchmark_files = []
    # TODO: Automatically test that each benchmark has the correct package.
    return 0


def TestLint(jobs = 1):
    return lint.LintFiles(lint.GetJavaFiles(), jobs)


def TestTopLevelWrapperScripts():
    rc = 0
    rc |= TestCommand(["./run.py"], _cwd=utils.dir_root)
    non_root_path = os.path.join(utils.dir_root, "test", "foobar")
    rc |= TestCommand(["mkdir", "-p", non_root_path])
    rc |= TestCommand([os.path.join(utils.dir_root, "./run.py")], _cwd=non_root_path)
    rc |= TestCommand(["./run.py", "--output-pkl=/tmp/res1"], _cwd=utils.dir_root)
    rc |= TestCommand(["./run.py", "--output-pkl=/tmp/res2"], _cwd=utils.dir_root)
    rc |= TestCommand(["./compare.py", "/tmp/res1", "/tmp/res2"], _cwd=utils.dir_root)
    return rc

if __name__ == "__main__":
    args = BuildOptions()

    rc = 0
    if not args.no_host_tests:
        rc |= TestBenchmarksOnHost()
        rc |= TestBenchmarksCompareScript()
        rc |= TestBenchmarkPackages()
        rc |= TestLint(args.jobs)
        rc |= TestTopLevelWrapperScripts()
    if args.target:
        rc |= TestBenchmarksOnTarget(args.target)

    if rc != 0:
        print("Tests FAILED.")
    else:
        print("Tests passed.")
