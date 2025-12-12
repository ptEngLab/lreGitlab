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

    private static final String KEY_JDK_HOME = "jdkhome";
    private static final String KEY_USER_SPECIFIED_JDK = "userspecifiedjdk";
    private static final String KEY_CLASS_PATHS = "javaenvclasspaths";
    private static final String KEY_VM_PARAMETERS = "javavmparameters";
    private static final String KEY_USE_XBOOT = "usexboot";
    private static final String KEY_ENABLE_CLASSLOADER = "enableclassloaderpervuser";

    // Constants for field names used in error messages
    private static final String FIELD_USER_SPECIFIED_JDK = "UserSpecifiedJdk";
    private static final String FIELD_USE_XBOOT = "UseXboot";
    private static final String FIELD_ENABLE_CLASSLOADER = "EnableClassLoaderPerVuser";

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

        hasConfig |= parseClassPaths(configMap, javaVm);
        hasConfig |= parseUserSpecifiedJdk(configMap, javaVm);
        hasConfig |= parseJdkHome(configMap, javaVm);
        hasConfig |= parseVmParameters(configMap, javaVm);
        hasConfig |= parseUseXboot(configMap, javaVm);
        hasConfig |= parseEnableClassLoader(configMap, javaVm);

        return hasConfig ? javaVm : null;
    }

    private boolean parseClassPaths(Map<String, String> configMap, JavaVM javaVm) {
        String classPaths = configMap.get(KEY_CLASS_PATHS);
        if (StringUtils.isBlank(classPaths)) return false;

        List<String> pathsList = Arrays.stream(classPaths.split(";"))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .toList();

        if (pathsList.isEmpty()) return false;

        javaVm.setJavaEnvClassPaths(pathsList);
        return true;
    }

    private boolean parseUserSpecifiedJdk(Map<String, String> configMap, JavaVM javaVm) {
        if (!configMap.containsKey(KEY_USER_SPECIFIED_JDK)) return false;

        boolean userSpecifiedJdk = parseBooleanStrict(configMap.get(KEY_USER_SPECIFIED_JDK), FIELD_USER_SPECIFIED_JDK);
        javaVm.setUserSpecifiedJdk(userSpecifiedJdk);

        if (userSpecifiedJdk && !configMap.containsKey(KEY_JDK_HOME)) {
            throw new JavaVmException("JdkHome must be specified when UserSpecifiedJdk is true");
        }
        return true;
    }

    private boolean parseJdkHome(Map<String, String> configMap, JavaVM javaVm) {
        String jdkHome = configMap.get(KEY_JDK_HOME);
        if (jdkHome == null) return false;

        if (StringUtils.isBlank(jdkHome)) {
            throw new JavaVmException("JdkHome cannot be empty");
        }
        javaVm.setJdkHome(jdkHome);
        return true;
    }

    private boolean parseVmParameters(Map<String, String> configMap, JavaVM javaVm) {
        String params = configMap.get(KEY_VM_PARAMETERS);
        if (StringUtils.isBlank(params)) return false;

        javaVm.setJavaVmParameters(params.trim());
        return true;
    }

    private boolean parseUseXboot(Map<String, String> configMap, JavaVM javaVm) {
        if (!configMap.containsKey(KEY_USE_XBOOT)) return false;

        javaVm.setUseXboot(parseBooleanStrict(configMap.get(KEY_USE_XBOOT), FIELD_USE_XBOOT));
        return true;
    }

    private boolean parseEnableClassLoader(Map<String, String> configMap, JavaVM javaVm) {
        if (!configMap.containsKey(KEY_ENABLE_CLASSLOADER)) return false;

        javaVm.setEnableClassLoaderPerVuser(
                parseBooleanStrict(configMap.get(KEY_ENABLE_CLASSLOADER), FIELD_ENABLE_CLASSLOADER)
        );
        return true;
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
                if (currentKey != null) map.put(currentKey.trim().toLowerCase(), currentValue.toString().trim());

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