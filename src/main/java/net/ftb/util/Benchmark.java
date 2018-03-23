/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2018, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
 * FTB Launcher is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ftb.util;

import net.ftb.log.Logger;

import java.util.concurrent.ConcurrentHashMap;

public final class Benchmark {
    private static Long baseTime;
    private static ConcurrentHashMap<String, Long> startTimes = new ConcurrentHashMap<String, Long>();

    private Benchmark () {
    }

    /**
     * adds a new named timer for benchmarking
     */
    public static void start (String name) {
        startTimes.put(name, System.currentTimeMillis());
    }

    /**
     * reset named timer for benchmarking to current system time
     */
    public static void reset (String name) {
        if (startTimes.containsKey(name)) {
            startTimes.put(name, System.currentTimeMillis());
        } else {
            Logger.logError("Wrong key", new Exception(""));
        }
    }

    /**
     *
     * @return how many ms since timer was started
     */
    private static Long bench (String name) {
        return (System.currentTimeMillis() - startTimes.get(name));
    }

    /**
     * logs the time(in ms) that taskName took
     * @param taskName name of task being benchmarked
     */
    public static void logBench (String taskName) {
        if (startTimes.containsKey(taskName)) {
            Logger.logDebug(taskName + " took " + bench(taskName) + " ms.");
        } else {
            Logger.logError("Wrong key", new Exception(""));
        }
    }

    /*
     * logs the time( in ms) that taskName took, but log with namedAs String
     * @param taskName name of task being benchmarked
     * @param namedAs String to be used in log message
     *
     */
    public static void logBenchAs (String taskName, String namedAs) {
        if (startTimes.containsKey(taskName)) {
            Logger.logDebug(namedAs + " took " + bench(taskName) + " ms.");
        } else {
            Logger.logError("Wrong key", new Exception(""));
        }
    }

    /**
     * Similar with logBench(String taskName), but resets timer afterwards
     */
    public static void logBenchReset (String taskName) {
        if (startTimes.containsKey(taskName)) {
            logBench(taskName);
            reset(taskName);
        } else {
            Logger.logError("Wrong key", new Exception(""));
        }
    }

    /**
     * Similar with logBenchAs(String taskName, String namedAs), but resets time afterwards
     */
    public static void logBenchAsReset (String taskName, String namedAs) {
        if (startTimes.containsKey(taskName)) {
            logBenchAs(taskName, namedAs);
            reset(taskName);
        } else {
            Logger.logError("Wrong key", new Exception(""));
        }
    }
}
