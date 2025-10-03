package com.lre.actions.lre.testcontentvalidator.scheduler;

import com.lre.model.enums.SchedulerDurationType;
import com.lre.model.test.testcontent.scheduler.action.Action;
import com.lre.model.test.testcontent.scheduler.action.common.TimeInterval;
import com.lre.model.test.testcontent.scheduler.action.duration.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Matcher;

import static com.lre.actions.helpers.ConfigConstants.RUN_FOR_PATTERN;
import static com.lre.actions.helpers.ConfigConstants.RUN_UNTIL_COMPLETE_PATTERN;
import static com.lre.model.test.testcontent.scheduler.action.common.TimeInterval.parseTimeInterval;

@Slf4j
public record DurationValidator(String workloadType) {

    public Duration validateDuration(String input) {
        Duration duration = new Duration(); // default: UNTIL_COMPLETION

        if (StringUtils.isBlank(input)) {
            log.debug("Input is blank, using default Duration type: UNTIL_COMPLETION");
            return duration;
        }

        Matcher sim = RUN_UNTIL_COMPLETE_PATTERN.matcher(input);
        Matcher run = RUN_FOR_PATTERN.matcher(input);

        if (sim.matches()) {
            duration.setType(SchedulerDurationType.UNTIL_COMPLETION);
            return duration;
        }

        if (run.matches()) {
            duration.setType(SchedulerDurationType.RUN_FOR);
            duration.setTimeInterval(parseTimeInterval(run.group("interval")));
            return duration;
        }

        log.debug("Input did not match any pattern → using default Duration type: UNTIL_COMPLETION");
        return duration;
    }

    public void validateDurationActions(List<Action> actions) {
        List<Action> durationActions = getDurationActions(actions);

        if (durationActions.isEmpty()) {
            Duration duration = new Duration();
            duration.setType(SchedulerDurationType.RUN_FOR);
            duration.setTimeInterval(new TimeInterval(0,0,5,0)); // default 5 min
            actions.add(Action.builder().duration(duration).build());
            durationActions = getDurationActions(actions); // Refresh list
        }

        if (workloadType.startsWith("basic") && durationActions.size() > 1) {
            log.warn("Multiple StopVusers actions found. Keeping first, removing {} others.", durationActions.size() - 1);
            Action first = durationActions.get(0);
            actions.removeIf(a -> a.getStopVusers() != null && a != first);
        }
    }


    private List<Action> getDurationActions(List<Action> actions) {
        return actions.stream().filter(a -> a.getDuration() != null).toList();
    }


}
