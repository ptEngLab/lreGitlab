package com.lre.actions.lre.testcontentvalidator.groups;

import com.lre.actions.apis.LreRestApis;
import com.lre.actions.exceptions.LreException;
import com.lre.actions.helpers.CommonMethods;
import com.lre.model.test.testcontent.groups.Group;
import com.lre.model.test.testcontent.groups.script.Script;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class LreGroupScriptValidator {

    private final LreRestApis restApis;
    private List<Script> scriptCache;

    public LreGroupScriptValidator(LreRestApis restApis) {
        this.restApis = restApis;
        this.scriptCache = getAllScriptsCache(); // initialize cache
    }

    private List<Script> getAllScriptsCache() {
        if (scriptCache == null) scriptCache = restApis.fetchAllScripts();
        return scriptCache;
    }

    public void validateAndSetScript(Group group) {
        Script script = fetchScript(group);
        group.setScript(new Script(script.getId(), script.getProtocol()));      // return only id and protocol type
    }

    private Script fetchScript(Group group) {
        if (group.getYamlScriptId() > 0) {
            try {
                return fetchScriptById(group.getYamlScriptId(), group.getName());
            } catch (LreException e) {
                log.warn("Script ID {} not found for group {}, falling back to name lookup",
                        group.getYamlScriptId(), group.getName());
            }
        }

        if (StringUtils.isNotEmpty(group.getYamlScriptName())) {
            return fetchScriptByName(group.getYamlScriptName(), group.getName());
        }

        throw new LreException("No valid Script found for group: " + group.getName()
                + ". Script ID: " + group.getYamlScriptId()
                + ", Script Name: " + group.getYamlScriptName());
    }

    private Script fetchScriptById(int scriptId, String groupName) {
        Script script = restApis.fetchScriptById(scriptId);
        log.debug("Fetched script by ID {} for group {}", scriptId, groupName);
        return script;
    }

    private Script fetchScriptByName(String scriptName, String groupName) {
        String normalizedPath = CommonMethods.normalizePathWithSubject(scriptName);
        if (StringUtils.isBlank(normalizedPath)) {
            throw new LreException("Cannot fetch script: scriptName is null or empty for group " + groupName);
        }

        Path scriptPath = Paths.get(normalizedPath);
        String folderPath = scriptPath.getParent().toString();
        String fileName = scriptPath.getFileName().toString();

        return getScriptByName(folderPath, fileName);
    }

    private Script getScriptByName(String testFolderPath, String scriptName) {
        log.debug("Searching for script - Folder: {}, Name: {}", testFolderPath, scriptName);
        for (Script script : getAllScriptsCache()) {
            if (testFolderPath.equalsIgnoreCase(script.getTestFolderPath()) &&
                    scriptName.equalsIgnoreCase(script.getName())) {
                return script;
            }
        }
        String msg = String.format("No Script named '%s' was found under folder %s", scriptName, testFolderPath);
        log.warn(msg);
        throw new LreException(msg);
    }

}
