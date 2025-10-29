package com.lre.core.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lre.client.runmodel.EmailConfigModel;
import com.lre.client.runmodel.GitTestRunModel;
import com.lre.client.runmodel.LreTestRunModel;
import com.lre.model.enums.Operation;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ReadConfigFile {

    private final ConfigParser configParser;
    private final ConfigValidator configValidator;
    private final ConfigMapper configMapper;
    private Map<String, Object> cachedParameters;

    public ReadConfigFile(String configFilePath, Operation operation) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File configFile = new File(configFilePath);
        JsonNode configContent = configFile.exists()
                ? mapper.readTree(configFile)
                : mapper.createObjectNode();

        ParameterResolver resolver = new ParameterResolver(configContent);
        this.configParser = new ConfigParser(resolver, operation);
        this.configValidator = new ConfigValidator(resolver);
        this.configMapper = new ConfigMapper();
    }

    /**
     * Builds the LRE test run model.
     */
    public LreTestRunModel buildLreTestRunModel() {
        try {
            return configMapper.mapToLreModel(getParameters());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Builds the GitLab test run model
     */
    public GitTestRunModel buildGitTestRunModel() {
        return configMapper.mapToGitModel(getParameters());
    }

    /**
     * Builds the Email Config model
     */
    public EmailConfigModel buildEmailConfigModel() {
        return configMapper.mapToEmailModel(getParameters());
    }

    /**
     * Returns parsed and validated parameters (cached).
     */
    public Map<String, Object> getParameters() {
        if (cachedParameters == null) {
            cachedParameters = configParser.parseParameters(ParameterDefinitions.getDefinitions());
            configValidator.validate(cachedParameters);
        }
        return cachedParameters;
    }

}