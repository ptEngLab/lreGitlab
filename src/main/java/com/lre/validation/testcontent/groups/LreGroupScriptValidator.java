package com.lre.validation.testcontent.groups;

import com.lre.client.api.lre.LreRestApis;
import com.lre.common.exceptions.LreException;
import com.lre.common.utils.CommonUtils;
import com.lre.model.test.testcontent.groups.script.Script;
import com.lre.model.yaml.YamlGroup;
import com.lre.services.git.LreScriptManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class LreGroupScriptValidator {

    private final LreRestApis restApis;
    private final LreScriptManager scriptManager;

    public LreGroupScriptValidator(LreRestApis restApis) {
        this.restApis = restApis;
        this.scriptManager = new LreScriptManager(restApis);
    }

    public Script validateYamlGroupScript(YamlGroup group) {
        Script script = fetchScript(group);
        return new Script(script.getId(), script.getProtocol());      // return only id and protocol type
    }

    private Script fetchScript(YamlGroup group) {
        String script = group.getScript();
        if (StringUtils.isBlank(script)) throw new LreException("Script is required for group: " + group.getName());
        if (StringUtils.isNumeric(script)) return fetchScriptById(Integer.parseInt(script), group.getName());
        else return fetchScriptByName(script, group.getName());
    }


    private Script fetchScriptById(int scriptId, String groupName) {
        Script script = restApis.fetchScriptById(scriptId);
        if (script == null) throw new LreException("No Script found with ID " + scriptId + " for group " + groupName);
        log.debug("Fetched script by ID {} for group {}", scriptId, groupName);
        return script;
    }

    private Script fetchScriptByName(String scriptName, String groupName) {
        String normalizedPath = CommonUtils.normalizePathWithSubject(scriptName);
        if (normalizedPath.equalsIgnoreCase("subject")) {
            throw new LreException("Cannot fetch script: scriptName is null or empty for group " + groupName);
        }

        int lastBackslash = normalizedPath.lastIndexOf('\\');
        String folderPath = normalizedPath.substring(0, lastBackslash);
        String fileName = normalizedPath.substring(lastBackslash + 1);

        return scriptManager.getScriptByName(folderPath, fileName);
    }

}
