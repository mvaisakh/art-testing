#! /usr/bin/env python3

# Copyright (C) 2015 Linaro Limited. All rights received.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import argparse
import os
import sys

from collections import OrderedDict

from tools import utils
from tools import utils_stats
from tools.benchmarks.run import GetAndPrintBenchmarkResults
from tools.compilation_statistics.run import GetAndPrintCompilationStatisticsResults

def BuildOptions():
    parser = argparse.ArgumentParser(
        description = "Collect all statistics.",
        # Print default values.
        formatter_class = argparse.ArgumentDefaultsHelpFormatter)
    utils.AddCommonRunOptions(parser)
    utils.AddOutputFormatOptions(parser, utils.default_output_formats)
    args = parser.parse_args()

    if args.mode and not args.target:
        utils.Error('The `--mode` option is only valid when `--target` is specified.')

    added_pathnames = [] if args.add_pathname is None else args.add_pathname
    setattr(args, 'pathnames', [os.path.join(utils.dir_build, 'bench.apk')] + added_pathnames)
    return args

if __name__ == "__main__":
    # TODO: Mac OS support
    if os.uname().sysname != 'Linux':
        utils.Warning('Running this script is supported only on Linux.')

    args = BuildOptions()

    result = OrderedDict()
    result[utils.benchmarks_label] = GetAndPrintBenchmarkResults(args)

    # TODO: it is disabled due to migration to a new approach to run
    # benchmarks via chroot.
    #if args.target:
    #    result[utils.compilation_statistics_label] = \
    #        GetAndPrintCompilationStatisticsResults(args)

    utils.OutputObject(result, 'pkl', args.output_pkl)
    utils.OutputObject(result, 'json', args.output_json)
