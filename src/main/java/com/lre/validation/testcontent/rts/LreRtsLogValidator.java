package com.lre.validation.testcontent.rts;

import com.lre.model.enums.LogOptionsType;
import com.lre.model.enums.LogType;
import com.lre.model.test.testcontent.groups.rts.RTS;
import com.lre.model.test.testcontent.groups.rts.log.Log;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor
@Slf4j
public class LreRtsLogValidator {

    private static final int DEFAULT_CACHE_SIZE = 1;
    private static final int MAX_CACHE_SIZE = 100;
    private static final Set<String> VALID_FLAGS = Set.of("substitution", "server", "trace");

    public static class LogException extends IllegalArgumentException {
        public LogException(String message) { super(message); }
        @Override public synchronized Throwable fillInStackTrace() { return this; }
    }

    private static final String VALID_EXAMPLES = """
            Valid examples:
            Log: ignore
            Log: standard:always
            Log: standard:on error:20
            Log: extended:always
            Log: extended:on error:50:substitution,server,trace
            """;

    public void validateLogAndAttach(RTS rts, String input) {
        try {
            Log logObj = parseLog(input);
            validateBusinessRules(logObj);
            if (rts == null) throw new IllegalArgumentException("RTS cannot be null");
            rts.setLreLog(logObj);
            log.debug("Log set to RTS: {}", logObj);
        } catch (LogException e) {
            log.debug("Invalid Log '{}' -> {}", input, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error parsing Log '{}'", input, e);
            throw new LogException("Unexpected error parsing Log: " + e.getMessage());
        }
    }

    private Log parseLog(String input) {
        Log logObj = new Log();
        if (StringUtils.isBlank(input)) return logObj;

        String[] tokens = Arrays.stream(input.trim().toLowerCase().split(":"))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .toArray(String[]::new);

        if (tokens.length == 0) return logObj;

        logObj.setType(parseLogType(tokens[0]));

        if (logObj.getType() == LogType.IGNORE) {
            logObj.setLogOptions(null);
            logObj.setParametersSubstitution(null);
            logObj.setDataReturnedByServer(null);
            logObj.setAdvanceTrace(null);
            if (tokens.length > 1) log.warn("Ignore type cannot have additional parameters, ignoring them");
            return logObj;
        }

        logObj.setLogOptions(parseOptions(tokens));

        int flagStartIndex = 2;
        Log.LogOptions options = logObj.getLogOptions();
        if (options != null && options.getType() == LogOptionsType.ON_ERROR &&
                tokens.length > 2 && StringUtils.isNumeric(tokens[2])) {
            flagStartIndex = 3;
        }

        // STANDARD type: ignore extra flags
        if (logObj.getType() == LogType.STANDARD && tokens.length > flagStartIndex) {
            log.warn("Standard type cannot have logging flags, ignoring them");
            logObj.setParametersSubstitution(null);
            logObj.setDataReturnedByServer(null);
            logObj.setAdvanceTrace(null);
            return logObj;
        }

        // EXTENDED type: parse flags
        if (logObj.getType() == LogType.EXTENDED && tokens.length > flagStartIndex) {
            String flagsStr = String.join(",", Arrays.copyOfRange(tokens, flagStartIndex, tokens.length));
            Set<String> flags = Arrays.stream(flagsStr.split(","))
                    .map(String::trim)
                    .filter(f -> !f.isEmpty())
                    .filter(f -> {
                        boolean valid = VALID_FLAGS.contains(f);
                        if (!valid) log.warn("Invalid flag: '{}'. Valid flags are: substitution, server, trace.", f);
                        return valid;
                    })
                    .collect(Collectors.toSet());

            logObj.setParametersSubstitution(flags.contains("substitution"));
            logObj.setDataReturnedByServer(flags.contains("server"));
            logObj.setAdvanceTrace(flags.contains("trace"));
        } else if (logObj.getType() == LogType.EXTENDED) {
            // Default flags for EXTENDED
            logObj.setParametersSubstitution(false);
            logObj.setDataReturnedByServer(false);
            logObj.setAdvanceTrace(false);
        } else {
            // STANDARD or IGNORE
            logObj.setParametersSubstitution(null);
            logObj.setDataReturnedByServer(null);
            logObj.setAdvanceTrace(null);
        }

        return logObj;
    }

    private LogType parseLogType(String token) {
        try { return LogType.fromValue(token); }
        catch (IllegalArgumentException e) { throw new LogException("Invalid Log type: '" + token + "'\n" + VALID_EXAMPLES); }
    }

    private LogOptionsType parseOptionsType(String token) {
        try { return LogOptionsType.fromValue(token); }
        catch (IllegalArgumentException e) { throw new LogException("Invalid Log option: '" + token + "'\n" + VALID_EXAMPLES); }
    }

    private Log.LogOptions parseOptions(String[] tokens) {
        Log.LogOptions options = new Log.LogOptions();
        LogOptionsType type = tokens.length > 1 ? parseOptionsType(tokens[1]) : LogOptionsType.ON_ERROR;
        options.setType(type);

        if (type == LogOptionsType.ON_ERROR && tokens.length > 2 && StringUtils.isNumeric(tokens[2])) {
            options.setCacheSize(validateCacheSize(Integer.parseInt(tokens[2])));
        } else if (tokens.length > 2 && StringUtils.isNumeric(tokens[2])) {
            log.warn("Cache size only allowed with 'on error' type, ignoring: {}", tokens[2]);
            options.setCacheSize(null);
        } else {
            options.setCacheSize(null);
        }
        return options;
    }

    private int validateCacheSize(int size) {
        if (size < 1 || size > MAX_CACHE_SIZE) {
            log.warn("Cache size must be between 1 and 100, defaulting to {}", MAX_CACHE_SIZE);
            return MAX_CACHE_SIZE;
        }
        return size;
    }

    private void validateBusinessRules(Log lreLog) {
        switch (lreLog.getType()) {
            case IGNORE, STANDARD -> clearAllFlags(lreLog);
            case EXTENDED -> setDefaultFlags(lreLog);
        }

        Log.LogOptions options = lreLog.getLogOptions();
        if (options == null && lreLog.getType() != LogType.IGNORE) {
            options = new Log.LogOptions();
            lreLog.setLogOptions(options);
        }

        if (options != null) {
            if (options.getType() == LogOptionsType.ON_ERROR) {
                if (options.getCacheSize() == null) options.setCacheSize(DEFAULT_CACHE_SIZE);
            } else {
                // Clear cache for any non-ON_ERROR type
                options.setCacheSize(null);
            }
        }
    }

    private void clearAllFlags(Log lreLog) {
        lreLog.setParametersSubstitution(null);
        lreLog.setDataReturnedByServer(null);
        lreLog.setAdvanceTrace(null);
    }

    private void setDefaultFlags(Log lreLog) {
        if (lreLog.getParametersSubstitution() == null) lreLog.setParametersSubstitution(false);
        if (lreLog.getDataReturnedByServer() == null) lreLog.setDataReturnedByServer(false);
        if (lreLog.getAdvanceTrace() == null) lreLog.setAdvanceTrace(false);
    }

}
