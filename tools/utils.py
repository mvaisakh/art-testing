#    Copyright 2015 Linaro Limited
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
import json
import os
import pickle
import subprocess
import sys
import time
import traceback


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

# Printing helpers.

redirected_output = not sys.stdout.isatty()
verbose = True

def ColourCode(colour):
  return '' if redirected_output else colour

COLOUR_GREEN = ColourCode("\x1b[0;32m")
COLOUR_ORANGE = ColourCode("\x1b[0;33m")
COLOUR_RED = ColourCode("\x1b[0;31m")
NO_COLOUR = ColourCode("\x1b[0m")

def Warning(message):
    print(COLOUR_ORANGE + 'WARNING: ' + message + NO_COLOUR,
          file=sys.stderr)
    traceback.print_stack()

def Error(message, rc=1):
    print(COLOUR_RED + 'ERROR: ' + message + NO_COLOUR,
          file=sys.stderr)
    traceback.print_stack()
    sys.exit(rc)

def VerbosePrint(message):
    if verbose:
        print(message)



def ensure_dir(path):
    if path == '':
        # This can happen when a user refers to the current working directory.
        return
    try:
        if os.path.exists(path):
            if not os.path.isdir(path):
                Error('`%s` exists but is not a directory.' % path)
        else:
            os.makedirs(path)
    except:
        Error('Failed to ensure the directory `%s` exists.' % path)

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

# Wrapper around `subprocess.Popen` returning the output of the given command.
def Command(command, exit_on_error=True, cwd=None, shell=False):
    # `command` is expected to be a string when passed through the shell.
    command_string = ' '.join(command) if not shell else command
    if cwd:
        command_string = 'cd ' + cwd + ' && ' + command_string
    VerbosePrint(command_string)
    p = subprocess.Popen(command,
                         cwd=cwd,
                         shell=shell,
                         stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    outerr, empty_err = p.communicate()
    outerr = outerr.decode()
    rc = p.returncode
    if rc:
        message = 'Command failed:\n' + command_string + '\n' + outerr
        if exit_on_error:
            Error(message)
        else:
            Warning(message)
    return rc, outerr



class SetVerbosity(argparse.Action):
    def __init__(self, option_strings, dest, nargs, **kwargs):
        super(SetVerbosity, self).__init__(option_strings, dest, nargs, **kwargs)
    def __call__(self, parser, namespace, values, option_string):
        global verbose
        verbose = False

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
    opts.add_argument('--noverbose', action=SetVerbosity, nargs=0,
                      help='Do not print extra information and commands run.')


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
