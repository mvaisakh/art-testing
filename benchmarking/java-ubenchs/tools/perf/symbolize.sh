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

INTERACTIVE="no"
if [ "x$1" = "x--interactive" ] ; then
  INTERACTIVE="yes"
  shift
fi

# Pull the file from the device and symbolize it.
one() {
  DIR=$(dirname $1)
  NAME=$(basename $1)
  echo $DIR $NAME
  if [ "x$INTERACTIVE" = "xyes" ] ; then
    echo -n "What to do? [Y/n/q] "
    read -e input
    if [ "x$input" = "xn" ] ; then
      return
    fi
    if [ "x$input" = "xq" ] ; then
      exit 0
    fi
  fi
  adb pull $DIR/$NAME /tmp || exit 1
  mkdir -p $OUT/symbols/$DIR
  oatdump --symbolize=/tmp/$NAME --output=$OUT/symbols/$DIR/$NAME
}

# adb shell find seems to output in DOS format (CRLF), which messes up scripting
adbshell() {
  adb shell $@ | sed 's/\r$//'
}

# Check for all ISA directories on device.
all() {
  FILES=$(adbshell find /data -name '*.oat' -o -name '*.dex' -o -name '*.odex')
  for FILE in $FILES ; do
    one $FILE
  done
}

if [ "$1" = "" ] ; then
  all
else
  one $1
fi

