package com.lre.actions.lre;

import com.lre.actions.apis.LreRestApis;
import com.lre.actions.common.entities.base.test.Test;
import com.lre.actions.exceptions.LreException;
import com.lre.actions.runmodel.LreTestRunModel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import java.util.List;

@AllArgsConstructor
@Slf4j
public class LreTestManager {
    private final LreRestApis restApis;
    private final LreTestRunModel model;

    public void fetchTestDetails() {
        if (model.isExistingTest() && model.getTestId() != 0) {
            log.info("Using existing test with ID: {}", model.getTestId());
            findTestById(model.getTestId());

        } else if (model.isExistingTest() && StringUtils.isNotBlank(model.getTestName())) {
            log.info("Looking up test by name: {}", model.getTestName());
            findTestByName(model.getTestName());

        } else {
            log.info("Creating new test with name: {}", model.getTestName());
            createNewTest();
        }
        logTestDetails();
    }

    private void findTestById(int testId) {
        Test test = restApis.getTest(testId);
        if (test == null) throw new LreException(String.format("Test with ID '%d' not found", testId));
        model.setTestName(test.getName());
        model.setTestFolderPath(test.getTestFolderPath());
    }

    private void findTestByName(String testName) {
        List<Test> tests = restApis.getAllTests();
        Test test = tests.stream().filter(t -> t.getName().equalsIgnoreCase(testName)).findFirst().orElse(null);
        if (test == null) throw new LreException(String.format("Test name '%s' not found", testName));
        model.setTestId(test.getId());
        model.setTestName(test.getName());
        model.setTestFolderPath(test.getTestFolderPath());
    }

    private void createNewTest() {
        // TODO
    }

    private void logTestDetails() {
        String format = "%-20s : %-20s \n";
        String message = "Resolved test details \n" +
                String.format(format, "Test ID", model.getTestId()) +
                String.format(format, "Test Name", model.getTestName()) +
                String.format(format, "Test Folder", model.getTestFolderPath());
        log.info(message);
    }

}