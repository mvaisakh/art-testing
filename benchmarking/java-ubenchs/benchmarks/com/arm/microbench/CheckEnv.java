/*
 *    Copyright 2015 ARM Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.arm.microbench;

public class CheckEnv {
    public static boolean isAndroid() {
        String vmName = System.getProperty("java.vm.name");
        String runtimeName = System.getProperty("java.runtime.name");
        if ((vmName != null) && vmName.toLowerCase().startsWith("dalvik")) {
            return true;
        }
        if ((runtimeName != null) && runtimeName.toLowerCase().startsWith("android")) {
            return true;
        }
        return false;
    }

    public static boolean isArm() {
        String osArch = System.getProperty("os.arch");
        if (osArch == null) {
            return false;
        }
        osArch = osArch.toLowerCase();
        if (osArch.startsWith("arm") || osArch.startsWith("aarch")) {
            return true;
        }
        return false;
    }

    public static boolean isAArch64() {
        String osArch = System.getProperty("os.arch");
        if (osArch == null) {
            return false;
        }
        osArch = osArch.toLowerCase();
        if (osArch.startsWith("aarch64")) {
            return true;
        }
        return false;
    }
}
