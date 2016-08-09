# Copyright (C) 2015 Linaro Limited. All rights received.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import os
import subprocess
import tempfile

import utils


def GetTargetArgs(target):
    return ['-s', target] if target != utils.adb_default_target_string else []


def pull(f,
         local_path,
         target=utils.adb_default_target_string,
         exit_on_error=True):
    return utils.Command(
        ['adb'] + GetTargetArgs(target) + ['pull', f, local_path],
        exit_on_error=exit_on_error)


def push(f,
         target_path=utils.adb_default_target_copy_path,
         target=utils.adb_default_target_string,
         exit_on_error=True):
    return utils.Command(
        ['adb'] + GetTargetArgs(target) + ['push', f, target_path],
        exit_on_error=exit_on_error)


def root(target=utils.adb_default_target_string, exit_on_error=True):
    return utils.Command(['adb'] + GetTargetArgs(target) + ['root'], exit_on_error=exit_on_error)


def shell(command,
          target=utils.adb_default_target_string,
          target_copy_path=utils.adb_default_target_copy_path,
          exit_on_error=True):
    if not isinstance(command, list):
        command = [command]
    # We need to quote the actual command in the text printed so it can be
    # copy-pasted and executed.
    command_string = ' '.join(['adb'] + GetTargetArgs(target) + \
                              ['shell', '"%s"' % ' '.join(command)])
    if len(' '.join(command)) < 512:
        command = ['adb'] + GetTargetArgs(target) + ['shell'] + command
        return utils.Command(command, command_string=command_string , exit_on_error=exit_on_error)
    else:
        fd, path = tempfile.mkstemp()
        remote_path = utils.TargetPathJoin(target_copy_path, os.path.basename(path))
        os.write(fd, bytes(' '.join(command), 'UTF-8'))
        os.close(fd)
        push(path, remote_path, target)
        os.remove(path)
        command = ['adb'] + GetTargetArgs(target) + ['shell', 'source', remote_path]
        rc, outerr = utils.Command(command,
                                   command_string=command_string ,
                                   exit_on_error=exit_on_error)
        shell(['rm', '-f', remote_path], target, exit_on_error=exit_on_error)
        return rc, outerr


def GetISAList(target):
    # The 32-bit ISA name should be a substring of the 64-bit one.
    command = 'getprop | grep "dalvik\.vm\.isa\..*\.variant" | cut -d "." -f4 | sort'
    rc, out = shell(command, target)
    return out.split()

def GetISA(target, mode):
    isa_list = GetISAList(target)

    if not mode:
        # To be consistent with RunBenchADB(), the default mode depends on the executable
        # /system/bin/dalvikvm points to.
        command = 'readlink /system/bin/dalvikvm'
        rc, out = shell(command, target)

        # The default (e.g. if /system/bin/dalvikvm is not a soft link) is 64-bit.
        if '32' in out:
            mode = '32'
        else:
            mode = '64'

    if mode == '64':
        # 64-bit ISA names should contain '64'.
        isa = next((i for i in isa_list if mode in i), None)

        if not isa:
            utils.Error('The target adb device does not support 64-bit mode.')
    else:
        # The 32-bit ISA name comes first.
        isa = isa_list[0]
    return isa
