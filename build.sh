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

SCRIPT_PATH=$(dirname $0)

DIR_ROOT=$SCRIPT_PATH
DIR_BUILD=$DIR_ROOT/out/build
DIR_BENCHMARKS=$DIR_ROOT/benchmarks
DIR_FRAMEWORK=$DIR_ROOT/framework
JAVA_VERSION=1.7


# Set to true to build for the target.
TARGET_BUILD=false
# Set to true to print the commands executed.
VERBOSE=false
# Set to false to not treat build warnings as errors.
WERROR=true




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

info() {
  echo -e "${CORANGE}INFO: $*${CNC}" >&2
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
The script will automatically attempt to build the APK if the \`dx\` command is
available in the PATH.
Output files are produced in $DIR_BUILD.

Options:
	-h	Show this help message.
	-t	Build for the target. Requires building from an Android environment.
	-v	Verbose. Print the commands executed.
	-W	Do not treat build warnings as errors.
"

while getopts ':htlvW' option; do
  case "$option" in
    h) echo "$usage"; exit ;;
    t) TARGET_BUILD=true ;;
    v) VERBOSE=true ;;
    W) WERROR=false ;;
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
JAVA_BENCHMARK_FILES="$(find $DIR_BENCHMARKS -type f -name '*'.java)"
# Reenable wildcard expansion.
set +f

# Transform the list of java files in a list of strings that will be provided to
# the benchmark framework to indicate what benchmark classes are available.
# Remove the `.java` extension.
JAVA_BENCHMARK_CLASSES=${JAVA_BENCHMARK_FILES//.java/}
# Remove the leading full or relative path.
JAVA_BENCHMARK_CLASSES=${JAVA_BENCHMARK_CLASSES//.\//}
JAVA_BENCHMARK_CLASSES=${JAVA_BENCHMARK_CLASSES//$DIR_ROOT\//}
# Trim trailing whitespaces.
JAVA_BENCHMARK_CLASSES=${JAVA_BENCHMARK_CLASSES/%[[:space:]]/}
read -a array <<< $JAVA_BENCHMARK_CLASSES
# Make it a list of literal string.
JAVA_BENCHMARK_CLASSES=$(printf "    \"%s\",\n" "${array[@]}")
# Remove the trailing comma.
JAVA_BENCHMARK_CLASSES=${JAVA_BENCHMARK_CLASSES%?}
# Write the result file.
BENCHMARK_LIST_TEMPLATE="$(cat $DIR_FRAMEWORK/org/linaro/bench/BenchmarkList.java.template)"
BENCHMARK_LIST_TEMPLATE=${BENCHMARK_LIST_TEMPLATE/<to be filled by the build system>/$JAVA_BENCHMARK_CLASSES}
echo "$BENCHMARK_LIST_TEMPLATE" > $DIR_FRAMEWORK/org/linaro/bench/BenchmarkList.java

# Framework java files are compiled unconditionally.
JAVA_FRAMEWORK_FILES="$(find $DIR_FRAMEWORK -type f -name '*'.java)"



# Build everything.

verbose_safe rm -rf $DIR_BUILD
verbose_safe mkdir -p $DIR_BUILD/classes/
verbose_safe javac -encoding UTF-8 -target $JAVA_VERSION -source $JAVA_VERSION -cp $DIR_BENCHMARKS -cp $DIR_FRAMEWORK -d $DIR_BUILD/classes/ $JAVA_FRAMEWORK_FILES $JAVA_BENCHMARK_FILES
verbose_safe jar cf $DIR_BUILD/bench.jar $DIR_BUILD/classes/
DX=$(which dx)
if [ $TARGET_BUILD = "true" ] || [ -n "$DX" ]; then
  if [ $TARGET_BUILD = "false" ]; then
    info "This is not a target build (\`-t\` was not specified), but" \
      "the \`dx\` command was found, so the APK will be built. (\`dx\`: $DX)"
  fi
  if hash dx 2> /dev/null; then
    verbose_safe dx --dex --output $DIR_BUILD/bench.apk $DIR_BUILD/classes/
  else
    warning "\`dx\` command not found. bench.apk won't be generated." \
      "Are you running from an Android environment?"
  fi
fi
