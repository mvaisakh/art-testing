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

SCRIPT_PATH=$(dirname $(readlink -e $0))

DIR_BUILD=$SCRIPT_PATH/build
DIR_BENCHMARKS=benchmarks
DIR_FRAMEWORK=framework



# Set to true to only build for the host and not use any android specific
# commands.
HOST_BUILD=false
# Set to true to print the commands executed.
VERBOSE=false
# Set to true to treat warnings as errors, in which case hitting a warning will
# cause the script to exit.
WERROR=false




# Helpers.

CRED="\033[0;31m"
CORANGE="\033[0;33m"
CGREEN="\033[0;32m"
CNC="\033[0m"

# Disable colour output when redirected.
if [ ! -t 1 ]; then
  CRED=
  CORANGE=
  CGREEN=
  CNC=
fi

error() {
  echo -e "${CRED}ERROR: $*${CNC}" >&2
  exit 1
}

warning() {
  echo -e "${CORANGE}WARNING: $*${CNC}" >&2
  $WERROR && exit 2
}

verbose_safe() {
  if $VERBOSE; then
    echo "$@"
  fi
  "$@" || error "FAILED command:\n$*";
}




# Arguments handling

usage="Usage: $(basename "$0")
Build Java benchmark class, APK, and jar files.
Output files are produced in $DIR_BUILD.

Options:
	-h	Show this help message.
	-H	Only build for the host.
	  	This allows compiling for the host outside of an Android environment.
	-v	Verbose. Print the commands executed.
	-w	Treat warnings as errors, causing them to abort.
"

while getopts ':hHlvw' option; do
  case "$option" in
    h) echo "$usage"; exit ;;
    H) HOST_BUILD=true ;;
    v) VERBOSE=true ;;
    w) WERROR=true ;;
    \?)
      printf "Illegal option: -%s\n" "$OPTARG" >&2
      echo "$usage"
      exit 1
      ;;
  esac
done

shift $((OPTIND - 1))




# Disable wildcard expansion.
set -f
# Find what Java files we need to compile.
JAVA_BENCHMARK_FILES="$(find $DIR_BENCHMARKS -type f -name '*'.java) "
# Reenable wildcard expansion.
set +f

# Transform the list of java files in a list of strings that will be provided to
# the benchmark framework to indicate what benchmark classes are available.
# Remove the `.java` extension.
JAVA_BENCHMARK_CLASSES=${JAVA_BENCHMARK_FILES//.java/}
# Remove the leading `./` and `benchmarks`.
JAVA_BENCHMARK_CLASSES=${JAVA_BENCHMARK_CLASSES//.\//}
JAVA_BENCHMARK_CLASSES=${JAVA_BENCHMARK_CLASSES//benchmarks\//}
# Trim trailing whitespaces.
JAVA_BENCHMARK_CLASSES=${JAVA_BENCHMARK_CLASSES/%[[:space:]]/}
IFS=' ' read -a array <<< $JAVA_BENCHMARK_CLASSES
# Sort the names.
readarray -t sorted < <(printf '%s\0' "${array[@]}" | sort -z | xargs -0n1)
JAVA_BENCHMARK_CLASSES=$(echo ${sorted[@]})
# Make it a list of literal string.
JAVA_BENCHMARK_CLASSES="\""${JAVA_BENCHMARK_CLASSES//[[:space:]]/\", \"}"\""
# Write the result file.
BENCHMARK_LIST_TEMPLATE="$(cat $DIR_FRAMEWORK/org/linaro/bench/BenchmarkList.java.template)"
BENCHMARK_LIST_TEMPLATE=${BENCHMARK_LIST_TEMPLATE/<to be filled by the build system>/$JAVA_BENCHMARK_CLASSES}
echo "$BENCHMARK_LIST_TEMPLATE" > $DIR_FRAMEWORK/org/linaro/bench/BenchmarkList.java

# Framework java files are compiled unconditionally.
JAVA_FRAMEWORK_FILES="$(find $DIR_FRAMEWORK -type f -name '*'.java)"



# Build everything.

verbose_safe rm --recursive --force $DIR_BUILD
verbose_safe mkdir --parents $DIR_BUILD/classes/
verbose_safe javac -cp $DIR_BENCHMARKS -cp $DIR_FRAMEWORK -d $DIR_BUILD/classes/ $JAVA_FRAMEWORK_FILES $JAVA_BENCHMARK_FILES
verbose_safe jar cf $DIR_BUILD/bench.jar $DIR_BUILD/classes/
if ! $HOST_BUILD; then
  if hash dx 2> /dev/null; then
    verbose_safe dx --dex --output $DIR_BUILD/bench.apk $DIR_BUILD/classes/
  else
    warning "\`dx\` command not found. bench.apk won't be generated." \
      "Are you running from an Android environment?"
  fi
fi
