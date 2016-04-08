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

def GetFormat(value):
    if isinstance(value, float):
        return '.3f'
    else:
        # For non-floating-point values use the default format.
        return ''

# Pretty-prints a table. The arguments must look like:
# - headers: a list of strings
#     ['header1', 'header2', 'header3']
# - lines: a list of lists of data.
#     [['name1', 0.123, 0.456],
#      ['name2', 1.1, 2.2]]
# - line_start: a string, which will be printed at the beginning of every line
#   of the table.
def PrintTable(headers, lines, line_start=''):
    # We assume that all lines have the same lengths. We could easily handle
    # lines with different lengths if that assumption breaks.
    expected_number_of_fields = len(headers)

    column_lengths = [max(len(('{0:%s}' % GetFormat(line[i])).format(line[i])) \
                          for line in lines) \
                      for i in range(expected_number_of_fields)]
    if headers:
        header_lengths = [len(field) for field in headers]
        column_lengths = [max(header_lengths[i], column_lengths[i]) \
                          for i in range(expected_number_of_fields)]

    # Print the headers.
    headers_format_list = [('{%d:>%d} ' % (i, column_lengths[i])) \
                           for i in range(expected_number_of_fields)]
    headers_format = ' '.join(headers_format_list)
    print(line_start + headers_format.format(*headers))

    # Separate the headers from the data with a line of `-` characters.
    delimiters = ['-' * l for l in column_lengths]
    print(line_start + headers_format.format(*delimiters))

    # Print the lines of data.
    for line in lines:
        format_list = ['{0:<%d} ' % column_lengths[0]]
        format_list += [('{%d:>%d%s} ' % (i, column_lengths[i], GetFormat(line[i]))) \
                        for i in range(1, expected_number_of_fields)]
        line_format = ' '.join(format_list)
        print(line_start + line_format.format(*line))
