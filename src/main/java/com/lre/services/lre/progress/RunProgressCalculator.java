package com.lre.services.lre.progress;

import com.lre.model.enums.RunState;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class RunProgressCalculator {
    private final long timeslotDurationMillis;

    // Percentage of total timeslot allocated to each state
    private final Map<RunState, Double> stateTimeAllocations = Map.of(
            RunState.INITIALIZING, 0.05,      // 5% of timeslot
            RunState.RUNNING, 0.85,           // 85% of timeslot (main execution)
            RunState.BEFORE_COLLATING_RESULTS, 0.02,  // 2% of timeslot
            RunState.COLLATING_RESULTS, 0.04, // 4% of timeslot
            RunState.BEFORE_CREATING_ANALYSIS_DATA, 0.02, // 2% of timeslot
            RunState.CREATING_ANALYSIS_DATA, 0.02     // 2% of timeslot
    );

    // Base progress when entering each state
    private final Map<RunState, Integer> stateStartProgress = Map.of(
            RunState.INITIALIZING, 0,
            RunState.RUNNING, 10,
            RunState.BEFORE_COLLATING_RESULTS, 85,
            RunState.COLLATING_RESULTS, 87,
            RunState.BEFORE_CREATING_ANALYSIS_DATA, 92,
            RunState.CREATING_ANALYSIS_DATA, 95,
            RunState.FINISHED, 100
    );

    // Target progress when completing each state
    private final Map<RunState, Integer> stateEndProgress = Map.of(
            RunState.INITIALIZING, 10,
            RunState.RUNNING, 85,
            RunState.BEFORE_COLLATING_RESULTS, 87,
            RunState.COLLATING_RESULTS, 92,
            RunState.BEFORE_CREATING_ANALYSIS_DATA, 94,
            RunState.CREATING_ANALYSIS_DATA, 98,
            RunState.FINISHED, 100
    );

    public RunProgressCalculator(long timeslotDurationMillis) {
        this.timeslotDurationMillis = timeslotDurationMillis;
    }

    public int calculateProgress(RunState state, long totalElapsedMillis) {
        if (state == RunState.FINISHED) return 100;

        // For RUNNING state, use time-based progress
        if (state == RunState.RUNNING) {
            return calculateRunningProgress(totalElapsedMillis);
        }

        // For other states, calculate based on elapsed time within state allocation
        return calculateStateProgress(state, totalElapsedMillis);
    }

    private int calculateRunningProgress(long totalElapsedMillis) {
        if (timeslotDurationMillis <= 0) return 10;

        // Progress from 10% to 85% based on elapsed time
        int runningProgress = 10 + (int) ((totalElapsedMillis * 75) / timeslotDurationMillis);
        return Math.min(runningProgress, 85);
    }

    private int calculateStateProgress(RunState state, long totalElapsedMillis) {
        Double stateAllocation = stateTimeAllocations.get(state);
        Integer startProgress = stateStartProgress.get(state);
        Integer endProgress = stateEndProgress.get(state);

        if (stateAllocation == null || startProgress == null || endProgress == null) {
            return startProgress != null ? startProgress : 0;
        }

        // Calculate how much time should be allocated to this state
        long stateAllocatedTime = (long) (timeslotDurationMillis * stateAllocation);

        // Estimate when this state should start (cumulative of previous states)
        long stateStartTime = calculateStateStartTime(state);

        // Calculate progress within this state
        long timeInState = Math.max(0, totalElapsedMillis - stateStartTime);
        int progressRange = endProgress - startProgress;

        if (stateAllocatedTime > 0) {
            int stateInternalProgress = (int) ((timeInState * progressRange) / stateAllocatedTime);
            return startProgress + Math.min(stateInternalProgress, progressRange);
        }

        return startProgress;
    }

    private long calculateStateStartTime(RunState state) {
        // Calculate cumulative time of all states before this one
        long cumulativeTime = 0;

        for (Map.Entry<RunState, Double> entry : stateTimeAllocations.entrySet()) {
            if (entry.getKey() == state) {
                break;
            }
            cumulativeTime += (long) (timeslotDurationMillis * entry.getValue());
        }

        return cumulativeTime;
    }

    public String buildProgressBar(int percent) {
        int barWidth = 20;
        int filledBlocks = (percent * barWidth) / 100;
        return "█".repeat(filledBlocks) + "░".repeat(barWidth - filledBlocks);
    }
}