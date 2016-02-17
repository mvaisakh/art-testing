# Copyright (C) 2015 Linaro Limited. All rights received.
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

import sys

verbose = True

def VerbosePrint(message):
    if verbose:
        print(message)

redirected_output = not sys.stdout.isatty()

def ColourCode(colour):
  return '' if redirected_output else colour

COLOUR_GREEN = ColourCode("\x1b[0;32m")
COLOUR_ORANGE = ColourCode("\x1b[0;33m")
COLOUR_RED = ColourCode("\x1b[0;31m")
NO_COLOUR = ColourCode("\x1b[0m")

# Pretty-prints a table. The arguments must look like:
# - headers: a list of strings
#     ['header1', 'header2', 'header3']
# - lines: a list of lists of data.
#     [['name1', 0.123, 0.456],
#      ['name2', 1.1, 2.2]]
# - line_start: a string, which will be printed at the beginning of every line
#   of the table.
def PrintTable(headers, line_format, lines, line_start=''):
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
    print(line_start + headers_format.format(*headers))
    delimiters = ['-' * l for l in col_lengths]
    print(line_start + headers_format.format(*delimiters))
    for line in lines:
        print(line_start + format.format(*line))
