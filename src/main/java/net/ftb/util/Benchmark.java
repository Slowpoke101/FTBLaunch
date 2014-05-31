package net.ftb.util;


import net.ftb.log.Logger;

public class Benchmark {
    private static Long baseTime;

    /**
     * starts timer for benchmarking
     */
    public Benchmark(){
        reset();
    }
    /**
     * starts timer for benchmarking to current system time
     */
    public static void reset(){
        baseTime = System.currentTimeMillis();
    }

    /**
     *
     * @return how many ms since timer was started
     */
    public static Long bench(){
        return System.currentTimeMillis() - baseTime;
    }
    /**
     * logs the time(in ms) that taskName took
     * @param taskName name of task being benchmarked
     */
    public static void logBench(String taskName){
        Logger.logDebug(taskName + " took " + bench() + " ms.");
    }
}
