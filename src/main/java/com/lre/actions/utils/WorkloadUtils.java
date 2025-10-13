package com.lre.actions.utils;

public final class WorkloadUtils {

    private WorkloadUtils() {}

    public static boolean isBasic(String workload) {
        return workload != null && workload.toLowerCase().startsWith("basic");
    }

    public static boolean isRealWorld(String workload) {
        return workload != null && workload.toLowerCase().startsWith("real-world");
    }

    public static boolean isRealWorldByGroup(String workload) {
        return "real-world by group".equalsIgnoreCase(workload);
    }

    public static boolean isRealWorldByTest(String workload) {
        return "real-world by test".equalsIgnoreCase(workload);
    }
}
