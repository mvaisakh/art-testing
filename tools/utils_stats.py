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
import warnings

from collections import OrderedDict

try:
    import scipy.stats
except:
    # TODO - utils defines a Warning() function, but utils already imports
    # utils_stats, so importing utils here would cause a circular dependency.
    # Either refactor utils and utils_stats so that there's no need for circular
    # includes, or use warnings.warn in utils_stats.
    warnings.warn("You don't have SciPy installed, t-test and Wilcoxon tests "
                  "won't be reported. You will see `nan` in the appropriate "
                  "fields.")

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

def ComputeStatsTests(list1, list2):
    wilcoxon_p = float('NaN')
    ttest_p = float('NaN')
    if not list1 or not list2 or len(list1) < 10 or len(list2) < 10:
        warnings.warn("Number of samples too small to compute Wilcoxon test.")
    try:
        wilcoxon_p = scipy.stats.wilcoxon(list1, list2)[1]
    except:
        pass
    try:
        ttest_p = scipy.stats.ttest_rel(list1, list2)[1]
    except:
        pass
    return wilcoxon_p, ttest_p

def GetSuiteName(benchmark):
    return benchmark.split("/", 2)[1]

def ComputeGeomeanHelper(data, res, current_key, compute_leaf_geomean):
    if isinstance(data, dict) or isinstance(data, OrderedDict):
        means = []
        stdevs = []
        for k in data:
            sub_means, sub_stdevs = ComputeGeomeanHelper(data[k], res, k,
                                                         compute_leaf_geomean)
            means += sub_means
            stdevs += sub_stdevs
        geomean     = CalcGeomean(means)
        geomean_err = CalcGeomeanError(means, stdevs, geomean)
        res.append([current_key, geomean, geomean_err])
        return means, stdevs
    elif isinstance(data, list):
        _, _, _, _, _, mean, stdev, _ = ComputeStats(data)
        if compute_leaf_geomean:
            geomean     = CalcGeomean(data)
            geomean_err = CalcGeomeanError([mean], [stdev], geomean)
            res.append([current_key, geomean, geomean_err])
        return [mean], [stdev]
    else:
        # TODO: We want to use `utils`, but there is a circular dependency.
        print("ERROR: Unexpected data type: %s." % type(data))
        sys.exit(1)

def ComputeGeomean(data, key='OVERALL', compute_leaf_geomean=False):
    res = []
    ComputeGeomeanHelper(data, res, key,
                         compute_leaf_geomean=compute_leaf_geomean)
    return res

def ComputeAndPrintGeomeanWithRelativeDiff(data, key='OVERALL', compute_leaf_geomean=False):
    res = ComputeGeomean(data, key, compute_leaf_geomean)
    # Make the error relative.
    res = list(map(lambda x: [x[0], x[1], GetRatio(x[2], x[1])], res))
    utils_print.PrintTable(['', 'geomean', 'geomean error (%)'], res)

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
