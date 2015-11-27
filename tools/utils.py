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

import json
import os
import pickle
import subprocess
import sys
import time


dir_tools = os.path.dirname(os.path.realpath(__file__))
dir_root = os.path.realpath(os.path.join(dir_tools, '..'))
dir_benchmarks = os.path.join(dir_root, 'benchmarks')
dir_out = os.path.join(dir_root, 'out')
dir_build = os.path.join(dir_out, 'build')
dir_build_java_classes = os.path.join(dir_build, 'classes')
dir_framework = os.path.join(dir_root, 'framework')

# Constant shared values that should not be modified.
si_factors = {'m' : 0.001, 'n' : 0.000000001, 'u' : 0.000001}
adb_default_target_string = '<default>'
adb_default_target_copy_path = '/data/local/tmp'

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

def Error(message, rc=1):
    print('ERROR: ' + message)
    sys.exit(rc)



# Common arguments for `run` scripts.
def AddCommonRunOptions(parser):
    opts = parser.add_argument_group('options common to all `run` scripts')
    opts.add_argument('--iterations', '-i',
                      type=int,
                      default=1,
                      help="The number of iterations to run.")
    opts.add_argument('--target', '-t',
                      nargs='?', default=None, const=adb_default_target_string,
                      help='Run on target adb device.')
    opts.add_argument('--mode',
                      choices = ['32', '64'], default = '',
                      help='''When specified, force using the 32bit or 64bit
                      architecture instead of the target primary architecture.
                      This is only valid when running on target.''')
    opts.add_argument('--target-copy-path',
                      default = adb_default_target_copy_path,
                      help = '''Path where objects should be copied on the
                      target.''')


default_output_formats = ['pkl', 'json']

def OutputObject(object, format, output_filename):
    if format not in default_output_formats:
        Error('Unexpected format: ' + format)
    else:
        ensure_dir(os.path.dirname(output_filename))
        if format == 'pkl':
            with open(output_filename, 'wb') as output_file:
                # Create a python2-compatible pickle dump.
                pickle.dump(object, output_file, 2)
                print('Wrote results to %s.' % output_filename)
        elif format == 'json':
            with open(output_filename, 'w') as output_file:
                print(json.dumps(object), file=output_file)
                print('Wrote results to %s.' % output_filename)

def AddOutputFormatOptions(parser, formats=default_output_formats):
    opts = parser.add_argument_group('output formats')
    format_output_filename = os.path.relpath(
        os.path.join(dir_out,
                     '{type}',
                     time.strftime("%Y.%m.%d-%H:%M:%S") + '.{type}'))
    format_help_message = 'Dump results to the given file in {type} format.'
    for f in formats:
        const_output_filename = format_output_filename.format(type=f)
        opts.add_argument('--output-%s' % f,
                          nargs='?', default=const_output_filename,
                          metavar='FILE',
                          help=format_help_message.format(type=f))


# Common arguments for `compare` scripts.
def AddCommonCompareOptions(parser):
    parser.add_argument('res_1', metavar = 'res_1.pkl')
    parser.add_argument('res_2', metavar = 'res_2.pkl')
