package com.lre.actions.lre.testcontentvalidator.groups;

import com.lre.model.test.testcontent.groups.Group;
import com.lre.model.test.testcontent.groups.rts.RTS;
import com.lre.model.test.testcontent.groups.rts.selenium.Selenium;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class LreRtsSeleniumValidator {

    public static class SeleniumException extends IllegalArgumentException {
        public SeleniumException(String message) {
            super(message);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    private static final String VALID_EXAMPLES = """
            Valid examples:
            Selenium: "JREPath=C:\\java\\jdk,ClassPath=myclasspath.jar,TestNgFiles=testng.xml"
            Selenium: "JREPath=C:\\java\\jdk,TestNgFiles=testng.xml"
            Selenium: "TestNgFiles=testng.xml"
            """;

    public void validateSeleniumForGroup(Group group) {
        String input = group.getYamlSelenium();
        try {
            Selenium selenium = parseSelenium(input);
            attachSeleniumToGroup(group, selenium);
            log.debug("Selenium configuration applied: {}", selenium);
        } catch (SeleniumException e) {
            log.debug("Invalid Selenium config '{}' -> {}", input, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error parsing Selenium '{}'", input, e);
            throw new SeleniumException("Unexpected error parsing Selenium: " + e.getMessage());
        }
    }

    private void attachSeleniumToGroup(Group group, Selenium selenium) {
        if (selenium == null) return; // don’t attach anything

        RTS rts = group.getRts();
        if (rts == null) group.setRts(rts = new RTS());
        rts.setSeleniumSettings(selenium);
    }

    private Selenium parseSelenium(String input) {
        if (StringUtils.isBlank(input)) return null;

        Map<String, String> configMap = parseKeyValuePairs(input);

        Selenium selenium = new Selenium();
        selenium.setClassPath(configMap.getOrDefault("classpath", null));
        selenium.setJrePath(configMap.getOrDefault("jrepath", null));
        selenium.setTestNgFiles(configMap.getOrDefault("testngfiles", null));

        return selenium;
    }


    /**
     * Parse key=value pairs, allowing commas inside the value for the last pair.
     * Uses LinkedHashMap to preserve insertion order.
     */
    private Map<String, String> parseKeyValuePairs(String input) {
        Map<String, String> map = new HashMap<>();
        for (String part : input.split(",")) {
            String[] kv = part.split("=", 2);
            if (kv.length != 2) {
                throw new SeleniumException("Invalid entry: " + part + ". Expected key=value. " + VALID_EXAMPLES);
            }
            map.put(kv[0].trim().toLowerCase(), kv[1].trim());
        }

        return map;
    }

}