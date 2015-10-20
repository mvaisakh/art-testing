#!/bin/bash

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

. $(dirname $0)/common.sh

usage() {
  echo "Usage: $(basename "$0") \"<command>\" <out_folder>"
  echo "  Capture events defined in events.sh"
  echo "Example:"
  echo "  $(basename "$0") \"find > /dev/null\" perf-out/find"
}

if [ $# -ne 2 ] ; then
  usage
  exit
fi

cmd=$1
out=$2
# Load events from events.sh
. $(dirname "$0")/config/events.sh

for event in $events ; do
  $(dirname "$0")/record-one.sh "$cmd" $out $event
done

