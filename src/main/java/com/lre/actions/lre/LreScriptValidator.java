package com.lre.actions.lre;

import com.lre.actions.apis.LreRestApis;
import com.lre.actions.exceptions.LreException;
import com.lre.actions.helpers.CommonMethods;
import com.lre.model.test.testcontent.TestContent;
import com.lre.model.test.testcontent.groups.Group;
import com.lre.model.test.testcontent.groups.script.Script;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class LreScriptValidator {
    private final LreRestApis restApis;
    private final TestContent content;
    private List<Script> scriptCache;

    public LreScriptValidator(LreRestApis restApis, TestContent content) {
        this.restApis = restApis;
        this.content = content;
        scriptCache = getAllScriptsCache();
    }

    public TestContent validateGroups() {

        if (content.getGroups() != null) {
            for (Group group : content.getGroups()) {
                group.setScript(validateScript(group));
                group.setScriptId(null);
                group.setScriptName(null);
            }
        }
        clearScriptCache();
        return content;

    }

    private Script validateScript(Group group) {
        Script script = fetchScript(group);
        return new Script(script.getId(), script.getProtocol());
    }

    private Script fetchScript(Group group) {
        if (group.getScriptId() > 0) {
            try {
                return fetchScriptById(group.getScriptId(), group.getName());
            } catch (LreException e) {
                log.warn("Script ID {} not found for group {}, falling back to name lookup",
                        group.getScriptId(), group.getName());
            }
        }

        if (StringUtils.isNotEmpty(group.getScriptName())) {
            return fetchScriptByName(group.getScriptName(), group.getName());
        }

        throw new LreException("No valid Script found for group: " + group.getName()
                + ". Script ID: " + group.getScriptId()
                + ", Script Name: " + group.getScriptName());
    }

    private Script fetchScriptById(int scriptId, String groupName) {
        Script script = restApis.getScriptById(scriptId);
        log.debug("Fetched script by ID {} for group {}", scriptId, groupName);
        return script;
    }

    private Script fetchScriptByName(String scriptName, String groupName) {
        String normalizedPath = CommonMethods.normalizePathWithSubject(scriptName);

        if (StringUtils.isBlank(normalizedPath)) {
            throw new LreException("Cannot fetch script: scriptName is null or empty for group " + groupName);
        }

        Path scriptPath = Paths.get(normalizedPath);
        String folderPath = scriptPath.getParent().toString();  // safe
        String fileName = scriptPath.getFileName().toString();

        Script script = getScriptByName(folderPath, fileName);
        log.debug("Fetched script by Name {} for group {}", scriptName, groupName);
        return script;
    }

    private Script getScriptByName(String testFolderPath, String scriptName) {
        List<Script> scripts = getAllScriptsCache();
        log.debug("Searching for script - Folder: {}, Name: {}", testFolderPath, scriptName);
        for (Script script : scripts) {
            String lreScriptPath = script.getTestFolderPath();
            String lreScriptName = script.getName();
            if (testFolderPath.equalsIgnoreCase(lreScriptPath) && scriptName.equalsIgnoreCase(lreScriptName))
                return script;
        }
        String msg = String.format("No Script named '%s' was found under this folder %s", scriptName, testFolderPath);
        log.warn(msg);
        throw new LreException(msg);
    }

    private List<Script> getAllScriptsCache() {
        if (scriptCache == null) scriptCache = restApis.getAllScripts();
        return scriptCache;
    }

    private void clearScriptCache() {
        scriptCache = null;
    }

}
