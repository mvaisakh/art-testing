#!/bin/sh

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

usage() {
  echo "Usage: $(basename "$0") <event_def_js_file>"
  echo "  Print all event ids defined in the js file, which can be used to modify events.sh."
  echo "Example:"
  echo "  $(basename "$0") events-generic.js"
}

if [ $# -lt 1 ] ; then
  usage
  exit
fi

echo $(grep EventCode $* | cut -d'"' -f 4 | sed 's/^0x/r/')

