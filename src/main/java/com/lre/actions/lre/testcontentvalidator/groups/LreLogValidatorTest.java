package com.lre.actions.lre.testcontentvalidator.groups;

import com.lre.model.enums.LogOptionsType;
import com.lre.model.enums.LogType;
import com.lre.model.test.testcontent.groups.Group;
import com.lre.model.test.testcontent.groups.rts.log.Log;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class LreLogValidatorTest {

    private final LreLogValidator validator = new LreLogValidator();

    static Stream<TestCase> logScenarios() {
        return Stream.of(
                // IGNORE
                new TestCase("ignore", LogType.IGNORE, null, null, null, null, null),

                // STANDARD
                new TestCase("standard:always", LogType.STANDARD, LogOptionsType.ALWAYS, null, null, null, null),
                new TestCase("standard:on error:20", LogType.STANDARD, LogOptionsType.ON_ERROR, 20, null, null, null),
                new TestCase("standard:on error:1", LogType.STANDARD, LogOptionsType.ON_ERROR, 1, null, null, null),

                // EXTENDED minimal
                new TestCase("extended:always", LogType.EXTENDED, LogOptionsType.ALWAYS, null, false, false, false),
                new TestCase("extended:on error:50", LogType.EXTENDED, LogOptionsType.ON_ERROR, 50, false, false, false),

                // EXTENDED all flags
                new TestCase("extended:on error:50:substitution,server,trace", LogType.EXTENDED, LogOptionsType.ON_ERROR, 50, true, true, true),
                new TestCase("extended:on error:100:substitution,server,trace", LogType.EXTENDED, LogOptionsType.ON_ERROR, 100, true, true, true),
                new TestCase("extended:always:substitution,server,trace", LogType.EXTENDED, LogOptionsType.ALWAYS, null, true, true, true),

                // EXTENDED single flags
                new TestCase("extended:always:substitution", LogType.EXTENDED, LogOptionsType.ALWAYS, null, true, false, false),
                new TestCase("extended:always:server", LogType.EXTENDED, LogOptionsType.ALWAYS, null, false, true, false),
                new TestCase("extended:always:trace", LogType.EXTENDED, LogOptionsType.ALWAYS, null, false, false, true),
                new TestCase("extended:on error:30:server", LogType.EXTENDED, LogOptionsType.ON_ERROR, 30, false, true, false),
                new TestCase("extended:on error:15:trace", LogType.EXTENDED, LogOptionsType.ON_ERROR, 15, false, false, true),
                new TestCase("extended:on error:20:server", LogType.EXTENDED, LogOptionsType.ON_ERROR, 20, false, true, false),

                // EXTENDED two-flag combinations
                new TestCase("extended:on error:10:substitution,server", LogType.EXTENDED, LogOptionsType.ON_ERROR, 10, true, true, false),
                new TestCase("extended:on error:25:substitution,trace", LogType.EXTENDED, LogOptionsType.ON_ERROR, 25, true, false, true),
                new TestCase("extended:on error:30:server,trace", LogType.EXTENDED, LogOptionsType.ON_ERROR, 30, false, true, true),
                new TestCase("extended:always:substitution,trace", LogType.EXTENDED, LogOptionsType.ALWAYS, null, true, false, true),
                new TestCase("extended:always:server,trace", LogType.EXTENDED, LogOptionsType.ALWAYS, null, false, true, true)
        );
    }

    @ParameterizedTest
    @MethodSource("logScenarios")
    void testLogParsing(TestCase testCase) {
        Group group = new Group();
        group.setYamlLog(testCase.input);

        validator.validateLogForGroup(group);

        Log log = group.getRts().getLreLog();
        assertEquals(testCase.expectedType, log.getType());
        if (testCase.expectedOptions != null) {
            assertNotNull(log.getLogOptions());
            assertEquals(testCase.expectedOptions, log.getLogOptions().getType());
            assertEquals(testCase.expectedCache, log.getLogOptions().getCacheSize());
        }
        assertEquals(testCase.expectedSubstitution, log.getParametersSubstitution());
        assertEquals(testCase.expectedServer, log.getDataReturnedByServer());
        assertEquals(testCase.expectedTrace, log.getAdvanceTrace());
    }

    private record TestCase(String input,
                            LogType expectedType,
                            LogOptionsType expectedOptions,
                            Integer expectedCache,
                            Boolean expectedSubstitution,
                            Boolean expectedServer,
                            Boolean expectedTrace) {
    }
}
