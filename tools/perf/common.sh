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

# Common defintions.
SCRIPT_NAME=$(basename $0)
SCRIPT_PATH=$(realpath $(dirname $0))
ADB=$SCRIPT_PATH/adb-wrapper.sh
UBENCH_ROOT=$(realpath $SCRIPT_PATH/../..)
UBENCH_SRC_FOLDER=$UBENCH_ROOT/benchmarks
UBENCH_NAME=bench.apk
UBENCH_LOCAL=$UBENCH_ROOT/out/build/$UBENCH_NAME
UBENCH_REMOTE_DIR=/data/local/tmp
UBENCH_REMOTE=$UBENCH_REMOTE_DIR/$UBENCH_NAME
UBENCH_REMOTE_CACHE_FILE=${UBENCH_REMOTE//\//@}@classes.dex
UBENCH_REMOTE_CACHE_FILE=${UBENCH_REMOTE_CACHE_FILE/@/}
REMOTE_DALVIKVMS=$($ADB shell ls /system/bin/dalvikvm* | xargs -n 1 $ADB shell realpath | sort -u)
VMLINUX=$(realpath $SCRIPT_PATH/vmlinux)
REMOTE_PERF_DATA=/data/local/tmp/perf.data
ANDROID_SYMBOL_FOLDER=$ANDROID_PRODUCT_OUT/symbols
PERF_SYMBOL_FLAG="--symfs $ANDROID_SYMBOL_FOLDER"
if [ -f "$VMLINUX" ] ; then
  PERF_SYMBOL_FLAG="-k $VMLINUX $PERF_SYMBOL_FLAG"
fi
PERF_BINUTILS_FLAG=--objdump=aarch64-linux-android-objdump
# Due to removal of external/linux-tools-perf from AOSP project
# (commit c9f4115ea9a7b27eae143190e3ab99ba46a8d48) 'simpleperf' must be used instead of 'perf'.
# After transition some parts of the scripts doesn't work (Flamegraph, etc).
#
# TODO: check and fix the rest of the scripts.
PERF_RECORD="perf record"
# The `--input` option is broken for `perf annotate` in some versions of
# `perf`. Abstracting the perf binary makes it easy to use a custom `perf`
# binary on host within these scripts.
# A custom version of `perf` can simply be compiled with:
#     cd <linux sources>/tools/perf
#     make
HOST_PERF_BINARY=perf
PERF_REPORT="$HOST_PERF_BINARY report"
PERF_SCRIPT="$HOST_PERF_BINARY script"
PERF_ANNOTATE="$HOST_PERF_BINARY annotate"
PERF_OUT=$SCRIPT_PATH/perf-out
CFG_FOLDER=$PERF_OUT/cfg
STRUCTURED_SOURCE_FOLDER=$PERF_OUT/structured_src
# Need to work around `--input` issue for perf from version 3.14 ~ 3.19 .
HOST_PERF_VERSION=$($HOST_PERF_BINARY version | grep -Eo '[0-9]+\.[0-9]+')
if [ $(bc <<< "3.14 <= $HOST_PERF_VERSION && $HOST_PERF_VERSION <= 3.19") -eq 1 ] ; then
  NEED_PERF_DATA_WORK_AROUND=true
else
  NEED_PERF_DATA_WORK_AROUND=false
fi

# Helpers.
COLOR_RED="\033[31m"
COLOR_GREEN="\033[32m"
COLOR_YELLOW="\033[33m"
COLOR_NORMAL="\033[0m"

# Disable color output when redirected.
if [ ! -t 1 ]; then
  COLOR_RED=
  COLOR_GREEN=
  COLOR_YELLOW=
  COLOR_NORMAL=
fi

# Some command need to save stdin. Redirect log information to stderr to avoid
# mess up the information.
exec 3>&2

print_error() {
  echo -e "${COLOR_RED}ERROR: $*${COLOR_NORMAL}" >&3
}

print_warning() {
  echo -e "${COLOR_YELLOW}WARNING: $*${COLOR_NORMAL}" >&3
}

print_info() {
  echo -e "${COLOR_GREEN}INFO: $*${COLOR_NORMAL}" >&3
}

error() {
  print_error $*
  exit 1
}

STD_ERR_TMP_FILE=/tmp/err.tmp.log

# run, safe, unsafe won't show the output from stderr unless a command returns
# with failure. So do not use below helpers to run a script if you need the
# output from stderr.
run() {
  print_info "Executing command: $*"
  "$@" 2>$STD_ERR_TMP_FILE
}

safe() {
  run "$@"
  local error=$?
  if [ $? -ne 0 ] ; then
    cat $STD_ERR_TMP_FILE 1>&3
    print_error "FAILED command: $*"
    error "error number: $error"
  fi
}

unsafe() {
  run "$@"
  local error=$?
  if [ $error -ne 0 ] ; then
    cat $STD_ERR_TMP_FILE 1>&3
    print_warning "FAILED command: $*"
    print_warning "error number: $error"
  fi
}

