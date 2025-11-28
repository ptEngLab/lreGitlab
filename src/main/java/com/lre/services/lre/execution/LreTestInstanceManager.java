package com.lre.services.lre.execution;

import com.lre.client.api.lre.LreRestApis;
import com.lre.model.testset.LreTestSetCreateRequest;
import com.lre.model.testset.LreTestSetFolder;
import com.lre.model.testinstance.LreTestInstance;
import com.lre.model.testinstance.LreTestInstanceCreateRequest;
import com.lre.model.testset.LreTestSet;
import com.lre.model.testset.LreTestSetFolderCreateRequest;
import com.lre.client.runmodel.LreTestRunModel;
import com.lre.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

import static com.lre.common.constants.ConfigConstants.DEFAULT_TEST_SET_FOLDER_NAME;
import static com.lre.common.constants.ConfigConstants.DEFAULT_TEST_SET_NAME;

@Slf4j
public record LreTestInstanceManager(LreRestApis restApis, LreTestRunModel model) {
    public void resolveTestInstance() {
        List<LreTestInstance> testInstances = fetchTestInstancesForTestId();
        int testInstanceId = testInstances.isEmpty() ? createNewTestInstance() : useExistingTestInstance(testInstances);
        model.setTestInstanceId(testInstanceId);
    }

    private List<LreTestInstance> fetchTestInstancesForTestId() {
        int testId = model.getTestId();
        log.info("Fetching test instances for test id: {}", testId);
        List<LreTestInstance> instances = restApis.fetchTestInstances(testId);
        log.debug("Retrieved {} test instances for test id: {}", instances.size(), testId);
        return instances;
    }

    private int useExistingTestInstance(List<LreTestInstance> testInstances) {
        LreTestInstance latestInstance = getLatestTestInstance(testInstances);
        int instanceId = latestInstance.getTestInstanceId();
        log.info("Using existing test instance ID: {}", instanceId);
        return instanceId;
    }

    private int createNewTestInstance() {
        int testId = model.getTestId();
        log.info("Creating new test instance for test id: {}", testId);

        LreTestSet testSet = findOrCreateTestSet();
        int testSetId = testSet.getTestSetId();
        log.debug("Creating test instance for test id: {}, TestSet Id: {}", testId, testSetId);

        LreTestInstanceCreateRequest instanceCreateRequest = new LreTestInstanceCreateRequest(testId, testSetId);
        LreTestInstance testInstance = restApis.createTestInstance(JsonUtils.toJson(instanceCreateRequest));

        log.info("Test Instance created successfully. Instance id: {}", testInstance.getTestInstanceId());
        return testInstance.getTestInstanceId();
    }

    private LreTestSet findOrCreateTestSet() {
        List<LreTestSet> testSets = restApis.fetchAllTestSets();

        if (testSets.isEmpty()) {
            log.info("No test sets found. Creating default test set '{}'", DEFAULT_TEST_SET_NAME);
            int testSetFolderId = getOrCreateDefaultTestSetFolder();
            LreTestSetCreateRequest request = new LreTestSetCreateRequest(testSetFolderId);
            return restApis.createTestSet(JsonUtils.toJson(request));
        }

        LreTestSet latestTestSet = getLatestTestSet(testSets);
        log.info("Found {} existing TestSets. Using TestSet with name '{}', TestSet Id {}",
                testSets.size(), latestTestSet.getTestSetName(), latestTestSet.getTestSetId());
        return latestTestSet;
    }

    private int getOrCreateDefaultTestSetFolder() {
        List<LreTestSetFolder> testSetFolders = restApis.fetchAllTestSetFolders();
        Optional<LreTestSetFolder> existingFolder = testSetFolders.stream()
                .filter(f -> DEFAULT_TEST_SET_FOLDER_NAME.equalsIgnoreCase(f.getTestSetFolderName()))
                .findFirst();

        if (existingFolder.isPresent()) {
            log.info("Default test set folder '{}' already exists", DEFAULT_TEST_SET_FOLDER_NAME);
            return existingFolder.get().getTestSetFolderId();
        }

        log.info("Default test set folder '{}' not found. Creating...", DEFAULT_TEST_SET_FOLDER_NAME);
        LreTestSetFolderCreateRequest request = new LreTestSetFolderCreateRequest();
        LreTestSetFolder folder = restApis.createTestSetFolder(JsonUtils.toJson(request));
        return folder.getTestSetFolderId();
    }

    private LreTestInstance getLatestTestInstance(List<LreTestInstance> instances) {
        return instances.get(instances.size() - 1);
    }

    private LreTestSet getLatestTestSet(List<LreTestSet> testSets) {
        return testSets.get(testSets.size() - 1);
    }
}
