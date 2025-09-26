package com.lre.actions.lre;

import com.lre.actions.apis.LreRestApis;
import com.lre.model.run.LreRunResponse;
import com.lre.model.run.LreRunStatus;
import com.lre.model.timeslot.TimeslotCheckRequest;
import com.lre.actions.runmodel.LreTestRunModel;
import com.lre.actions.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.lre.actions.helpers.CommonMethods.*;
import static com.lre.actions.helpers.ConfigConstants.DASHBOARD_URL;

@Slf4j
@AllArgsConstructor
public class LreTestExecutor {

    private final LreRestApis restApis;
    private final LreTestRunModel model;
    private final LreTimeslotManager timeslotManager;


    public void executeTestRun() {
        TimeslotCheckRequest request = timeslotManager.buildTimeslotRequest();
        LreRunResponse response = restApis.startRun(model.getTestId(), JsonUtils.toJson(request));
        model.setRunId(response.getQcRunId());
        model.setLreInternalRunId(response.getRunId());
        String dashboardUrl = String.format(DASHBOARD_URL, model.getLreServerUrl(), response.getQcRunId());
        model.setDashboardUrl(dashboardUrl);
        writeRunIdToFile(response.getQcRunId());
        LreRunStatus runStatus = restApis.getRunStatus(model.getRunId());
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

        log.info(logTableDynamic(rows));

    }


}
