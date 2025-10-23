package com.lre.services;

import com.lre.actions.apis.LreRestApis;
import com.lre.actions.exceptions.LreException;
import com.lre.actions.runmodel.LreTestRunModel;
import com.lre.actions.utils.JsonUtils;
import com.lre.actions.utils.XmlUtils;
import com.lre.model.script.LreScript;
import com.lre.model.script.LreScriptUploadReq;
import com.lre.model.test.Test;
import com.lre.model.test.testcontent.TestContent;
import com.lre.model.testplan.LreTestPlan;
import com.lre.model.testplan.LreTestPlanCreationRequest;
import com.lre.validation.testcontent.LreTestContentValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.lre.actions.utils.CommonUtils.normalizePathWithSubject;
import static com.lre.actions.utils.CommonUtils.replaceBackSlash;

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

    public void uploadScriptsFromGitToLre(Path compressedScript) {
        String scriptPathInLre = validateTestPlan();
        uploadScriptsToLre(scriptPathInLre, compressedScript);

    }

    private void findTestById(int testId) {
        log.info("Using existing test with ID: {}", testId);
        Test test = restApis.fetchTest(testId);
        if (test == null) throw new LreException(String.format(TEST_NOT_FOUND_BY_ID, testId));
        model.setTestName(test.getName());
        model.setTestFolderPath(normalizePathWithSubject(test.getTestFolderPath()));
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
        model.setTestFolderPath(normalizePathWithSubject(test.getTestFolderPath()));
    }

    private void createNewTest() {
        validateTestPlan();
        createOrUpdateTest();
    }

    private String validateTestPlan() {
        List<LreTestPlan> currentTestPlans = getAllTestPlansCached();

        String normalizedInput = model.getTestFolderPath();

        if (!normalizedInput.toLowerCase().startsWith("subject\\")) {
            throw new IllegalStateException("Expected path to start with 'Subject\\', but got: " + normalizedInput);
        }

        String[] pathParts = normalizedInput.split("\\\\");
        StringBuilder currentPathBuilder = new StringBuilder("Subject");
        String parentPath = "Subject"; // Start with Subject as initial parent

        Set<String> existingPathStrings = currentTestPlans.stream()
                .map(plan -> normalizePathWithSubject(plan.getFullPath()).toLowerCase())
                .collect(Collectors.toSet());

        // Process subdirectories starting from index 1
        for (int i = 1; i < pathParts.length; i++) {
            String pathPart = pathParts[i];

            // Build current path
            if (i > 1) {
                currentPathBuilder.append("\\");
            }
            currentPathBuilder.append(pathPart);

            String currentPath = currentPathBuilder.toString();
            String currentPathLower = currentPath.toLowerCase();

            if (!existingPathStrings.contains(currentPathLower)) {
                LreTestPlan testPlan = createNewTestPlanPath(parentPath, pathPart);
//                testPlan.setFullPath(currentPath);
                currentTestPlans.add(testPlan);
                existingPathStrings.add(currentPathLower);
            }

            // Update parent for next iteration
            parentPath = currentPath;
        }

        String finalPath = currentPathBuilder.toString();
        log.debug("Test plan validation completed for path: {}", normalizedInput);
        return finalPath;
    }


    private void createOrUpdateTest() {
        String testName = model.getTestName();
        String testFolderPath = model.getTestFolderPath();
        Optional<Test> existingTest = findTestByNameAndFolder(testName, testFolderPath);
        TestContent testContent = new LreTestContentValidator(model, restApis).buildTestContent();
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
        return restApis.createTestPlan(JsonUtils.toJson(request));
    }

    private List<Test> getAllTestsCached() {
        if (testsCache == null) testsCache = restApis.fetchAllTests();
        return testsCache;
    }

    private List<LreTestPlan> getAllTestPlansCached() {
        if (testPlansCache == null) testPlansCache = restApis.fetchAllTestPlans();
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
        Test createdTest = restApis.createTest(XmlUtils.toXml(test));
        model.setTestId(createdTest.getId());
    }

    private void uploadScriptsToLre(String scriptPathInLre, Path compressedScript) {
        LreScriptUploadReq scriptUploadReq = new LreScriptUploadReq(scriptPathInLre);
        LreScript script = restApis.uploadScript(compressedScript, JsonUtils.toJson(scriptUploadReq));
        log.info("Script {}, Folder path {} uploaded successfully", script.getName(), replaceBackSlash(script.getTestFolderPath()));

    }
}
