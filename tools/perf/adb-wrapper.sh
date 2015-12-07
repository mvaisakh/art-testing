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

# On some environment, adb seems to output in DOS format (CRLF), which messes
# up scripting.
adbshellstrip() {
  adb shell "$@" | tr -d '\r'
}

if [ "x-$1" != "x-shell" ] ; then
  adb "$@"
elif [ $# -eq 1 ] ; then
  # adb shell without specified command
  adbshellstrip
else
  cmd=$2
  shift 2
  cmd_fullpath=$(adbshellstrip which $cmd)
  cmd_realpath=$(adbshellstrip realpath $cmd_fullpath)
  # Always prefer toybox.
  if [ "$(basename $cmd_realpath)" = "toolbox" ] ; then
    adbshellstrip toybox $cmd "$@"
  else
    adbshellstrip $cmd "$@"
  fi
fi
