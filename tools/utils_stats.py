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

def ComputeAndPrintRelationGeomean(data_1, data_2):
    if not data_1 or not data_2:
        return
    geomeans_1 = ComputeGeomean(data_1)
    geomeans_2 = ComputeGeomean(data_2)
    assert(len(geomeans_1) == len(geomeans_2))
    res = []
    for i in range(len(geomeans_1)):
        g1 = geomeans_1[i]
        g2 = geomeans_2[i]
        assert(g1[0] == g2[0])
        res.append([g1[0],                                          # Name.
                    GetRelativeDiff(g1[1], g2[1]),                  # Diff.
                    GetRatio(g1[2], g1[1]), GetRatio(g2[2], g2[1]), # Errors.
                    g1[1], g2[1]])                                  # Values.

    utils_print.PrintTable(['', 'geomean diff (%)',
                            'geomean error 1 (%)', 'geomean error 2 (%)',
                            'geomean 1', 'geomean 2',], res)
