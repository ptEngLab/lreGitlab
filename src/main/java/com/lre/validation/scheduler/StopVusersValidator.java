package com.lre.validation.scheduler;

import com.lre.common.utils.WorkloadUtils;
import com.lre.model.enums.SchedulerDurationType;
import com.lre.model.enums.SchedulerVusersType;
import com.lre.model.test.testcontent.scheduler.action.Action;
import com.lre.model.test.testcontent.scheduler.action.common.Ramp;
import com.lre.model.test.testcontent.scheduler.action.stopvusers.StopVusers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Matcher;

import static com.lre.common.constants.ConfigConstants.GRADUALLY_PATTERN;
import static com.lre.common.constants.ConfigConstants.SIMULTANEOUSLY_PATTERN;

@Slf4j
public record StopVusersValidator(String workloadType, int vusersCount) {

    public StopVusers validateStop(String input) {
        StopVusers stopVusers = new StopVusers(); // default: SIMULTANEOUSLY
        if (StringUtils.isBlank(input)) return stopVusers;

        input = StringUtils.trimToEmpty(input).toLowerCase();
        Matcher sim = SIMULTANEOUSLY_PATTERN.matcher(input);
        Matcher grad = GRADUALLY_PATTERN.matcher(input);

        if (sim.matches()) {
            stopVusers.setType(SchedulerVusersType.SIMULTANEOUSLY);
            setVusersForRealWorld(stopVusers, sim);
        } else if (grad.matches()) {
            stopVusers.setType(SchedulerVusersType.GRADUALLY);
            setVusersForRealWorld(stopVusers, grad);
            stopVusers.setRamp(new Ramp(grad.group("users"), grad.group("interval")));
        } else {
            log.debug("[Scheduler] Unknown StopVusers '{}'. Using default SIMULTANEOUSLY.", input);
        }

        return stopVusers;
    }

    private void setVusersForRealWorld(StopVusers stopVusers, Matcher matcher) {
        String count = matcher.group("vusersCount");
        if (WorkloadUtils.isRealWorld(workloadType) && StringUtils.isNotEmpty(count))
            stopVusers.setVusersFromString(count);
    }

    public void validateStopVusersActions(List<Action> actions) {
        List<Action> stops = getStopVusersActions(actions);
        boolean anyRunFor = actions.stream()
                .filter(a -> a.getDuration() != null)
                .anyMatch(d -> d.getDuration().getType() == SchedulerDurationType.RUN_FOR);

        if (anyRunFor && stops.isEmpty()) {
            log.warn("[Scheduler] Duration has RUN_FOR but no StopVusers found. Adding default.");
            StopVusers stop = new StopVusers();
            stop.setType(SchedulerVusersType.SIMULTANEOUSLY);
            actions.add(Action.builder().stopVusers(stop).build());
            stops = getStopVusersActions(actions);
        }

        if (WorkloadUtils.isBasic(workloadType) && stops.size() > 1) {
            log.warn("[Scheduler] Multiple StopVusers found. Keeping first.");
            Action first = stops.get(0);
            actions.removeIf(a -> a.getStopVusers() != null && a != first);
        }
    }

    private List<Action> getStopVusersActions(List<Action> actions) {
        return actions.stream().filter(a -> a.getStopVusers() != null).toList();
    }
}
