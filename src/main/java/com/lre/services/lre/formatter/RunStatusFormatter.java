package com.lre.services.lre.formatter;

import com.lre.model.enums.RunState;
import lombok.extern.slf4j.Slf4j;

import static com.lre.common.utils.CommonUtils.formatDuration;

@Slf4j
public record RunStatusFormatter(int runId, long timeslotDurationMillis) {

    public String formatStatusLog(RunState state, long elapsedMillis, long remainingMillis, int progress, String progressBar) {
        return String.format(
                "| %-10s | %-40s | %-25s | %-14s | %-14s | %-20s |",
                "RunId: " + runId,
                "State: " + state.getValue(),
                "Elapsed: " + formatDuration(elapsedMillis),
                "Timeslot: " + formatDuration(timeslotDurationMillis),
                "Time remaining: " + formatDuration(remainingMillis),
                String.format("%3d%% %s", progress, progressBar)
        );
    }


}