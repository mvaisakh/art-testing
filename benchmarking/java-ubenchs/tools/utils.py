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

import glob
import os
import re


dir_tools = os.path.dirname(os.path.realpath(__file__))
dir_root = os.path.realpath(os.path.join(dir_tools, '..'))
dir_benchmarks = os.path.join(dir_root, 'benchmarks')
dir_build = os.path.join(dir_root, 'build')
dir_build_java_classes = os.path.join(dir_build, 'classes')
dir_framework = os.path.join(dir_root, 'framework')


def GetFiles(ext, path):
    return glob.glob(os.path.join(path, '*.' + ext))


def ListBenchmarkJavaFiles():
    list_files = []
    # List java files in 'benchmarks/'.
    list_files += GetFiles('java', dir_benchmarks)
    # List java files in subdirectories of 'benchmarks/'.
    bench_subdirs = [x for x in os.listdir(dir_benchmarks) if os.path.isdir(os.path.join(dir_benchmarks, x))]
    for subdir in bench_subdirs:
        for root, dirs, files in os.walk(os.path.join(dir_benchmarks, subdir)):
            list_files += map(lambda x : os.path.join(root, x), files)
    list_files = [f for f in list_files if re.match(r'^[\w\/-]+\.java$', f)]
    list_files = list(map(lambda x : os.path.relpath(x, dir_root), list_files))
    list_files.sort()
    return list_files



