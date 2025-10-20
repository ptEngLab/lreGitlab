package com.lre.services;

import com.lre.actions.apis.LreRestApis;
import com.lre.model.run.LreRunResponse;
import com.lre.model.run.LreRunStatus;
import com.lre.model.timeslot.TimeslotCheckRequest;
import com.lre.actions.runmodel.LreTestRunModel;
import com.lre.actions.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import static com.lre.actions.utils.CommonUtils.*;
import static com.lre.actions.utils.ConfigConstants.DASHBOARD_URL;

@Slf4j
public record LreTestExecutor(LreRestApis restApis, LreTestRunModel model, LreTimeslotManager timeslotManager) {

    public void executeTestRun() {
        TimeslotCheckRequest request = timeslotManager.buildTimeslotRequest();
        LreRunResponse response = restApis.startRun(model.getTestId(), JsonUtils.toJson(request));
        model.setRunId(response.getQcRunId());
        model.setLreInternalRunId(response.getRunId());
        String dashboardUrl = String.format(DASHBOARD_URL, model.getLreServerUrl(), response.getQcRunId());
        model.setDashboardUrl(dashboardUrl);
        writeRunIdToFile(response.getQcRunId());
        LreRunStatus runStatus = restApis.fetchRunStatus(model.getRunId());
        model.setTimeslotId(runStatus.getTimeslotId());
        printInitMessage();
    }

    private void printInitMessage() {
        String[][] rows = {
                {
                        "Domain: " + model.getDomain(),
                        "Project: " + model.getProject(),
                        "Test Id: " + model.getTestId()
                },
                {
                        "Test Name: " + model.getTestName(),
                        "Test Folder: " + model.getTestFolderPath(),
                        "Test Instance Id: " + model.getTestInstanceId()

                },
                {
                        "Timeslot Duration: " + timeslotManager,
                        "Timeslot id: " + model.getTimeslotId(),
                        "Post Run Action: " + model.getLrePostRunAction().getAction()

                }
        };

        log.info(logTable(rows));

    }


}
