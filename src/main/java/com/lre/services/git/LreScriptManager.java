package com.lre.services.git;

import com.lre.client.api.lre.LreRestApis;
import com.lre.common.exceptions.LreException;
import com.lre.client.runmodel.LreTestRunModel;
import com.lre.model.test.testcontent.groups.script.Script;
import com.lre.services.lre.LreTestManager;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.List;

/**
 * Handles uploading and deleting scripts in LoadRunner Enterprise.
 */
@Slf4j
public class LreScriptManager {
    private final LreRestApis lreRestApis;
    private final List<Script> scriptCache;

    public LreScriptManager(LreRestApis restApis) {
        this.lreRestApis = restApis;
        this.scriptCache = restApis.fetchAllScripts();
    }
    /**
     * Uploads a script to LRE
     */
    public void upload(LreTestRunModel lreModel, Path scriptZip) {
        new LreTestManager(lreModel, lreRestApis).uploadScriptsFromGitToLre(scriptZip);
        log.debug("Uploaded script {}: zip: {}", lreModel.getTestName(), scriptZip.getFileName());
    }

    /**
     * Deletes a script from LRE
     */
    public void delete(String folderPath, String scriptName) {
            Script lreScript = getScriptByName(folderPath, scriptName);
            lreRestApis.deleteScript(lreScript.getId());
            log.debug("Deleted script: {} , {}", folderPath, scriptName);
    }


    public Script getScriptByName(String testFolderPath, String scriptName) {
        log.debug("Searching for script - Folder: {}, Name: {}", testFolderPath, scriptName);
        for (Script script : scriptCache) {
            String scriptFolderPath = script.getTestFolderPath();
            String scriptFileName = script.getName();
            if (testFolderPath.equalsIgnoreCase(scriptFolderPath) && scriptName.equalsIgnoreCase(scriptFileName)) {
                return script;
            }
        }
        String msg = String.format("No Script named '%s' was found under folder %s", scriptName, testFolderPath);
        log.warn(msg);
        throw new LreException(msg);
    }
}