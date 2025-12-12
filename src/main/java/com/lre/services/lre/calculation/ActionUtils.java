package com.lre.services.lre.calculation;

import com.lre.model.test.testcontent.scheduler.action.Action;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@UtilityClass
public final class ActionUtils {

    public static <T> T getFirstAction(List<Action> actions,
                                       Function<Action, T> extractor,
                                       String context) {
        return actions.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing required action for " + context));
    }
}