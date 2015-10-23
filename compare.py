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
import pickle

from tools import utils_stats

def BuildOptions():
    parser = argparse.ArgumentParser(
        description = "Compare two results of the `run.py` script.",
        # Print default values.
        formatter_class = argparse.ArgumentDefaultsHelpFormatter)
    parser.add_argument('res_1', metavar = 'res_1.pkl')
    parser.add_argument('res_2', metavar = 'res_2.pkl')
    parser.add_argument('--significant-changes', '-s',
                        action = 'store_true', default = False,
                        help = '''Only show significant changes between the two
                        sets of results.''')
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
        m1, M1, ave1, d1, dp1 = utils_stats.ComputeStats(in_1[bench])
        m2, M2, ave2, d2, dp2 = utils_stats.ComputeStats(in_2[bench])
        diff = (ave2 - ave1) / ave1 * 100 if ave1 != 0 else float("inf")
        if abs(diff) >= diff_threshold \
                or dp1 >= dev_threshold \
                or dp2 >= dev_threshold:
            out_1[bench] = in_1[bench]
            out_2[bench] = in_2[bench]
    return out_1, out_2


if __name__ == "__main__":
    args = BuildOptions()
    pkl_file_1 = open(args.res_1, 'rb')
    pkl_file_2 = open(args.res_2, 'rb')
    res_1 = pickle.load(pkl_file_1)
    res_2 = pickle.load(pkl_file_2)

    if args.significant_changes:
        res_1, res_2 = \
            FilterSignificantChanges(res_1, res_2,
                                     args.significant_diff_threshold,
                                     args.significant_deviation_threshold)

    utils_stats.PrintDiff(res_1, res_2)

    pkl_file_1.close()
    pkl_file_2.close()
