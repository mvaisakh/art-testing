#!/usr/bin/env python3

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

import argparse
import multiprocessing
import os
import subprocess
import sys

import utils


def BuildOptions():
    parser = argparse.ArgumentParser(
        description = "Lint the java code in the repository.",
        # Print default values.
        formatter_class = argparse.ArgumentDefaultsHelpFormatter)
    parser.add_argument('--jobs', '-j', metavar='N', type=int, nargs='?',
                        default=multiprocessing.cpu_count(),
                        help='Lint using N jobs')
    return parser.parse_args()


def GetJavaFiles():
    java_files = []
    for dir_java_files in [utils.dir_framework, utils.dir_benchmarks]:
        for root, dirs, files in os.walk(dir_java_files):
            files = map(lambda x : os.path.join(root, x), files)
            java_files += [f for f in files if f.endswith('.java')]
    java_files.sort()
    return java_files


def Lint(filename):
    command = \
        [os.path.join(utils.dir_tools, 'checkstyle', 'checkstyle'), filename]
    print(' '.join(command))
    process = subprocess.Popen(command,
                               stdout=subprocess.PIPE,
                               stderr=subprocess.PIPE)
    out, err = process.communicate()
    rc = process.wait()
    if rc != 0:
        print(out)
    return rc


def LintFiles(files, jobs = 1):
  pool = multiprocessing.Pool(jobs)
  # The '.get(9999999)' is workaround to allow killing the test script with
  # ctrl+C from the shell. This bug is documented at
  # http://bugs.python.org/issue8296.
  try:
    results = pool.map_async(Lint, files).get(9999999)
    pool.close()
    pool.join()
  except KeyboardInterrupt:
    pool.terminate()
    sys.exit(1)
  n_incorrectly_formatted_files = sum(results)
  return n_incorrectly_formatted_files


if __name__ == "__main__":
    args = BuildOptions()
    rc = LintFiles(GetJavaFiles(), args.jobs)
    sys.exit(rc)
