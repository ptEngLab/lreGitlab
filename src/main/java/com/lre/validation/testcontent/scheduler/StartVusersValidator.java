package com.lre.validation.testcontent.scheduler;

import com.lre.model.enums.SchedulerVusersType;
import com.lre.model.test.testcontent.scheduler.action.Action;
import com.lre.model.test.testcontent.scheduler.action.common.Ramp;
import com.lre.model.test.testcontent.scheduler.action.startvusers.StartVusers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Matcher;

import static com.lre.actions.utils.ConfigConstants.*;

@Slf4j
public record StartVusersValidator(String workloadType, int vusersCount) {

    public StartVusers validateStart(String input) {
        StartVusers startVusers = new StartVusers(); // default: SIMULTANEOUSLY

        if (StringUtils.isBlank(input)) {
            if (realWorldByGroup.equalsIgnoreCase(workloadType)) startVusers.setVusers(vusersCount);
            log.debug("WorkloadType {},  StartVusers Input is blank, using default StartVusers type: SIMULTANEOUSLY", workloadType);
            return startVusers;
        }

        Matcher sim = SIMULTANEOUSLY_PATTERN.matcher(input);
        Matcher grad = GRADUALLY_PATTERN.matcher(input);

        if (sim.matches()) {
            startVusers.setType(SchedulerVusersType.SIMULTANEOUSLY);
            setVusersForRealWorld(startVusers, sim);
            return startVusers;
        }

        if (grad.matches()) {
            startVusers.setType(SchedulerVusersType.GRADUALLY);
            setVusersForRealWorld(startVusers, grad);
            Ramp ramp = new Ramp(grad.group("users"), grad.group("interval"));
            startVusers.setRamp(ramp);
            return startVusers;
        }

        log.debug("Input '{}' did not match any known pattern → using default StartVusers type", input);
        return startVusers;
    }

    private void setVusersForRealWorld(StartVusers startVusers, Matcher matcher) {
        String startVusersCount = matcher.group("vusersCount");
        if (realWorldByGroup.equalsIgnoreCase(workloadType) || realWorldByTest.equalsIgnoreCase(workloadType)) {
            if (StringUtils.isNotEmpty(startVusersCount)) startVusers.setVusersFromString(startVusersCount);
            else startVusers.setVusers(vusersCount);
        }
    }

    public void validateStartVusersActions(List<Action> actions) {
        List<Action> startVusersActions = getStartVusersActions(actions);

        if (startVusersActions.isEmpty()) {
            StartVusers start = new StartVusers();
            start.setType(SchedulerVusersType.SIMULTANEOUSLY);
            if (realWorldByGroup.equalsIgnoreCase(workloadType)) start.setVusers(vusersCount);
            actions.add(Action.builder().startVusers(start).build());
            startVusersActions = getStartVusersActions(actions); // Refresh list
        }

        if (workloadType.startsWith("basic") && startVusersActions.size() > 1) {
            log.warn("Multiple StartVusers actions found. Keeping first, removing {} others.", startVusersActions.size() - 1);
            Action first = startVusersActions.get(0);
            actions.removeIf(a -> a.getStartVusers() != null && a != first);
        }
    }

    private List<Action> getStartVusersActions(List<Action> actions) {
        return actions.stream().filter(a -> a.getStartVusers() != null).toList();
    }

}
