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

import math
import statistics
import math

from functools import reduce

import utils_print

def CalcGeomean(nums):
    assert len(nums) != 0
    # We calculate it this way so that we don't lose precision.
    return math.exp((sum(map(lambda x: math.log(x), nums))) / len(nums))

def CalcGeomeanErrorImpl(nums, stdevs, geomean, length):
    assert len(nums) == len(stdevs)
    assert length != 0
    return math.sqrt(sum(map(lambda x, y: (x / y) ** 2, stdevs, nums))) * geomean / length

def CalcGeomeanError(nums, stdevs, geomean):
    return CalcGeomeanErrorImpl(nums, stdevs, geomean, len(nums))

def CalcGeomeanRelationError(nums1, nums2, stdevs1, stdevs2, geomean):
    assert len(nums1) == len(nums2) == len(stdevs1) == len(stdevs2)
    nums = nums1 + nums2
    stdevs = stdevs1 + stdevs2
    return CalcGeomeanErrorImpl(nums, stdevs, geomean, len(nums1))

def GetRelativeDiff(x1, x2):
    return (x2 - x1) / x1 * 100 if x1 else float("inf")

def GetRatio(x1, x2):
    return x1 / x2 * 100 if x2 else float("inf")

def CalcMAD(nums, median):
    # Calculate absolute deviations about the median.
    nums = map(lambda x: abs(x - median), nums)
    mad = statistics.median(nums)
    return mad

def ComputeStats(nums):
        m = min(nums)
        M = max(nums)
        median = statistics.median(nums)
        mad = CalcMAD(nums, median)
        madp = GetRatio(mad, median)
        ave = statistics.mean(nums)
        d = statistics.pstdev(nums, ave)
        dp = GetRatio(d, ave)
        return m, M, median, mad, madp, ave, d, dp

def GetSuiteName(benchmark):
    return benchmark.split("/", 2)[1]

def ComputeGeomean(dict_results):
    if not dict_results: return
    stats_dict = {}

    for benchmark in dict_results:
        suite_name = GetSuiteName(benchmark)
        if (suite_name not in stats_dict):
             stats_dict[suite_name] = {}
        stats_dict[suite_name][benchmark] = dict_results[benchmark]

    # Overall and per suite geomean calculations.
    mean_list  = []
    stdev_list = []
    results = []

    for suite_name in stats_dict:
        suite_mean_list = []
        suite_stdev_list = []

        for benchmark in stats_dict[suite_name]:
            m, M, median, mad, madp, mean, stdev, dp = ComputeStats(stats_dict[suite_name][benchmark])
            suite_mean_list.append(mean)
            suite_stdev_list.append(stdev)
            mean_list.append(mean)
            stdev_list.append(stdev)

        suite_geomean     = CalcGeomean(suite_mean_list)
        suite_geomean_err = CalcGeomeanError(suite_mean_list, suite_stdev_list, suite_geomean)
        results.append([suite_name, suite_geomean, suite_geomean_err,
                        GetRatio(suite_geomean_err, suite_geomean)])

    geomean     = CalcGeomean(mean_list)
    geomean_err = CalcGeomeanError(mean_list, stdev_list, geomean)

    results.append(['OVERALL', geomean, geomean_err,
                    GetRatio(geomean_err, geomean)])
    return results

def ComputeAndPrintGeomean(dict_results):
    if not dict_results: return
    results = ComputeGeomean(dict_results)
    print("GEOMEANS:")
    headers = ['suite', 'geomean', 'error', 'error (% of geomean)']
    utils_print.PrintTable(headers, results)

# Print a table showing the difference between two runs of benchmarks.
def PrintDiff(res_1, res_2, title = ''):
    # Only print results for benchmarks present in both sets of results.
    # Pay attention to maintain the order of the keys.
    benchmarks = [b for b in res_1.keys() if b in res_2.keys()]
    if not benchmarks: return
    headers = [title, 'mean1', 'stdev1 (% of mean1)', 'mean2',
               'stdev2 (% of mean2)', '(mean2 - mean1) / mean1 * 100']
    results = []
    stats_dict = {}
    # collecting data
    for bench in benchmarks:
        suite_name = GetSuiteName(bench)

        if (suite_name not in stats_dict):
             stats_dict[suite_name] = {}

        stats_dict[suite_name][bench] = []
        data1 = m1, M1, median1, mad1, madp1, ave1, d1, dp1 = ComputeStats(res_1[bench])
        data2 = m2, M2, median2, mad2, madp2, ave2, d2, dp2 = ComputeStats(res_2[bench])

        stats_dict[suite_name][bench].append(data1)
        stats_dict[suite_name][bench].append(data2)
        diff = GetRelativeDiff(ave1, ave2)
        results.append([bench, ave1, dp1, ave2, dp2, diff])

    utils_print.PrintTable(headers, results)

    # overall and per suite geomeans calculations
    print("\nGEOMEANS:")
    mean_list1  = []
    mean_list2  = []
    stdev_list1 = []
    stdev_list2 = []
    headers = ['suite', 'geomean', 'error', 'error (% of geomean)']
    results = []

    for suite_name in stats_dict:
        suite_mean_list1  = []
        suite_mean_list2  = []
        suite_stdev_list1 = []
        suite_stdev_list2 = []

        for benchmark in stats_dict[suite_name]:
            bench_mean1  = stats_dict[suite_name][benchmark][0][5]
            bench_mean2  = stats_dict[suite_name][benchmark][1][5]
            bench_stdev1 = stats_dict[suite_name][benchmark][0][6]
            bench_stdev2 = stats_dict[suite_name][benchmark][1][6]

            suite_mean_list1.append(bench_mean1)
            suite_mean_list2.append(bench_mean2)
            suite_stdev_list1.append(bench_stdev1)
            suite_stdev_list2.append(bench_stdev2)

            mean_list1.append(bench_mean1)
            mean_list2.append(bench_mean2)

            stdev_list1.append(bench_stdev1)
            stdev_list2.append(bench_stdev2)

        suite_geomean = CalcGeomean(suite_mean_list2) / CalcGeomean(suite_mean_list1)
        suite_geomean_err = CalcGeomeanRelationError(suite_mean_list1, suite_mean_list2,
                suite_stdev_list1, suite_stdev_list2, suite_geomean)
        results.append([suite_name, suite_geomean, suite_geomean_err,
                GetRatio(suite_geomean_err, suite_geomean)])

    geomean     = CalcGeomean(mean_list2) / CalcGeomean(mean_list1)
    geomean_err = CalcGeomeanRelationError(mean_list1, mean_list2,
            stdev_list1, stdev_list2, geomean)

    results.append(['OVERALL', geomean, geomean_err,
                    GetRatio(geomean_err, geomean)])
    utils_print.PrintTable(headers, results)
