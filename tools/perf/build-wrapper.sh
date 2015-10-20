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

# Build bench apk and push it to target device.
safe cd $UBENCH_ROOT
safe ./build.sh -t
safe adb push $UBENCH_LOCAL $UBENCH_REMOTE
safe cd -

# Clear dalvik cache
safe adb shell rm -f /data/dalvik-cache/*/$UBENCH_REMOTE_CACHE_FILE

# Trigger the build with debug symbols generated.
safe adb shell ANDROID_DATA=$UBENCH_REMOTE_DIR DEX_LOCATION=$UBENCH_REMOTE_DIR dalvikvm -Xcompiler-option -g -cp $UBENCH_REMOTE org.linaro.bench.RunBench --help > /dev/null
safe adb shell ANDROID_DATA=$UBENCH_REMOTE_DIR DEX_LOCATION=$UBENCH_REMOTE_DIR dalvikvm32 -Xcompiler-option -g -cp $UBENCH_REMOTE org.linaro.bench.RunBench --help > /dev/null
safe adb shell ANDROID_DATA=$UBENCH_REMOTE_DIR DEX_LOCATION=$UBENCH_REMOTE_DIR dalvikvm64 -Xcompiler-option -g -cp $UBENCH_REMOTE org.linaro.bench.RunBench --help > /dev/null

# Symbolize boot.oat.
print_info Symbolizing boot.oat
adb shell ls -1 $UBENCH_REMOTE_DIR/dalvik-cache/*/*boot.oat | xargs -n 1 bash $ANDROID_BUILD_TOP/art/tools/symbolize.sh

# Pull compiled benchmark.
print_info Pulling compiled benchmark
adb shell ls -1 $UBENCH_REMOTE_DIR/dalvik-cache/*/$UBENCH_REMOTE_CACHE_FILE | xargs -n 1 -i adb pull {} $ANDROID_PRODUCT_OUT/symbols/{}

# File paths are not stored in the debug section. We need source code putting in a plan folder.
safe mkdir -p $PLAN_SOURCE_FOLDER
safe find $UBENCH_SRC_FOLDER -name "*.java" -exec cp -f {} $PLAN_SOURCE_FOLDER \;

