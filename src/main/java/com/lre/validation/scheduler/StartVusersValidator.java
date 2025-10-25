package com.lre.validation.scheduler;

import com.lre.common.utils.WorkloadUtils;
import com.lre.model.enums.SchedulerVusersType;
import com.lre.model.test.testcontent.scheduler.action.Action;
import com.lre.model.test.testcontent.scheduler.action.common.Ramp;
import com.lre.model.test.testcontent.scheduler.action.startvusers.StartVusers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Matcher;

import static com.lre.common.constants.ConfigConstants.*;

@Slf4j
public record StartVusersValidator(String workloadType, int vusersCount) {

    public StartVusers validateStart(String input) {
        StartVusers startVusers = new StartVusers(); // default: SIMULTANEOUSLY

        if (StringUtils.isBlank(input)) {
            if (WorkloadUtils.isRealWorldByGroup(workloadType)) startVusers.setVusers(vusersCount);
            log.debug("WorkloadType {},  StartVusers Input is blank, using default StartVusers type: SIMULTANEOUSLY", workloadType);
            return startVusers;
        }

        input = StringUtils.trimToEmpty(input).toLowerCase();
        Matcher sim = SIMULTANEOUSLY_PATTERN.matcher(input);
        Matcher grad = GRADUALLY_PATTERN.matcher(input);

        if (sim.matches()) {
            startVusers.setType(SchedulerVusersType.SIMULTANEOUSLY);
            setVusersForRealWorld(startVusers, sim);
        } else if (grad.matches()) {
            startVusers.setType(SchedulerVusersType.GRADUALLY);
            setVusersForRealWorld(startVusers, grad);
            startVusers.setRamp(new Ramp(grad.group("users"), grad.group("interval")));
        } else {
            log.debug("[Scheduler] Unknown StartVusers '{}'. Using default SIMULTANEOUSLY.", input);
        }

        return startVusers;
    }

    private void setVusersForRealWorld(StartVusers startVusers, Matcher matcher) {
        String startVusersCount = matcher.group("vusersCount");
        if (WorkloadUtils.isRealWorld(workloadType)) {
            if (StringUtils.isNotEmpty(startVusersCount)) startVusers.setVusersFromString(startVusersCount);
            else startVusers.setVusers(vusersCount);
        }
    }

    public void validateStartVusersActions(List<Action> actions) {
        List<Action> startVusersActions = getStartVusersActions(actions);

        if (startVusersActions.isEmpty()) {
            log.warn("[Scheduler] No StartVusers action found. Adding default.");
            StartVusers start = new StartVusers();
            start.setType(SchedulerVusersType.SIMULTANEOUSLY);
            if (WorkloadUtils.isRealWorldByGroup(workloadType)) start.setVusers(vusersCount);
            actions.add(Action.builder().startVusers(start).build());
            startVusersActions = getStartVusersActions(actions);
        }

        if (WorkloadUtils.isBasic(workloadType) && startVusersActions.size() > 1) {
            log.warn("[Scheduler] Multiple StartVusers found. Keeping first.");
            Action first = startVusersActions.get(0);
            actions.removeIf(a -> a.getStartVusers() != null && a != first);
        }
    }


    private List<Action> getStartVusersActions(List<Action> actions) {
        return actions.stream().filter(a -> a.getStartVusers() != null).toList();
    }

}
