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


def shell(command,
          target=utils.adb_default_target_string,
          exit_on_error=True):
    if not isinstance(command, list):
        command = [command]
    # We need to quote the actual command in the text printed so it can be
    # copy-pasted and executed.
    command_string = ' '.join(['adb'] + GetTargetArgs(target) + \
                              ['shell', '"%s"' % ' '.join(command)])
    command = ['adb'] + GetTargetArgs(target) + ['shell'] + command
    return utils.Command(command, exit_on_error=exit_on_error)
