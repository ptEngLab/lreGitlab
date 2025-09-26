package com.lre.model.run;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

@Getter
public enum PostRunAction {
    COLLATE_AND_ANALYSE("Collate and Analyse", 2,
            EnumSet.of(RunState.FINISHED, RunState.RUN_FAILURE, RunState.CANCELED)),

    COLLATE("Collate Results", 1,
            EnumSet.of(RunState.BEFORE_CREATING_ANALYSIS_DATA, RunState.RUN_FAILURE, RunState.CANCELED)),

    DO_NOTHING("Do Not Collate", 0,
            EnumSet.of(RunState.BEFORE_COLLATING_RESULTS, RunState.RUN_FAILURE, RunState.CANCELED));

    private final String action;
    private final int numericValue;
    private final Set<RunState> terminalStates;

    PostRunAction(String action, int numericValue, Set<RunState> terminalStates) {
        this.action = action;
        this.numericValue = numericValue;
        this.terminalStates = terminalStates;
    }

    @JsonValue
    public String value() {
        return action;
    }

    @JsonCreator
    public static PostRunAction fromString(String action) {
        for (PostRunAction postRunAction : PostRunAction.values()) {
            if (postRunAction.getAction().equalsIgnoreCase(action)) return postRunAction;
        }
        throw new IllegalArgumentException("Unknown PostRunAction: " + action);
    }

    public static PostRunAction fromNumericValue(int numericValue) {
        for (PostRunAction postRunAction : PostRunAction.values()) {
            if (postRunAction.getNumericValue() == numericValue) return postRunAction;
        }
        throw new IllegalArgumentException("Unknown numeric value for PostRunAction: " + numericValue);
    }

    /**
     * Check if a given RunState is a terminal for this PostRunAction
     */
    public boolean isTerminal(RunState state) {
        return terminalStates.contains(state);
    }
}
