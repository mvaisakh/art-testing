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

def ComputeStats(nums):
        m = min(nums)
        M = max(nums)
        ave = statistics.mean(nums)
        d = statistics.pstdev(nums, ave)
        dp = d / ave * 100 if ave != 0 else float("inf")
        return m, M, ave, d, dp

def PrintStats(dict_results, iterations = None):
    headers = ['', 'min', 'max', 'mean', 'stdev', 'stdev (% of mean)']
    results = []
    for benchmark in sorted(dict_results):
        results.append([benchmark] + list(ComputeStats(dict_results[benchmark])))
    PrintTable(headers, results)


# Print a table showing the difference between two runs of benchmarks.
def PrintDiff(res_1, res_2):
    # Only print results for benchmarks present in both sets of results.
    benchmarks = set(res_1.keys()).intersection(set(res_2.keys()))
    headers = ['', 'mean1', 'stdev1 (% of mean1)', 'mean2', 'stdev2 (% of mean2)',
               '(mean2 - mean1) / mean1 * 100']
    results = []
    for bench in sorted(benchmarks):
        m1, M1, ave1, d1, dp1 = ComputeStats(res_1[bench])
        m2, M2, ave2, d2, dp2 = ComputeStats(res_2[bench])
        diff = (ave2 - ave1) / ave1 * 100 if ave1 != 0 else float("inf")
        results.append([bench, ave1, dp1, ave2, dp2, diff])
    PrintTable(headers, results)


# Pretty-prints a table. The arguments must look like:
# - headers: a list of strings
#     ['header1', 'header2', 'header3']
# - lines: a list of lists of data.
#     [['name1', 0.123, 0.456],
#      ['name2', 1.1, 2.2]]
def PrintTable(headers, lines):
    expected_number_of_fields = len(headers)
    col_lengths = [len(field) for field in headers]

    data_format_code = '.3f'
    data_format = '{0:%s}' % data_format_code

    # Scan through the inputs to compute the maximum column lengths.
    # Pay attention to correctly format the data.
    for line in lines:
        n_fields = len(line)
        if n_fields != expected_number_of_fields:
            print('Expected %d fields, found %d.' % \
                  (expected_number_of_fields, n_fields))
            raise Exception
        col_lengths = [max(col_lengths[0], len(line[0]))] + \
            [max(col_lengths[i], len(data_format.format(float(line[i])))) \
             for i in range(1, len(line))]

    # Compute the format strings.
    # The first column is expected to be a description of the line.
    headers_formats_list = ['{0:<%d} ' % col_lengths[0]]
    formats_list = ['{0:<%d} ' % col_lengths[0]]
    for i in range(1, len(col_lengths)):
        headers_formats_list += ['{%d:>%d} ' % (i, col_lengths[i])]
        formats_list += ['{%d:>%d%s} ' % (i, col_lengths[i], data_format_code)]

    # Print the table.
    headers_format = '  '.join(headers_formats_list)
    format = '  '.join(formats_list)
    print(headers_format.format(*headers))
    delimiters = ['-' * l for l in col_lengths]
    print(headers_format.format(*delimiters))
    for line in lines:
        print(format.format(*line))
