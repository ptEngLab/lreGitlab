package com.lre.validation.rts;

import com.lre.model.test.testcontent.groups.rts.RTS;
import com.lre.model.test.testcontent.groups.rts.javavm.JavaVM;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Slf4j
public class LreRtsJavaVmValidator {

    public static class JavaVmException extends IllegalArgumentException {
        public JavaVmException(String message) {
            super(message);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    private static final String VALID_EXAMPLES = """
            Valid examples:
            
            JavaVM: "UserSpecifiedJdk=true,JdkHome=/path/to/jdk"
            
            JavaVM: "JavaVmParameters=-Xmx512m,EnableClassLoaderPerVuser=true"
            
            JavaVM: "JavaEnvClassPaths=/path1.jar;/path2.jar,UseXboot=false"
            
            JavaVM: "UserSpecifiedJdk=false,JavaVmParameters=-Xms256m -Xmx1024m -verbose:gc"
            
            JavaVM: "JavaEnvClassPaths=lib\\junit.jar;lib\\hamcrest.jar,UserSpecifiedJdk=true,JdkHome=C:\\\\Program Files\\\\Java\\\\jdk-11,JavaVmParameters=-Dapp.config=test.properties,UseXboot=true,EnableClassLoaderPerVuser=false"
            
            JavaVM: "EnableClassLoaderPerVuser=true,JavaVmParameters=-Xmx256m -Ddebug=true"
            
            JavaVM: "JavaEnvClassPaths=user_binaries\\\\myapp.jar,UserSpecifiedJdk=true,JdkHome=/usr/lib/jvm/java-17"
            
            JavaVM: "UseXboot=false,EnableClassLoaderPerVuser=true,JavaVmParameters=-Xms128m -Xmx512m -Dspring.profiles.active=test"
            """;

    public void validateJavaVmAndAttach(RTS rts, String input) {
        try {
            JavaVM javaVm = parseJavaVm(input);
            if (javaVm == null) return; // don’t attach anything
            if (rts == null) throw new IllegalArgumentException("RTS cannot be null");
            rts.setJavaVM(javaVm);
            log.debug("JavaVm configuration applied: {}", javaVm);
        } catch (JavaVmException e) {
            log.warn("Invalid JavaVm config '{}': {}", input, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error parsing JavaVm '{}'", input, e);
            throw new JavaVmException("Unexpected error parsing JavaVm: " + e.getMessage());
        }
    }

    private JavaVM parseJavaVm(String input) {
        if (StringUtils.isBlank(input)) {
            return null;
        }

        Map<String, String> configMap = parseKeyValuePairs(input);
        JavaVM javaVm = new JavaVM();

        boolean hasConfig = false;

        // Parse JavaEnvClassPaths
        if (configMap.containsKey("javaenvclasspaths")) {
            String classPaths = configMap.get("javaenvclasspaths");
            if (StringUtils.isNotBlank(classPaths)) {
                List<String> pathsList = new ArrayList<>();
                for (String path : classPaths.split(";")) {
                    String normalized = path.trim();
                    if (StringUtils.isNotBlank(normalized)) {
                        pathsList.add(normalized);
                    }
                }
                if (!pathsList.isEmpty()) {
                    javaVm.setJavaEnvClassPaths(pathsList);
                    hasConfig = true;
                }
            }
        }

        // UserSpecifiedJdk
        if (configMap.containsKey("userspecifiedjdk")) {
            String value = configMap.get("userspecifiedjdk");
            boolean userSpecifiedJdk = parseBooleanStrict(value, "UserSpecifiedJdk");
            javaVm.setUserSpecifiedJdk(userSpecifiedJdk);
            hasConfig = true;

            if (userSpecifiedJdk && !configMap.containsKey("jdkhome")) {
                throw new JavaVmException("JdkHome must be specified when UserSpecifiedJdk is true");
            }
        }

        // JdkHome
        if (configMap.containsKey("jdkhome")) {
            String jdkHome = configMap.get("jdkhome");
            if (StringUtils.isBlank(jdkHome)) {
                throw new JavaVmException("JdkHome cannot be empty");
            }
            javaVm.setJdkHome(jdkHome);
            hasConfig = true;
        }

        // JavaVmParameters
        if (configMap.containsKey("javavmparameters")) {
            String params = configMap.get("javavmparameters");
            if (StringUtils.isNotBlank(params)) {
                javaVm.setJavaVmParameters(params.trim());
                hasConfig = true;
            }
        }

        // UseXboot
        if (configMap.containsKey("usexboot")) {
            String value = configMap.get("usexboot");
            javaVm.setUseXboot(parseBooleanStrict(value, "UseXboot"));
            hasConfig = true;
        }

        // EnableClassLoaderPerVuser
        if (configMap.containsKey("enableclassloaderpervuser")) {
            String value = configMap.get("enableclassloaderpervuser");
            javaVm.setEnableClassLoaderPerVuser(parseBooleanStrict(value, "EnableClassLoaderPerVuser"));
            hasConfig = true;
        }

        return hasConfig ? javaVm : null;
    }

    /**
     * Parse key=value pairs, allowing commas inside the value for the last pair.
     * Uses LinkedHashMap to preserve insertion order.
     */
    private Map<String, String> parseKeyValuePairs(String input) {
        Map<String, String> map = new LinkedHashMap<>();
        String[] parts = input.split(",");

        String currentKey = null;
        StringBuilder currentValue = new StringBuilder();

        for (String part : parts) {
            if (part.contains("=")) {
                // Save previous key-value pair
                if (currentKey != null) {
                    map.put(currentKey.trim().toLowerCase(), currentValue.toString().trim());
                }
                String[] kv = part.split("=", 2);
                currentKey = kv[0].trim().toLowerCase();
                currentValue = new StringBuilder(kv[1].trim());
            } else {
                // Append comma-containing value
                if (currentKey != null && !currentValue.isEmpty()) {
                    currentValue.append(",").append(part.trim());
                } else {
                    throw new JavaVmException("Invalid entry: " + part + ". Expected key=value. " + VALID_EXAMPLES);
                }
            }
        }

        // Save the last key-value pair
        if (currentKey != null) {
            map.put(currentKey.trim().toLowerCase(), currentValue.toString().trim());
        }

        return map;
    }

    private boolean parseBooleanStrict(String value, String fieldName) {
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;
        throw new JavaVmException(fieldName + " must be 'true' or 'false', got: " + value);
    }
}
