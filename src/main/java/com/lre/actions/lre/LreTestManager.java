package com.lre.actions.lre;

import com.lre.actions.apis.LreRestApis;
import com.lre.model.testplan.LreTestPlan;
import com.lre.model.testplan.LreTestPlanCreationRequest;
import com.lre.actions.exceptions.LreException;
import com.lre.model.test.Test;
import com.lre.model.test.testcontent.TestContent;
import com.lre.actions.runmodel.LreTestRunModel;
import com.lre.actions.utils.JsonUtils;
import com.lre.actions.utils.XmlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class LreTestManager {
    private final LreTestRunModel model;
    private final LreRestApis restApis;

    private static final String TEST_NOT_FOUND_BY_ID = "Test with ID '%d' not found";
    private static final String TEST_NOT_FOUND_BY_NAME = "Test name '%s' not found";

    private List<Test> testsCache;
    private List<LreTestPlan> testPlansCache;

    public LreTestManager(LreTestRunModel model, LreRestApis restApis) {
        this.model = model;
        this.restApis = restApis;
        this.testsCache = getAllTestsCached();
        this.testPlansCache = getAllTestPlansCached();
    }


    public void fetchTestDetails() {
        int testId = model.getTestId();
        String testName = model.getTestName();
        if (model.isExistingTest() && testId != 0) findTestById(testId);
        else if (model.isExistingTest() && StringUtils.isNotBlank(testName)) findTestByName(testName);
        else createNewTest();
    }

    private void findTestById(int testId) {
        log.info("Using existing test with ID: {}", testId);
        Test test = restApis.getTest(testId);
        if (test == null) throw new LreException(String.format(TEST_NOT_FOUND_BY_ID, testId));
        model.setTestName(test.getName());
        model.setTestFolderPath(test.getTestFolderPath());
    }

    private void findTestByName(String testName) {
        log.info("Looking up test by name: {}", testName);
        List<Test> tests = getAllTestsCached();
        Test test = tests.stream()
                .filter(t -> t.getName().equalsIgnoreCase(testName))
                .findFirst()
                .orElseThrow(() -> new LreException(String.format(TEST_NOT_FOUND_BY_NAME, testName)));

        model.setTestId(test.getId());
        model.setTestName(test.getName());
        model.setTestFolderPath(test.getTestFolderPath());
    }

    private void createNewTest() {
        validateTestPlan();
        createOrUpdateTest();
    }

    private void validateTestPlan() {
        List<LreTestPlan> currentTestPlans = getAllTestPlansCached();
        Path fullPath = Paths.get(model.getTestFolderPath());
        Path currentPath = Paths.get("");

        Set<String> existingPaths = currentTestPlans.stream()
                .map(plan -> Paths.get(plan.getFullPath()).normalize().toString())
                .collect(Collectors.toSet());

        for (Path pathPart : fullPath) {
            currentPath = currentPath.resolve(pathPart).normalize();
            String currentPathStr = currentPath.toString();

            if (!existingPaths.contains(currentPathStr)) {
                String parentPath = currentPath.getParent() != null ? currentPath.getParent().toString() : "";
                LreTestPlan testPlan = createNewTestPlanPath(parentPath, pathPart.toString());
                currentTestPlans.add(testPlan);
                existingPaths.add(currentPathStr);
            }
        }

        log.debug("Test plan validation completed for path: {}", fullPath);
    }

    private void createOrUpdateTest() {
        String testName = model.getTestName();
        String testFolderPath = model.getTestFolderPath();
        Optional<Test> existingTest = findTestByNameAndFolder(testName, testFolderPath);
        TestContent testContent = getTestContent();
        if (existingTest.isPresent()) updateExistingTest(existingTest.get(), testContent);
        else createNewTest(testName, testFolderPath, testContent);
        testsCache = null; // invalidate cache only after test creation/update
    }

    private Optional<Test> findTestByNameAndFolder(String testName, String testFolderPath) {
        List<Test> tests = getAllTestsCached();
        return tests.stream()
                .filter(test -> test.getName().equalsIgnoreCase(testName) &&
                        test.getTestFolderPath().equalsIgnoreCase(testFolderPath))
                .findFirst();
    }

    private LreTestPlan createNewTestPlanPath(String parentPath, String name) {
        LreTestPlanCreationRequest request = new LreTestPlanCreationRequest(parentPath, name);
        return restApis.createNewTestPlan(JsonUtils.toJson(request));
    }

    private List<Test> getAllTestsCached() {
        if (testsCache == null) testsCache = restApis.getAllTests();
        return testsCache;
    }

    private List<LreTestPlan> getAllTestPlansCached() {
        if (testPlansCache == null) testPlansCache = restApis.getAllTestPlans();
        return testPlansCache;
    }

    private void updateExistingTest(Test existingTest, TestContent testContent) {
        int testId = existingTest.getId();
        restApis.updateTest(testId, XmlUtils.toXml(testContent));
        model.setTestId(testId);
    }

    private void createNewTest(String testName, String testFolderPath, TestContent testContent) {
        log.info("Creating new test: '{}' in folder: '{}'", testName, testFolderPath);
        Test test = new Test(testName, testFolderPath, testContent);
        test.normalizeAfterDeserialization();
        Test createdTest = restApis.createNewTest(XmlUtils.toXml(test));
        model.setTestId(createdTest.getId());
    }

    private TestContent getTestContent() {
        LreTestContentValidator testContentValidator = new LreTestContentValidator(model, restApis);
        return testContentValidator.validateAndGetTestContent();
    }
}
