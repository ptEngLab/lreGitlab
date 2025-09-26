package com.lre.actions.lre.testcontentvalidator.groups;

import com.lre.model.enums.PacingStartNewIterationType;
import com.lre.model.test.testcontent.groups.Group;
import com.lre.model.test.testcontent.groups.rts.RTS;
import com.lre.model.test.testcontent.groups.rts.pacing.Pacing;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static com.lre.actions.helpers.CommonMethods.parsePositive;

@Slf4j
@NoArgsConstructor
public class LrePacingValidator {

    public static class PacingException extends IllegalArgumentException {
        public PacingException(String message) { super(message); }
        @Override public synchronized Throwable fillInStackTrace() { return this; }
    }

    private static final String VALID_EXAMPLES = """
        Valid examples:
        Pacing: immediately
        Pacing: fixed delay:5/3
        Pacing: random delay:10-20/2
        Pacing: fixed interval:8
        Pacing: random interval:10-15/5
        """;

    public void validatePacingForGroup(Group group) {
        String input = group.getYamlPacing();
        try {
            Pacing pacing = parsePacing(input);
            attachPacingToGroup(group, pacing);
            log.debug("Pacing set to RTS: {}", pacing);
        } catch (PacingException e) {
            log.debug("Invalid pacing '{}' -> {}", input, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error parsing pacing '{}'", input, e);
            throw new PacingException("Unexpected error parsing pacing: " + e.getMessage());
        }
    }

    private void attachPacingToGroup(Group group, Pacing pacing) {
        RTS rts = group.getRts();
        if (rts == null) group.setRts(rts = new RTS());
        rts.setPacing(pacing);
    }

    private Pacing parsePacing(String input) {
        if (StringUtils.isBlank(input)) return new Pacing();

        String trimmed = input.trim().toLowerCase();
        Pacing pacing = new Pacing();
        Pacing.StartNewIteration sni = new Pacing.StartNewIteration();

        if (trimmed.equals(PacingStartNewIterationType.IMMEDIATELY.getValue())) {
            pacing.setStartNewIteration(sni);
            return pacing;
        }

        String[] parts = trimmed.split(":");
        if (parts.length != 2) throw new PacingException("Invalid format, expected 'type:config'" + VALID_EXAMPLES);

        PacingStartNewIterationType type = PacingStartNewIterationType.fromString(parts[0].trim());
        sni.setType(type);

        applyTimingAndIterations(parts[1].trim(), pacing, sni, type);

        pacing.setStartNewIteration(sni);
        return pacing;
    }

    private void applyTimingAndIterations(String config, Pacing pacing, Pacing.StartNewIteration sni, PacingStartNewIterationType type) {
        String[] split = config.split("/");
        int iterations = split.length == 2 ?
                parsePositive(split[1].trim(), "iterations", Integer::parseInt, PacingException::new, VALID_EXAMPLES) : 1;
        pacing.setNumberOfIterations(iterations);

        String timing = split[0].trim();
        switch (type.getConfigShape()) {
            case NONE -> { if (!timing.isEmpty()) log.warn("Ignoring timing '{}' for type {}", timing, type.getValue()); }
            case FIXED -> sni.setDelayOfSeconds(parsePositive(timing, type.getValue(), Integer::parseInt, PacingException::new, VALID_EXAMPLES));
            case RANGE -> {
                String[] range = timing.split("-");
                if (range.length != 2) throw new PacingException("Expected range 'min-max' for type " + type.getValue());
                int min = parsePositive(range[0].trim(), type.getValue(), Integer::parseInt, PacingException::new, VALID_EXAMPLES);
                int max = parsePositive(range[1].trim(), type.getValue(), Integer::parseInt, PacingException::new, VALID_EXAMPLES);
                if (min > max) throw new PacingException("Min cannot exceed max: " + min + "-" + max);
                sni.setDelayAtRangeOfSeconds(min);
                sni.setDelayAtRangeToSeconds(max);
            }
        }
    }
}
