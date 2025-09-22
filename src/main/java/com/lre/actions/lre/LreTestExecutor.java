package com.lre.actions.lre;

import com.lre.actions.apis.LreRestApis;
import com.lre.actions.common.entities.base.run.LreRunResponse;
import com.lre.actions.common.entities.base.timeslot.TimeslotCheckRequest;
import com.lre.actions.runmodel.LreTestRunModel;
import com.lre.actions.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.lre.actions.helpers.CommonMethods.writeRunIdToFile;
import static com.lre.actions.helpers.ConfigConstants.DASHBOARD_URL;

@Slf4j
@AllArgsConstructor
public class LreTestExecutor {

    private final LreRestApis restApis;
    private final LreTestRunModel model;
    private final LreTimeslotManager timeslotManager;


    public void executeTestRun() {
        printInitMessage();
        TimeslotCheckRequest request = timeslotManager.buildTimeslotRequest();
        LreRunResponse response = restApis.startRun(model.getTestId(), JsonUtils.toJson(request));
        model.setRunId(response.getQcRunId());
        model.setLreInternalRunId(response.getRunId());
        String dashboardUrl = String.format(DASHBOARD_URL, model.getLreServerUrl(), response.getQcRunId());
        model.setDashboardUrl(dashboardUrl);
        log.info("Run started successfully. Run ID: {}, Dashboard URL: {}", model.getRunId(), model.getDashboardUrl());
        writeRunIdToFile(response.getQcRunId());

    }

    private void printInitMessage() {
        String format = "%-20s : %-20s \n";
        String msg = "Executing LoadTest\n" + String.format(format, "Domain", model.getDomain()) +
                String.format(format, "Project", model.getProject()) +
                String.format(format, "Test Id", model.getTestId()) +
                String.format(format, "Test Name", model.getTestName()) +
                String.format(format, "Test Instance Id", model.getTestInstanceId()) +
                String.format(format, "Timeslot Id", "Will be created") +
                String.format(format, "Timeslot duration", timeslotManager) +
                String.format(format, "Post Run Action", model.getLrePostRunAction().getAction());
        log.info(msg);
    }
}
