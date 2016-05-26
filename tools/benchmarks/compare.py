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
import json
import os
import sys

from collections import OrderedDict

dir_benchs = os.path.dirname(os.path.realpath(__file__))
dir_tools = os.path.join(dir_benchs, '..')
sys.path.insert(0, dir_tools)
import utils
import utils_stats

def BuildOptions():
    parser = argparse.ArgumentParser(
        description = "Compare two results of the `run.py` script.",
        # Print default values.
        formatter_class = argparse.ArgumentDefaultsHelpFormatter)
    utils.AddCommonCompareOptions(parser)
    parser.add_argument('--order-by-diff', '-o',
                        action = 'store_true', default = False,
                        help = 'Show results with bigger differences first.')
    # TODO: The default threshold values below have been calibrated for Nexus9.
    #        Make sure they are relevant for other platforms.
    parser.add_argument('--significant-diff-threshold', '--sdiff',
                        metavar = 'threshold (%)',
                        type = float, default = 0.2,
                        help = '''Results with a difference above this threshold
                        (in %%) will be included in the list of significant
                        results.''')
    parser.add_argument('--significant-deviation-threshold', '--sdev',
                        metavar = 'threshold (%)',
                        type = float, default = 3.0,
                        help = '''Results with a deviation higher than this
                        threshold (in %%) will be included in the significant
                        results even if the difference threshold is not met.''')
    return parser.parse_args()


# Filter out benchmarks that do not show any significant difference between the
# two set of results.
def FilterSignificantChanges(in_1, in_2, diff_threshold, dev_threshold):
    out_1 = {}
    out_2 = {}
    benchmarks = set(in_1.keys()).intersection(set(in_2.keys()))
    for bench in benchmarks:
        m1, M1, _, _, _, ave1, d1, dp1 = utils_stats.ComputeStats(in_1[bench])
        m2, M2, _, _, _, ave2, d2, dp2 = utils_stats.ComputeStats(in_2[bench])
        diff = utils_stats.GetRelativeDiff(ave1, ave2)
        if abs(diff) >= diff_threshold \
                or dp1 >= dev_threshold \
                or dp2 >= dev_threshold:
            out_1[bench] = in_1[bench]
            out_2[bench] = in_2[bench]
    return out_1, out_2


def OrderResultsByDifference(in_1, in_2):
    regressions = {}
    improvements = {}
    regressions_1 = OrderedDict({})
    regressions_2 = OrderedDict({})
    improvements_1 = OrderedDict({})
    improvements_2 = OrderedDict({})
    benchmarks = set(in_1.keys()).intersection(set(in_2.keys()))
    for bench in benchmarks:
        m1, M1, _, _, _, ave1, d1, dp1 = utils_stats.ComputeStats(in_1[bench])
        m2, M2, _, _, _, ave2, d2, dp2 = utils_stats.ComputeStats(in_2[bench])
        diff = utils_stats.GetRelativeDiff(ave1, ave2)
        if diff > 0:
            regressions[bench] = diff
        else:
            improvements[bench] = diff

    order_regressions = \
        list(bench for bench in \
             sorted(regressions, key = lambda x: -abs(regressions[x])))
    for bench in order_regressions:
        regressions_1[bench] = in_1[bench]
        regressions_2[bench] = in_2[bench]
    order_improvements = \
        list(bench for bench \
             in sorted(improvements, key = lambda x: -abs(improvements[x])))
    for bench in order_improvements:
        improvements_1[bench] = in_1[bench]
        improvements_2[bench] = in_2[bench]

    return (regressions_1, regressions_2), (improvements_1, improvements_2)


if __name__ == "__main__":
    args = BuildOptions()
    file_1 = open(args.res_1, 'r')
    file_2 = open(args.res_2, 'r')
    res_1 = json.load(file_1, object_pairs_hook=OrderedDict)
    res_2 = json.load(file_2, object_pairs_hook=OrderedDict)
    res_1 = utils.Filter(res_1, args.filter, args.filter_out)
    res_2 = utils.Filter(res_2, args.filter, args.filter_out)

    if args.significant_changes:
        res_1, res_2 = \
            FilterSignificantChanges(res_1, res_2,
                                     args.significant_diff_threshold,
                                     args.significant_deviation_threshold)

    utils.Error('This script is deprecated. Use the top-level `compare.py` '
                'script instead.')

    file_1.close()
    file_2.close()
