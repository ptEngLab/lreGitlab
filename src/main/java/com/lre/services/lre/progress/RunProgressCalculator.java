package com.lre.services.lre.progress;

import com.lre.model.enums.RunState;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RunProgressCalculator {
    private final Map<RunState, Long> stateStartTimes = new HashMap<>();
    private final long timeslotDurationMillis;

    public RunProgressCalculator(long timeslotDurationMillis) {
        this.timeslotDurationMillis = timeslotDurationMillis;
    }

    public void recordStateTransition(RunState state) {
        stateStartTimes.put(state, System.currentTimeMillis());
        log.debug("Recorded state transition to: {}", state);
    }

    public int calculateProgress(RunState state) {
        // Historical state duration patterns
        Map<RunState, Long> typicalStateDurations = Map.of(
            RunState.INITIALIZING, TimeUnit.MINUTES.toMillis(2),
            RunState.RUNNING, timeslotDurationMillis,
            RunState.BEFORE_COLLATING_RESULTS, TimeUnit.MINUTES.toMillis(1),
            RunState.COLLATING_RESULTS, TimeUnit.MINUTES.toMillis(3),
            RunState.BEFORE_CREATING_ANALYSIS_DATA, TimeUnit.MINUTES.toMillis(1),
            RunState.CREATING_ANALYSIS_DATA, TimeUnit.MINUTES.toMillis(2)
        );

        // Progress milestones for each state
        Map<RunState, Integer> stateStartProgress = Map.of(
            RunState.INITIALIZING, 0,
            RunState.RUNNING, 10,
            RunState.BEFORE_COLLATING_RESULTS, 85,
            RunState.COLLATING_RESULTS, 87,
            RunState.BEFORE_CREATING_ANALYSIS_DATA, 92,
            RunState.CREATING_ANALYSIS_DATA, 94,
            RunState.FINISHED, 100
        );

        Map<RunState, Integer> stateEndProgress = Map.of(
            RunState.INITIALIZING, 10,
            RunState.RUNNING, 85,
            RunState.BEFORE_COLLATING_RESULTS, 87,
            RunState.COLLATING_RESULTS, 92,
            RunState.BEFORE_CREATING_ANALYSIS_DATA, 94,
            RunState.CREATING_ANALYSIS_DATA, 98,
            RunState.FINISHED, 100
        );

        if (state == RunState.FINISHED) return 100;

        if (!stateStartProgress.containsKey(state)) {
            log.warn("Unknown state: {}, returning 0% progress", state);
            return 0;
        }

        Long stateStartTime = stateStartTimes.get(state);
        if (stateStartTime == null) {
            return stateStartProgress.get(state);
        }

        long stateElapsed = System.currentTimeMillis() - stateStartTime;
        Long typicalDuration = typicalStateDurations.get(state);

        if (typicalDuration != null && typicalDuration > 0) {
            int progressRange = stateEndProgress.get(state) - stateStartProgress.get(state);
            int stateInternalProgress = (int) ((stateElapsed * progressRange) / typicalDuration);
            return stateStartProgress.get(state) + Math.min(stateInternalProgress, progressRange);
        }

        return stateStartProgress.get(state);
    }

    public String buildProgressBar(int percent) {
        int barWidth = 20;
        int filledBlocks = (percent * barWidth) / 100;
        return "█".repeat(filledBlocks) + "░".repeat(barWidth - filledBlocks);
    }
}