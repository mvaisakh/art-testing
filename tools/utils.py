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

si_factors = {'m' : 0.001, 'n' : 0.000000001, 'u' : 0.000001}

dir_tools = os.path.dirname(os.path.realpath(__file__))
dir_root = os.path.realpath(os.path.join(dir_tools, '..'))
dir_benchmarks = os.path.join(dir_root, 'benchmarks')
dir_out = os.path.join(dir_root, 'out')
dir_build = os.path.join(dir_out, 'build')
dir_build_java_classes = os.path.join(dir_build, 'classes')
dir_framework = os.path.join(dir_root, 'framework')

verbose = True

def ensure_dir(path):
    if not os.path.exists(path):
        os.makedirs(path)

def GetTimeValue(value, si_prefix):
    return value * si_factors[si_prefix] if si_prefix else value

def PrettySIFactor(value):
    si_factor = float('inf')
    si_prefix = ''

    for i in si_factors.items():
        if i[1] < si_factor and value < i[1] * 1000:
            si_factor = i[1]
            si_prefix = i[0]

    return si_factor, si_prefix

def VerbosePrint(msg):
    if verbose: print(msg)
