package com.lre.actions.lre.testcontentvalidator.rts;

import com.lre.model.enums.ThinkTimeType;
import com.lre.model.test.testcontent.groups.rts.RTS;
import com.lre.model.test.testcontent.groups.rts.thinktime.ThinkTime;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import static com.lre.actions.helpers.CommonMethods.parsePositive;


@Slf4j
@NoArgsConstructor
public class LreRtsThinkTimeValidator {

    public static class ThinkTimeException extends IllegalArgumentException {
        public ThinkTimeException(String message) {
            super(message);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    private static final String VALID_EXAMPLES = """
            Valid examples:
            # No think time
            ThinkTime: ignore
            # Replay recorded think time, limit to 10 seconds max
            ThinkTime: replay:10
            # Modify think time: limit to 20 seconds, multiply by 1.5
            ThinkTime: modify:20*1.5
            # Random think time: between 50% and 150% of recorded, limit to 30 seconds
            ThinkTime: random:50-150:30
            # Random think time: between 80% and 120% of recorded (no time limit)
            ThinkTime: random:80-120
            # Modify think time: only multiply by 2.0 (no time limit)
            ThinkTime: modify:*2.0
            """;

    public void validateThinkTimeAndAttach(RTS rts, String pacingInput) {
        try {
            ThinkTime tt = parseThinkTime(pacingInput);
            if (rts == null) throw new IllegalArgumentException("RTS cannot be null");
            rts.setThinkTime(tt);

            log.debug("ThinkTime set to RTS: {}", tt);
        } catch (ThinkTimeException e) {
            log.debug("Invalid ThinkTime '{}' -> {}", pacingInput, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error parsing thinkTime '{}'", pacingInput, e);
            throw new ThinkTimeException("Unexpected error parsing thinkTime: " + e.getMessage());
        }
    }

    private ThinkTime parseThinkTime(String input) {
        ThinkTime tt = new ThinkTime(); // defaults to IGNORE
        if (StringUtils.isBlank(input)) return tt;

        String trimmed = input.trim().toLowerCase();
        Pair<String, String> parts = splitTypeAndConfig(trimmed);

        ThinkTimeType type = getThinkTimeType(parts.getLeft());
        tt.setType(type);

        if (type != ThinkTimeType.IGNORE) parseConfiguration(parts.getRight(), tt, type);
        return tt;
    }

    private Pair<String, String> splitTypeAndConfig(String input) {
        int colonIndex = input.indexOf(':');
        return colonIndex < 0 ? Pair.of(input, "") :
                Pair.of(input.substring(0, colonIndex).trim(), input.substring(colonIndex + 1).trim());
    }

    private ThinkTimeType getThinkTimeType(String typePart) {
        try {
            return ThinkTimeType.fromValue(typePart);
        } catch (IllegalArgumentException e) {
            throw new ThinkTimeException("Invalid ThinkTime type: '" + typePart + "'" + VALID_EXAMPLES);
        }
    }

    private void parseConfiguration(String config, ThinkTime tt, ThinkTimeType type) {
        switch (type) {
            case REPLAY -> tt.setLimitThinkTimeSeconds(
                    parsePositive(config, "Replay limit", Integer::parseInt, ThinkTimeException::new, VALID_EXAMPLES));
            case MODIFY -> parseModifyConfig(config, tt);
            case RANDOM -> parseRandomConfig(config, tt);
            case IGNORE -> {
                if (!config.isEmpty()) log.warn("IGNORE ThinkTime config ignored: '{}'", config);
            }
            default -> throw new ThinkTimeException("Unsupported ThinkTime type: " + type + VALID_EXAMPLES);
        }
    }

    private void parseModifyConfig(String config, ThinkTime tt) {
        if (config.isEmpty())
            throw new ThinkTimeException("Modify ThinkTime requires '[limit][*factor]'" + VALID_EXAMPLES);

        if (config.startsWith("*")) {
            String multiplier = config.substring(1).trim();
            if (multiplier.isEmpty()) throw new ThinkTimeException("Multiply factor cannot be empty" + VALID_EXAMPLES);
            tt.setMultiplyFactor(parsePositive(multiplier, "Multiply factor", Double::parseDouble, ThinkTimeException::new, VALID_EXAMPLES));
        } else {
            String[] parts = config.split("\\*");
            if (parts[0].trim().isEmpty())
                throw new ThinkTimeException("Modify limit cannot be empty" + VALID_EXAMPLES);
            tt.setLimitThinkTimeSeconds(parsePositive(parts[0].trim(), "Modify limit", Integer::parseInt, ThinkTimeException::new, VALID_EXAMPLES));
            if (parts.length == 2) {
                if (parts[1].trim().isEmpty())
                    throw new ThinkTimeException("Multiply factor cannot be empty" + VALID_EXAMPLES);
                tt.setMultiplyFactor(parsePositive(parts[1].trim(), "Multiply factor", Double::parseDouble, ThinkTimeException::new, VALID_EXAMPLES));
            }
        }
    }

    private void parseRandomConfig(String config, ThinkTime tt) {
        String[] mainParts = config.split(":");

        if (mainParts.length > 2) throw new ThinkTimeException("Invalid random format: " + config + VALID_EXAMPLES);

        String[] percentParts = mainParts[0].split("-");

        if (percentParts.length != 2)
            throw new ThinkTimeException("Invalid percentage range '" + mainParts[0] + "'" + VALID_EXAMPLES);

        tt.setMinPercentage(parsePositive(percentParts[0].trim(), "Random min percentage",
                Integer::parseInt, ThinkTimeException::new, VALID_EXAMPLES));

        tt.setMaxPercentage(parsePositive(percentParts[1].trim(), "Random max percentage",
                Integer::parseInt, ThinkTimeException::new, VALID_EXAMPLES));

        if (tt.getMinPercentage() > tt.getMaxPercentage())
            throw new ThinkTimeException("Min percentage cannot exceed max." + VALID_EXAMPLES);

        if (mainParts.length == 2)
            tt.setLimitThinkTimeSeconds(parsePositive(mainParts[1].trim(), "Random limit", Integer::parseInt, ThinkTimeException::new, VALID_EXAMPLES));
    }
}
