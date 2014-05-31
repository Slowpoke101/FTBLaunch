package net.ftb.util;

import java.util.concurrent.ConcurrentHashMap;
import net.ftb.log.Logger;

public class Benchmark {
    private static Long baseTime;
    private static ConcurrentHashMap<String, Long> startTimes = new ConcurrentHashMap<String, Long>();

    /**
     * adds a new named timer for benchmarking
     */
    public static void start(String name) {
        startTimes.put(name, System.currentTimeMillis());
    }

    /**
     * reset named timer for benchmarking to current system time
     */
    public static void reset(String name){
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
    private static Long bench(String name){
        return (System.currentTimeMillis() -  startTimes.get(name));
    }

    /**
     * logs the time(in ms) that taskName took
     * @param taskName name of task being benchmarked
     */
    public static void logBench(String taskName){
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
    public static void logBenchAs(String taskName, String namedAs){
        if (startTimes.containsKey(taskName)) {
            Logger.logDebug(namedAs + " took " + bench(taskName) + " ms.");
        } else {
            Logger.logError("Wrong key", new Exception(""));
        }
    }

    /**
     * Similar with logBench(String taskName), but resets timer afterwards
     */
    public static void logBenchReset(String taskName){
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
    public static void logBenchAsReset(String taskName, String namedAs){
        if (startTimes.containsKey(taskName)) {
            logBenchAs(taskName, namedAs);
            reset(taskName);
        } else {
            Logger.logError("Wrong key", new Exception(""));
        }
    }
}
