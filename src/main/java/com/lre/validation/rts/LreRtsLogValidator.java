package com.lre.validation.rts;

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
        public LogException(String message) {
            super(message);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
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

        String[] tokens = tokenize(input);
        if (tokens.length == 0) return logObj;

        logObj.setType(parseLogType(tokens[0]));

        switch (logObj.getType()) {
            case IGNORE -> handleIgnore(logObj, tokens);
            case STANDARD -> handleStandard(logObj, tokens);
            case EXTENDED -> handleExtended(logObj, tokens);
            default -> clearFlags(logObj);
        }

        return logObj;
    }

    private String[] tokenize(String input) {
        return Arrays.stream(input.trim().toLowerCase().split(":"))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .toArray(String[]::new);
    }

    private void handleIgnore(Log logObj, String[] tokens) {
        clearFlags(logObj);
        logObj.setLogOptions(null);
        if (tokens.length > 1) log.warn("Ignore type cannot have additional parameters, ignoring them");
    }

    private void handleStandard(Log logObj, String[] tokens) {
        logObj.setLogOptions(parseOptions(tokens));
        int flagStartIndex = getFlagStartIndex(logObj, tokens);
        if (tokens.length > flagStartIndex) log.warn("Standard type cannot have logging flags, ignoring them");
        clearFlags(logObj);
    }

    private void handleExtended(Log logObj, String[] tokens) {
        logObj.setLogOptions(parseOptions(tokens));
        int flagStartIndex = getFlagStartIndex(logObj, tokens);

        if (tokens.length > flagStartIndex) {
            Set<String> flags = parseFlags(tokens, flagStartIndex);
            logObj.setParametersSubstitution(flags.contains("substitution"));
            logObj.setDataReturnedByServer(flags.contains("server"));
            logObj.setAdvanceTrace(flags.contains("trace"));
        } else {
            // Default flags for EXTENDED
            logObj.setParametersSubstitution(false);
            logObj.setDataReturnedByServer(false);
            logObj.setAdvanceTrace(false);
        }
    }

    private int getFlagStartIndex(Log logObj, String[] tokens) {
        Log.LogOptions options = logObj.getLogOptions();
        if (options != null && options.getType() == LogOptionsType.ON_ERROR &&
                tokens.length > 2 && StringUtils.isNumeric(tokens[2])) {
            return 3;
        }
        return 2;
    }

    private Set<String> parseFlags(String[] tokens, int startIndex) {
        return Arrays.stream(tokens, startIndex, tokens.length)
                .map(String::trim)
                .filter(f -> !f.isEmpty())
                .filter(f -> {
                    if (!VALID_FLAGS.contains(f)) {
                        log.warn("Invalid flag: '{}'. Valid flags are: {}", f, VALID_FLAGS);
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toSet());
    }

    private void clearFlags(Log logObj) {
        logObj.setParametersSubstitution(null);
        logObj.setDataReturnedByServer(null);
        logObj.setAdvanceTrace(null);
    }

    private LogType parseLogType(String token) {
        try {
            return LogType.fromValue(token);
        } catch (IllegalArgumentException e) {
            throw new LogException("Invalid Log type: '" + token + "'\n" + VALID_EXAMPLES);
        }
    }

    private LogOptionsType parseOptionsType(String token) {
        try {
            return LogOptionsType.fromValue(token);
        } catch (IllegalArgumentException e) {
            throw new LogException("Invalid Log option: '" + token + "'\n" + VALID_EXAMPLES);
        }
    }

    private Log.LogOptions parseOptions(String[] tokens) {
        Log.LogOptions options = new Log.LogOptions();
        LogOptionsType type = tokens.length > 1 ? parseOptionsType(tokens[1]) : LogOptionsType.ON_ERROR;
        options.setType(type);

        if (type == LogOptionsType.ON_ERROR) {
            options.setCacheSize(parseCacheSize(tokens));
        } else {
            options.setCacheSize(null);
        }
        return options;
    }

    private Integer parseCacheSize(String[] tokens) {
        if (tokens.length > 2 && StringUtils.isNumeric(tokens[2])) {
            return validateCacheSize(Integer.parseInt(tokens[2]));
        }
        return null;
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
            case IGNORE, STANDARD -> clearFlags(lreLog);
            case EXTENDED -> setDefaultFlags(lreLog);
            default -> { /* no-op */ }
        }

        Log.LogOptions options = lreLog.getLogOptions();
        if (options == null && lreLog.getType() != LogType.IGNORE) {
            options = new Log.LogOptions();
            lreLog.setLogOptions(options);
        }

        if (options != null && options.getType() == LogOptionsType.ON_ERROR) {
            if (options.getCacheSize() == null) {
                options.setCacheSize(DEFAULT_CACHE_SIZE);
            }
        } else if (options != null) {
            options.setCacheSize(null);
        }
    }

    private void setDefaultFlags(Log lreLog) {
        if (lreLog.getParametersSubstitution() == null) lreLog.setParametersSubstitution(false);
        if (lreLog.getDataReturnedByServer() == null) lreLog.setDataReturnedByServer(false);
        if (lreLog.getAdvanceTrace() == null) lreLog.setAdvanceTrace(false);
    }
}
