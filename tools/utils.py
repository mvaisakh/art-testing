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
import fnmatch
import json
import os
import pickle
import subprocess
import sys
import time
import traceback

from collections import OrderedDict

dir_tools = os.path.dirname(os.path.realpath(__file__))
sys.path.insert(0, dir_tools)

import utils_print
from utils_print import VerbosePrint
import utils_stats


dir_root = os.path.realpath(os.path.join(dir_tools, '..'))
dir_benchmarks = os.path.join(dir_root, 'benchmarks')
dir_out = os.path.join(dir_root, 'out')
dir_build = os.path.join(dir_out, 'build')
dir_build_java_classes = os.path.join(dir_build, 'classes')
dir_framework = os.path.join(dir_root, 'framework')


# Constant shared values that should not be modified.
si_unit_prefixes = {'' : 1, 'G' : 10 ** 9, 'K' : 10 ** 3, 'M' : 10 ** 6, 'm' : 10 ** -3, 'n' : 10 ** -9, 'u' : 10 ** -6}
benchmarks_label = 'benchmarks'
compilation_statistics_label = 'compilation statistics'
compilation_times_label = 'compilation times'
memory_stats_label = 'memory statistics'
oat_size_label = 'oat size'
adb_default_target_string = '<default>'
adb_default_target_copy_path = '/data/local/tmp'
default_mode = ''
default_compiler_mode = None
default_n_iterations = 1

# TODO: Use python's logging and warning capabilities instead!
def Info(message):
    print('INFO: ' + message)

def Warning(message, exc=None):
    print(utils_print.COLOUR_ORANGE + 'WARNING: ' + message, file=sys.stderr)
    if exc != None:
        traceback.print_exception(type(exc), exc, None)
    print(utils_print.NO_COLOUR)
    traceback.print_stack()

def Error(message, rc=1):
    print(utils_print.COLOUR_RED + 'ERROR: ' + message + utils_print.NO_COLOUR,
          file=sys.stderr)
    traceback.print_stack()
    sys.exit(rc)


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

def MergeLists(x, y):
    # To simplify things, assume that either y is the same as x, or it contains
    # elements that are ordered after those in x.
    # TODO: Use topological sorting or a more efficient algorithm.
    return x + [e for e in y if e not in x]

def PrettySIFactor(value):
    si_factor = float('inf')
    si_prefix = ''

    for i in si_unit_prefixes.items():
        if i[1] < si_factor and value < i[1] * 1000:
            si_factor = i[1]
            si_prefix = i[0]

    if si_factor == float('inf'):
        si_factor = 1

    return si_factor, si_prefix

# Wrapper around `subprocess.Popen` returning the output of the given command.
def Command(command, command_string=None, exit_on_error=True, cwd=None):
    if not command_string:
        command_string = ' '.join(command)
    if cwd:
        command_string = 'cd ' + cwd + ' && ' + command_string
    VerbosePrint(command_string)
    p = subprocess.Popen(command,
                         cwd=cwd,
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
        utils_print.verbose = False

# Common arguments for `run` scripts.
def AddCommonRunOptions(parser):
    opts = parser.add_argument_group('options common to all `run` scripts')
    opts.add_argument('--iterations', '-i',
                      type=int,
                      default=default_n_iterations,
                      help="The number of iterations to run.")
    opts.add_argument('--target', '-t',
                      nargs='?', default=None, const=adb_default_target_string,
                      help='Run on target adb device.')
    opts.add_argument('--mode',
                      choices = ['32', '64'], default = default_mode,
                      help='''When specified, force using the 32bit or 64bit
                      architecture instead of the target primary architecture.
                      This is only valid when running on target.''')
    opts.add_argument('--target-copy-path',
                      default = adb_default_target_copy_path,
                      help = '''Path where objects should be copied on the
                      target.''')
    opts.add_argument('--noverbose', action=SetVerbosity, nargs=0,
                      help='Do not print extra information and commands run.')
    compiler_modes = ['optimizing', 'quick', 'interpreter', 'jit']
    compiler_modes += [x + '-pic' for x in compiler_modes]
    opts.add_argument('--compiler-mode', choices=compiler_modes,
                      default=default_compiler_mode,
                      help='''The compiler to use on target. When this option is
                      not specified no additional arguments are passed so the
                      default compiler on the target is used.''')

def ValidateCommonRunOptions(args):
    options_requiring_target_mode = ['mode', 'compiler-mode']
    if not args.target:
        for opt in options_requiring_target_mode:
            if getattr(args, opt.replace('-', '_')):
                Error('The `--%s` option is only valid when `--target` is specified.' % opt)

# Returns a list of `dex2oat` options for the compiler.
def GetDex2oatOptions(compiler_mode):
    if compiler_mode is None:
        return []

    options = []
    pic = False

    if compiler_mode.endswith('-pic'):
        # We want the `pic` option to follow the compiler backend option.
        pic = True
        compiler_mode = compiler_mode[0:-len('-pic')]

    if compiler_mode == 'optimizing':
        options.append('--compiler-backend=Optimizing')
    elif compiler_mode == 'quick':
        options.append('--compiler-backend=Quick')
    elif compiler_mode == 'interpreter':
        options.append('--compiler-filter=interpret-only')
    elif compiler_mode == 'jit':
        options.append('--compiler-filter=verify-at-runtime')
    else:
        Error('Unsupported compiler mode: ' + compiler_mode)

    if pic:
        options.append('--compile-pic')

    return options

default_output_formats = ['json', 'pkl']

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
    parser.add_argument('-f', '--filter', action = 'append',
                        help='''Quoted (benchmark name) filter pattern. If no
                        filters match, filtering will be attempted with all the
                        patterns prefixed and suffixed with `*`.''')
    parser.add_argument('-F', '--filter-out', action = 'append',
                        help='''Filter out the benchmarks matching this pattern
                        from the results. Filters failing are **not** retried
                        with added wildcards.''')

def CheckDependencies(dependencies):
    for d in dependencies:
        rc, err = Command(['which', d], exit_on_error=False)

        if rc:
            Error("Couldn't find `" + d + "`.")

def PrintData(data, key=None, indentation=''):
    indentation_level = '    '
    if isinstance(data, OrderedDict) or isinstance(data, dict):
        if key is not None:
            print(indentation + key)
        entries = []
        for k in data:
            maybe_entry = PrintData(data[k], k, indentation + indentation_level)
            if maybe_entry is not None:
                entries.append(maybe_entry)
        if entries:
            utils_print.PrintTable([''] + utils_stats.stats_headers,
                                   ['s'] + utils_stats.stats_formats,
                                   entries,
                                   line_start=indentation)
            print('')
    elif isinstance(data, list):
        return [key] + list(utils_stats.ComputeStats(data))


def NameMatchesAnyFilter(name, filters):
    assert(isinstance(name, str))
    if filters is None:
        return False
    # Ensure we have a list of filters. This lets the function work if only one
    # filter is passed as a string.
    filters = list(filters)
    for f in filters:
        if fnmatch.fnmatch(name, f):
            return True
    return False


def FilterListHelper(data, filters, negative_filter=False):
    assert(isinstance(data, list))
    return [x for x in data \
            if NameMatchesAnyFilter(x, filters) != negative_filter]


def FilterList(data, filters, filters_out):
    assert(isinstance(data, list))

    res = data

    if filters:
        res = FilterListHelper(data, filters)
        if not res:
            # Try again with all patterns prefixed and suffixed with `*`.
            extended_filters = list(map(lambda f: '*' + f + '*', filters))
            Info('The filters ' + str(filters) + ' did not match any ' + \
                 'data. Retrying with ' + str(extended_filters) + '.')
            res = FilterListHelper(data, extended_filters)
    if filters_out:
        res = FilterListHelper(res, filters_out, negative_filter=True)

    return res


def FilterHelper(data, filters, negative_filter=False):
    if (not isinstance(data, dict) and not isinstance(data, OrderedDict)):
        return data if negative_filter else None
    res = OrderedDict()
    for key in data:
        name_matches_any_filter = NameMatchesAnyFilter(key, filters)
        if not name_matches_any_filter:
            # Filter the sub-data and keep it if it is not empty.
            subres = FilterHelper(data[key], filters, negative_filter)
            if subres:
                res[key] = subres
        elif not negative_filter:
            res[key] = data[key]
    return res


def Filter(data, filters, filters_out):
    if not isinstance(data, dict) and not isinstance(data, OrderedDict):
        return data

    res = data

    if filters:
        res = FilterHelper(data, filters)
        if not res:
            # Try again with all patterns prefixed and suffixed with `*`.
            extended_filters = list(map(lambda f: '*' + f + '*', filters))
            Info('The filters ' + str(filters) + ' did not match any ' + \
                 'data. Retrying with ' + str(extended_filters) + '.')
            res = FilterHelper(data, extended_filters)
    if filters_out:
        res = FilterHelper(res, filters_out, negative_filter=True)

    return res
