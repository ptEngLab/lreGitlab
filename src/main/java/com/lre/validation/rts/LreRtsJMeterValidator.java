package com.lre.validation.rts;

import com.lre.model.test.testcontent.groups.rts.RTS;
import com.lre.model.test.testcontent.groups.rts.jmeter.JMeter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class LreRtsJMeterValidator {

    public static class JMeterException extends IllegalArgumentException {
        public JMeterException(String message) {
            super(message);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    private static final String VALID_EXAMPLES = """
            Valid examples:
            
            JMeter: "StartMeasurements=true"
            
            JMeter: "StartMeasurements=false"
            
            JMeter: "StartMeasurements=true,JMeterUseDefaultPort=true,JMeterMinPort=60000,JMeterMaxPort=60010"
            
            JMeter: "StartMeasurements=true,JMeterUseDefaultPort=false,JMeterHomePath=c:/downloads/jmeterhome,JREPath=c:/jre/jrehome,AdditionalParameters=-Xmx1024m,-Xms512m"
            """;

    public void validateJMeterAndAttach(RTS rts, String input) {
        try {
            JMeter jmeter = parseJMeter(input);
            if (jmeter == null) return; // don’t attach anything
            if (rts == null) throw new IllegalArgumentException("RTS cannot be null");
            rts.setJmeterSettings(jmeter);
            log.debug("JMeter configuration applied: {}", jmeter);
        } catch (JMeterException e) {
            log.debug("Invalid JMeter config '{}' -> {}", input, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error parsing JMeter '{}'", input, e);
            throw new JMeterException("Unexpected error parsing JMeter: " + e.getMessage());
        }
    }

    private JMeter parseJMeter(String input) {
        if (StringUtils.isBlank(input)) return null;

        Map<String, String> configMap = parseKeyValuePairs(input);

        JMeter jmeter = new JMeter();
        setStartMeasurements(jmeter, configMap);
        setJMeterUseDefaultPortAndPorts(jmeter, configMap);

        // Optional fields with safe defaults
        jmeter.setJMeterHomePath(configMap.get("jmeterhomepath"));
        jmeter.setJrePath(configMap.get("jrepath"));
        jmeter.setAdditionalParameters(configMap.getOrDefault("additionalparameters", null));


        return jmeter;
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
                    map.put(currentKey.toLowerCase(), currentValue.toString().trim());
                }
                String[] kv = part.split("=", 2);
                currentKey = kv[0].trim();
                currentValue = new StringBuilder(kv[1].trim());
            } else {
                // Append comma-containing value
                if (!currentValue.isEmpty()) {
                    currentValue.append(",").append(part.trim());
                } else {
                    throw new JMeterException("Invalid entry: " + part + ". Expected key=value. " + VALID_EXAMPLES);
                }
            }
        }

        // Save the last key-value pair
        if (currentKey != null) {
            map.put(currentKey.toLowerCase(), currentValue.toString().trim());
        }

        return map;
    }

    private void setStartMeasurements(JMeter jmeter, Map<String, String> map) {
        String sm = map.getOrDefault("startmeasurements", "true");
        jmeter.setStartMeasurements(parseBooleanStrict(sm, "StartMeasurements"));
    }

    private void setJMeterUseDefaultPortAndPorts(JMeter jmeter, Map<String, String> map) {
        String udpValue = map.getOrDefault("jmeterusedefaultport", "false");
        boolean useDefaultPort = parseBooleanStrict(udpValue, "JMeterUseDefaultPort");

        if (!map.containsKey("jmeterusedefaultport")) {
            log.warn("jmeterusedefaultport not specified. Using default false.");
        }
        jmeter.setJMeterUseDefaultPort(useDefaultPort);

        if (useDefaultPort) {
            String min = map.get("jmeterminport");
            String max = map.get("jmetermaxport");
            if (min == null || max == null) {
                throw new JMeterException("JMeterMinPort and JMeterMaxPort required when using default ports. " + VALID_EXAMPLES);
            }
            try {
                int minPort = Integer.parseInt(min.trim());
                int maxPort = Integer.parseInt(max.trim());
                if (minPort < 1 || maxPort > 65535 || minPort > maxPort) {
                    throw new JMeterException("Ports must be 1-65535 and Min <= Max.");
                }
                jmeter.setJMeterMinPort(minPort);
                jmeter.setJMeterMaxPort(maxPort);
            } catch (NumberFormatException e) {
                throw new JMeterException("JMeterMinPort/JMeterMaxPort must be integers. " + VALID_EXAMPLES);
            }
        }
    }

    private boolean parseBooleanStrict(String value, String fieldName) {
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;
        throw new JMeterException(fieldName + " must be 'true' or 'false', got: " + value);
    }
}