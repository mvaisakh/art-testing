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

import os
import subprocess

dir_tools = os.path.dirname(os.path.realpath(__file__))
dir_root = os.path.realpath(os.path.join(dir_tools, '..'))
dir_benchmarks = os.path.join(dir_root, 'benchmarks')
dir_build = os.path.join(dir_root, 'build')
dir_build_java_classes = os.path.join(dir_build, 'classes')
dir_framework = os.path.join(dir_root, 'framework')

verbose = True

def ensure_dir(path):
    if not os.path.exists(path):
        os.makedirs(path)

def BuildBenchmarks(build_for_target):
    # Call the build script, with warnings treated as errors.
    command = [os.path.join(dir_root, 'build.sh'), '-w']
    if build_for_target:
        command += ['-t']
    VerbosePrint(' '.join(command))
    subprocess.check_call(command)

def VerbosePrint(msg):
    if verbose: print(msg)
