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
JAVA_VERSION=1.8
CROSS_COMPILE_FLAGS="-target $JAVA_VERSION -source $JAVA_VERSION"

# Set to true to build for the target.
TARGET_BUILD=false
# Set to true to print the commands executed.
VERBOSE=false
# Set to false to not treat build warnings as errors.
WERROR=true
JAVA_BENCHMARK_FILES=

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

# This function transforms the provided list of java file names
# to the list of benchmark names.
# The benchmark name is its fully-qualified class name with '/' instead of '.'.
#
# Args:
#   $1: The list of file names.
transform_java_file_names_to_benchmark_names() {
  local java_file_names="$1"

  # Remove the `.java` extension.
  local benchmark_names=${java_file_names//.java/}
  # Remove the leading full or relative path.
  benchmark_names=${benchmark_names//$DIR_ROOT\//}
  # Trim trailing whitespaces.
  benchmark_names=${benchmark_names/%[[:space:]]/}
  echo "${benchmark_names}"
}

# Find all available benchmarks.
find_all_benchmarks() {
  find $DIR_BENCHMARKS -type f -name '*'.java
}

# List all available benchmarks by their name.
list_all_benchmarks() {
  local benchmarks_java_files="$(find_all_benchmarks)"
  transform_java_file_names_to_benchmark_names "${benchmarks_java_files}"
}

# Arguments handling

usage="Usage: $(basename "$0") [FILES]
Build Java benchmark class, APK, and jar files.
The script will automatically attempt to build the APK if the \`dx\` command is
available in the PATH.
Output files are produced in $DIR_BUILD.
By default (when no files are provided on the command-line), all benchmarks are
included.

    build.sh benchmarks/math/AccessNBody.java benchmarks/math/MathCordic.java

Otherwise only the benchmark files specified on the command-line are
compiled. For example:

    build.sh benchmarks/math/AccessNBody.java benchmarks/math/MathCordic.java

Options:
    -h           Show this help message.
    -t           Build for the target. Requires building from an Android
                 environment.
    -l           List all available benchmarks and exit.
    -v           Verbose. Print the commands executed.
    -W           Do not treat build warnings as errors.
    -b BENCHMARK DEPRECATED
                 Include only one benchmark file, specified by its path.
                 Example: -b benchmarks/micro/ShifterOperand.java.
"

while getopts ':htlvWb:' option; do
  case "$option" in
    h) echo "$usage"; exit ;;
    t) TARGET_BUILD=true ;;
    l)
       list_all_benchmarks
       exit 0
       ;;
    v) VERBOSE=true ;;
    W) WERROR=false ;;
    b) JAVA_BENCHMARK_FILES=$OPTARG
       ;;
    \?)
      printf "Illegal option: -%s\n" "$OPTARG" >&2
      echo "$usage"
      exit 1
      ;;
    :)
      echo "Option -$OPTARG requires an argument." >&2
      exit 1
      ;;
  esac
done

shift $((OPTIND - 1))

for bench_file in "$@"; do
  JAVA_BENCHMARK_FILES="${JAVA_BENCHMARK_FILES} $(realpath ${bench_file})"
done

# Disable wildcard expansion.
set -f
# Find what Java files we need to compile.

if [[ -z $JAVA_BENCHMARK_FILES ]]; then
  JAVA_BENCHMARK_FILES="$(find_all_benchmarks)"
fi

# Reenable wildcard expansion.
set +f

# Transform the list of java files in a list of strings that will be provided to
# the benchmark framework to indicate what benchmark classes are available.
# Remove the `.java` extension.
JAVA_BENCHMARK_CLASSES="$(transform_java_file_names_to_benchmark_names "${JAVA_BENCHMARK_FILES}")"

# Make it a list of literal string.
tmp=""
for cl in ${JAVA_BENCHMARK_CLASSES}
do
  tmp+="    \"${cl}\","$'\n'
done
# Remove the trailing comma.
JAVA_BENCHMARK_CLASSES=${tmp%?}

# Write the result file.
BENCHMARK_LIST_TEMPLATE="$(cat $DIR_FRAMEWORK/org/linaro/bench/BenchmarkList.java.template)"
BENCHMARK_LIST_TEMPLATE=${BENCHMARK_LIST_TEMPLATE/<to be filled by the build system>/$JAVA_BENCHMARK_CLASSES}
echo "$BENCHMARK_LIST_TEMPLATE" > $DIR_FRAMEWORK/org/linaro/bench/BenchmarkList.java

# Framework java files are compiled unconditionally.
JAVA_FRAMEWORK_FILES="$(find $DIR_FRAMEWORK -type f -name '*'.java)"

# Build everything.

verbose_safe rm -rf $DIR_BUILD
verbose_safe mkdir -p $DIR_BUILD/classes/

for jar_file in "${DIR_BENCHMARKS}"/lib/*.jar
do
  jar_file="$(realpath "${jar_file}")"
  # Extract jar file and remove META-INF, which is not needed and can cause
  # issues with target runs.
  (cd $DIR_BUILD/classes && jar xfv "${jar_file}" && rm -rf META-INF)
done

javac_cmd_options=("-encoding" "UTF-8" \
  "-cp" "${DIR_BENCHMARKS}:${DIR_BUILD}/classes" \
  "-d" "$DIR_BUILD/classes/" \
  $JAVA_FRAMEWORK_FILES \
  $JAVA_BENCHMARK_FILES)

DX=$(which dx)
# Use different javac for target and host builds:
#  For target: the art/tools/javac-helper.sh is used to invoke javac with the correct bootclasspath.
#  For host: javac from PATH is used.
if [ $TARGET_BUILD = "true" ] || [ -n "$DX" ]; then
  if [ $TARGET_BUILD = "false" ]; then
    info "This is not a target build (\`-t\` was not specified), but" \
      "the \`dx\` command was found, so the APK will be built. (\`dx\`: $DX)"
  fi
  if [[ -d "${DIR_BENCHMARKS}"/resources ]]; then
    tar cfv $DIR_BUILD/resources.tar -C "${DIR_BENCHMARKS}" ./resources
  fi
  verbose_safe ${ANDROID_BUILD_TOP}/art/tools/javac-helper.sh --show-commands --core-only \
    $CROSS_COMPILE_FLAGS \
    "${javac_cmd_options[@]}"
  if hash dx 2> /dev/null; then
    verbose_safe dx --dex --output $DIR_BUILD/bench.apk $DIR_BUILD/classes/
  else
    warning "\`dx\` command not found. bench.apk won't be generated." \
      "Are you running from an Android environment?"
  fi
else
  verbose_safe javac \
    "${javac_cmd_options[@]}"
  (cd $DIR_BUILD/classes && jar cf ../bench.jar .)
fi
