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
UBENCH_ROOT=$(realpath $SCRIPT_PATH/../..)
UBENCH_SRC_FOLDER=$UBENCH_ROOT/benchmarks
UBENCH_NAME=bench.apk
UBENCH_LOCAL=$UBENCH_ROOT/build/$UBENCH_NAME
UBENCH_REMOTE_DIR=/data/local/tmp
UBENCH_REMOTE=$UBENCH_REMOTE_DIR/$UBENCH_NAME
UBENCH_REMOTE_CACHE_FILE=${UBENCH_REMOTE//\//@}@classes.dex
UBENCH_REMOTE_CACHE_FILE=${UBENCH_REMOTE_CACHE_FILE/@/}
VMLINUX=$(realpath $SCRIPT_PATH/vmlinux)
REMOTE_PERF_DATA=/data/local/tmp/perf.data
ANDROID_SYMBOL_FOLDER=$ANDROID_PRODUCT_OUT/symbols
PERF_SYMBOL_FLAG="--symfs $ANDROID_SYMBOL_FOLDER"
if [ -f "$VMLINUX" ] ; then
  PERF_SYMBOL_FLAG="-k $VMLINUX $PERF_SYMBOL_FLAG"
fi
PERF_BINUTILS_FLAG=--objdump=aarch64-linux-android-objdump
PERF_RECORD="perf record -q"
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
PLAN_SOURCE_FOLDER=$PERF_OUT/plan_src

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
  # Redirect log information to stdout.
  exec 3>&1
else
  # Redirect log information to stderr.
  exec 3>&2
fi

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

STD_ERR_TMP_FILE=/tmp/run.tmp.log

run() {
  print_info "Executing command: $*"
  $@ 2>$STD_ERR_TMP_FILE
}

safe() {
  run $@
  if [ $? -ne 0 ] ; then
    cat $STD_ERR_TMP_FILE 1>&3
    error "FAILED command: $*"
  fi
}

unsafe() {
  run $@
  if [ $? -ne 0 ] ; then
    cat $STD_ERR_TMP_FILE 1>&3
    print_warning "FAILED command: $*"
  fi
}

