package com.lre.validation.testcontent.scheduler;

import com.lre.model.enums.SchedulerDurationType;
import com.lre.model.enums.SchedulerVusersType;
import com.lre.model.test.testcontent.scheduler.action.Action;
import com.lre.model.test.testcontent.scheduler.action.common.Ramp;
import com.lre.model.test.testcontent.scheduler.action.stopvusers.StopVusers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Matcher;

import static com.lre.actions.utils.ConfigConstants.GRADUALLY_PATTERN;
import static com.lre.actions.utils.ConfigConstants.SIMULTANEOUSLY_PATTERN;

@Slf4j
public record StopVusersValidator(String workloadType, int vusersCount) {


    public StopVusers validateStop(String input) {
        StopVusers stopVusers = new StopVusers(); // default: SIMULTANEOUSLY

        if (StringUtils.isBlank(input)) {
            log.debug("Input is blank, using default StopVusers type: SIMULTANEOUSLY");
            return stopVusers;
        }

        Matcher sim = SIMULTANEOUSLY_PATTERN.matcher(input);
        Matcher grad = GRADUALLY_PATTERN.matcher(input);

        if (sim.matches()) {
            stopVusers.setType(SchedulerVusersType.SIMULTANEOUSLY);
            setVusersForRealWorld(stopVusers, sim);
            return stopVusers;
        }

        if (grad.matches()) {
            stopVusers.setType(SchedulerVusersType.GRADUALLY);
            setVusersForRealWorld(stopVusers, grad);
            Ramp ramp = new Ramp(grad.group("users"), grad.group("interval"));
            stopVusers.setRamp(ramp);
            return stopVusers;
        }

        log.debug("Input did not match any pattern → using default StopVusers type");
        return stopVusers;
    }

    private void setVusersForRealWorld(StopVusers stopVusers, Matcher matcher) {
        String stopVusersCount = matcher.group("vusersCount");
        if (workloadType.startsWith("real-world")) {
            if (StringUtils.isNotEmpty(stopVusersCount)) stopVusers.setVusersFromString(stopVusersCount);
        }
    }

    public void validateStopVusersActions(List<Action> actions) {
        List<Action> stopVusersActions = getStopVusersActions(actions);
        boolean anyRunFor = actions.stream()
                .filter(a -> a.getDuration() != null)
                .anyMatch(d -> d.getDuration().getType() == SchedulerDurationType.RUN_FOR);

        if (anyRunFor && stopVusersActions.isEmpty()) {
            StopVusers stop = new StopVusers();
            stop.setType(SchedulerVusersType.SIMULTANEOUSLY);
            actions.add(Action.builder().stopVusers(stop).build());
            stopVusersActions = getStopVusersActions(actions); // Refresh list
        }

        if (workloadType.startsWith("basic") && stopVusersActions.size() > 1) {
            log.warn("Multiple StopVusers actions found. Keeping first, removing {} others.", stopVusersActions.size() - 1);
            Action first = stopVusersActions.get(0);
            actions.removeIf(a -> a.getStopVusers() != null && a != first);
        }
    }

    private List<Action> getStopVusersActions(List<Action> actions) {
        return actions.stream().filter(a -> a.getStopVusers() != null).toList();
    }

}
