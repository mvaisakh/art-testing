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

public class SimpleLogger {
    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR, FATAL,
    };

    private LogLevel logLevel;

    static class SingletonHolder {
        // default log level: ERROR.
        static SimpleLogger instance = new SimpleLogger(LogLevel.ERROR);
    }

    public static SimpleLogger getInstance() {
        return SingletonHolder.instance;
    }

    public void setLogLevel(String level) {
        if (level.equals("DEBUG")) {
            setLogLevel(LogLevel.DEBUG);
        } else if (level.equals("INFO")) {
            setLogLevel(LogLevel.INFO);
        } else if (level.equals("WARN")) {
            setLogLevel(LogLevel.WARN);
        } else if (level.equals("ERROR")) {
            setLogLevel(LogLevel.ERROR);
        } else if (level.equals("FATAL")) {
            setLogLevel(LogLevel.FATAL);
        } else {
            fatal("Unknown log level.");
        }
    }

    private SimpleLogger(LogLevel level) {
        logLevel = level;
    }

    public void setLogLevel(LogLevel level) {
        logLevel = level;
    }

    public LogLevel getLogLevel() {
        return this.logLevel;
    }

    public void log(LogLevel thisLevel, String msg) {
        if (thisLevel.ordinal() < logLevel.ordinal()) {
            return;
        }
        System.err.println(thisLevel.toString() + ": " + msg);
        if (thisLevel.compareTo(LogLevel.FATAL) == 0) {
            System.exit(1);
        }
    }

    public void info(String msg) {
        log(LogLevel.INFO, msg);
    }

    public void debug(String msg) {
        log(LogLevel.DEBUG, msg);
    }

    public void error(String msg) {
        log(LogLevel.ERROR, msg);
    }

    public void fatal(String msg) {
        log(LogLevel.FATAL, msg);
    }
}
