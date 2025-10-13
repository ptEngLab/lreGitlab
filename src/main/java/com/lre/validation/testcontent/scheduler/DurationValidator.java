package com.lre.validation.testcontent.scheduler;

import com.lre.model.enums.SchedulerDurationType;
import com.lre.model.test.testcontent.scheduler.action.Action;
import com.lre.model.test.testcontent.scheduler.action.common.TimeInterval;
import com.lre.model.test.testcontent.scheduler.action.duration.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Matcher;

import static com.lre.actions.utils.ConfigConstants.*;
import static com.lre.model.test.testcontent.scheduler.action.common.TimeInterval.parseTimeInterval;

@Slf4j
public record DurationValidator(String workloadType) {

    public Duration validateDuration(String input) {
        Duration duration = new Duration(); // default: UNTIL_COMPLETION

        if (StringUtils.isBlank(input)) {
            log.debug("Input is blank, using default Duration type: UNTIL_COMPLETION");
            return duration;
        }

        Matcher untilCompleteMatcher = RUN_UNTIL_COMPLETE_PATTERN.matcher(input);
        Matcher runMatcher = RUN_FOR_PATTERN.matcher(input);

        if (untilCompleteMatcher.matches()) return handleUntilComplete(duration);
        if (runMatcher.matches()) return handleRunFor(duration, runMatcher);

        log.debug("Input did not match any duration pattern → using default Duration type: UNTIL_COMPLETION");
        return duration;
    }

    private Duration handleRunFor(Duration duration, Matcher run) {
        duration.setType(SchedulerDurationType.RUN_FOR);
        String interval = run.group("interval");

        if (StringUtils.isNotBlank(interval)) {
            duration.setTimeInterval(parseTimeInterval(interval));
        } else {
            log.warn("RUN_FOR matched but interval missing — using default 5m");
            duration.setTimeInterval(new TimeInterval(0, 0, 5, 0));
        }

        return duration;
    }

    private Duration handleUntilComplete(Duration duration) {
        duration.setType(SchedulerDurationType.UNTIL_COMPLETION);
        duration.setTimeInterval(null);
        return duration;
    }

    public void validateDurationActions(List<Action> actions) {
        List<Action> durationActions = getDurationActions(actions);

        if (durationActions.isEmpty()) {
            actions.add(Action.builder().duration(createDefaultDuration()).build());
            durationActions = getDurationActions(actions);
        }

        if (workloadType.startsWith("basic") && durationActions.size() > 1) {
            log.warn("Multiple duration actions found. Keeping first, removing {} others.", durationActions.size() - 1);
            Action first = durationActions.get(0);
            actions.removeIf(a -> a.getDuration() != null && a != first);
        }
    }

    private Duration createDefaultDuration() {
        Duration duration = new Duration();
        if (workloadType.startsWith("real-world")) {
            duration.setType(SchedulerDurationType.RUN_FOR);
            duration.setTimeInterval(new TimeInterval(0, 0, 5, 0)); // default 5 min
        } else {
            duration.setType(SchedulerDurationType.UNTIL_COMPLETION);
        }
        return duration;
    }

    private List<Action> getDurationActions(List<Action> actions) {
        return actions.stream().filter(a -> a.getDuration() != null).toList();
    }
}
