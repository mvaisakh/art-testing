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
import math
import statistics
import math

from functools import reduce

def CalcGeomean(nums):
    assert len(nums) != 0
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

def ComputeStats(nums):
        m = min(nums)
        M = max(nums)
        ave = statistics.mean(nums)
        d = statistics.pstdev(nums, ave)
        dp = d / ave * 100 if ave != 0 else float("inf")
        return m, M, ave, d, dp

def GetSuiteName(benchmark):
    return benchmark.split("/", 2)[1]

def PrintStats(dict_results, iterations = None):
    headers = ['', 'min', 'max', 'mean', 'stdev', 'stdev (% of mean)']
    results = []

    stats_dict = {}

    for benchmark in dict_results:
        suite_name = GetSuiteName(benchmark)

        if (suite_name not in stats_dict):
             stats_dict[suite_name] = {}

        data = ComputeStats(dict_results[benchmark])
        stats_dict[suite_name][benchmark] = data

        results.append([benchmark] + list(data))

    PrintTable(headers, ['.3f'] * len(headers), results)

    # overall and per suite geomeans calculations
    print("\nGEOMEANS:")
    mean_list  = []
    stdev_list = []
    headers = ['suite', 'geomean', 'error', 'error (% of geomean)']
    results = []

    for suite_name in stats_dict:
        suite_mean_list = []
        suite_stdev_list = []
        for benchmark in stats_dict[suite_name]:
            bench_mean  = stats_dict[suite_name][benchmark][2]
            bench_stdev = stats_dict[suite_name][benchmark][3]

            suite_mean_list.append(bench_mean)
            suite_stdev_list.append(bench_stdev)

            mean_list.append(bench_mean)
            stdev_list.append(bench_stdev)

        suite_geomean     = CalcGeomean(suite_mean_list)
        suite_geomean_err = CalcGeomeanError(suite_mean_list, suite_stdev_list, suite_geomean)
        results.append([suite_name, suite_geomean, suite_geomean_err,
                suite_geomean_err / suite_geomean * 100])

    geomean     = CalcGeomean(mean_list)
    geomean_err = CalcGeomeanError(mean_list, stdev_list, geomean)

    results.append(['OVERALL', geomean, geomean_err, geomean_err / geomean * 100])
    PrintTable(headers, ['.3f'] * len(headers), results)

# Print a table showing the difference between two runs of benchmarks.
def PrintDiff(res_1, res_2, title = ''):
    # Only print results for benchmarks present in both sets of results.
    # Pay attention to maintain the order of the keys.
    benchmarks = [b for b in res_1.keys() if b in res_2.keys()]
    headers = [title, 'mean1', 'stdev1 (% of mean1)', 'mean2', 'stdev2 (% of mean2)',
               '(mean2 - mean1) / mean1 * 100']
    results = []
    stats_dict = {}
    # collecting data
    for bench in benchmarks:
        suite_name = GetSuiteName(bench)

        if (suite_name not in stats_dict):
             stats_dict[suite_name] = {}

        stats_dict[suite_name][bench] = []
        data1 = m1, M1, ave1, d1, dp1 = ComputeStats(res_1[bench])
        data2 = m2, M2, ave2, d2, dp2 = ComputeStats(res_2[bench])

        stats_dict[suite_name][bench].append(data1)
        stats_dict[suite_name][bench].append(data2)
        diff = GetRelativeDiff(ave1, ave2)
        results.append([bench, ave1, dp1, ave2, dp2, diff])

    PrintTable(headers, ['.3f'] * len(headers), results)

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
            bench_mean1  = stats_dict[suite_name][benchmark][0][2]
            bench_mean2  = stats_dict[suite_name][benchmark][1][2]
            bench_stdev1 = stats_dict[suite_name][benchmark][0][3]
            bench_stdev2 = stats_dict[suite_name][benchmark][1][3]

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
                suite_geomean_err / suite_geomean * 100])

    geomean     = CalcGeomean(mean_list2) / CalcGeomean(mean_list1)
    geomean_err = CalcGeomeanRelationError(mean_list1, mean_list2,
            stdev_list1, stdev_list2, geomean)

    results.append(['OVERALL', geomean, geomean_err, geomean_err / geomean * 100])
    PrintTable(headers, ['.3f'] * len(headers), results)

# Pretty-prints a table. The arguments must look like:
# - headers: a list of strings
#     ['header1', 'header2', 'header3']
# - lines: a list of lists of data.
#     [['name1', 0.123, 0.456],
#      ['name2', 1.1, 2.2]]
def PrintTable(headers, line_format, lines):
    expected_number_of_fields = len(headers)
    col_lengths = [len(field) for field in headers]

    # Scan through the inputs to compute the maximum column lengths.
    # Pay attention to correctly format the data.
    for line in lines:
        n_fields = len(line)
        if n_fields != expected_number_of_fields:
            print('Expected %d fields, found %d.' % \
                  (expected_number_of_fields, n_fields))
            raise Exception
        col_lengths = [max(col_lengths[0], len(line[0]))] + \
            [max(col_lengths[i], len(('{0:%s}' % line_format[i]).format(line[i]))) \
             for i in range(1, len(line))]

    # Compute the format strings.
    # The first column is expected to be a description of the line.
    headers_formats_list = ['{0:<%d} ' % col_lengths[0]]
    formats_list = ['{0:<%d} ' % col_lengths[0]]
    for i in range(1, len(col_lengths)):
        headers_formats_list += ['{%d:>%d} ' % (i, col_lengths[i])]
        formats_list += ['{%d:>%d%s} ' % (i, col_lengths[i], line_format[i])]

    # Print the table.
    headers_format = '  '.join(headers_formats_list)
    format = '  '.join(formats_list)
    print(headers_format.format(*headers))
    delimiters = ['-' * l for l in col_lengths]
    print(headers_format.format(*delimiters))
    for line in lines:
        print(format.format(*line))
