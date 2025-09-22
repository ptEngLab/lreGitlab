package com.lre.actions.lre;

import com.lre.actions.apis.LreRestApis;
import com.lre.actions.common.entities.base.timeslot.TimeslotCheckRequest;
import com.lre.actions.common.entities.base.timeslot.TimeslotCheckResponse;
import com.lre.actions.exceptions.LreException;
import com.lre.actions.runmodel.LreTestRunModel;
import com.lre.actions.utils.JsonUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static com.lre.actions.helpers.ConfigConstants.MAX_HOURS;
import static com.lre.actions.helpers.ConfigConstants.MINUTES_PER_HOUR;

@Slf4j
@Getter
public class LreTimeslotManager {

    private final LreRestApis restApis;
    private final LreTestRunModel model;
    private final int hours;
    private final int minutes;
    private final int totalMinutes;



    public LreTimeslotManager(LreRestApis restApis, LreTestRunModel model) {
        this.restApis = restApis;
        this.model = model;
        this.totalMinutes = (model.getTimeslotDurationHours() * MINUTES_PER_HOUR) + model.getTimeslotDurationMinutes();
        this.hours = totalMinutes / MINUTES_PER_HOUR;
        this.minutes = (this.hours == MAX_HOURS) ? 0 : totalMinutes % MINUTES_PER_HOUR;
        if (this.hours > MAX_HOURS) {
            throw new IllegalArgumentException("Timeslot duration exceeds max allowed of " + MAX_HOURS + " hours");
        }
    }

    public TimeslotCheckRequest buildTimeslotRequest() {
        return new TimeslotCheckRequest(
                getTotalMinutes(),
                model.isVirtualUserFlexDaysMode(),
                model.getVirtualUserFlexDaysAmount(),
                model.getLrePostRunAction().getNumericValue(),
                model.getTestInstanceId()
        );
    }

    public void checkTimeslotAvailableForTestId() {
        log.info("Checking timeslot availability for testId {} with duration: {}", model.getTestId(), this);
        TimeslotCheckRequest request = buildTimeslotRequest();
        String payload = JsonUtils.toJson(request);
        log.debug("TimeslotCheckRequest payload: {}", payload);

        TimeslotCheckResponse response = restApis.calculateTimeslotAvailability(model.getTestId(), payload);

        if (response.hasErrors()) {
            log.error("Timeslot is not available for testId {}. Reasons:", model.getTestId());
            int counter = 1;
            for (String reason : response) log.error("  {}, {}", counter++, reason);
            throw new LreException("Timeslot is not available");
        }
    }

    @Override
    public String toString() {
        return String.format("%02d:%02d (hh:mm)", hours, minutes);
    }

}
