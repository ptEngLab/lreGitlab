package com.lre.model.test.testcontent.scheduler;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.lre.model.test.testcontent.scheduler.action.Action;
import com.lre.model.test.testcontent.scheduler.action.common.TimeInterval;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.lre.common.constants.ConfigConstants.LRE_API_XMLNS;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JacksonXmlRootElement(localName = "Scheduler", namespace = LRE_API_XMLNS)
@JsonInclude(JsonInclude.Include.NON_EMPTY) // skip null or empty lists in XML
public class Scheduler {

    @JsonProperty("Actions")
    @JacksonXmlElementWrapper(localName = "Actions", namespace = LRE_API_XMLNS)
    @JacksonXmlProperty(localName = "Action", namespace = LRE_API_XMLNS)
    @JsonDeserialize(as = ArrayList.class) // always deserialize as ArrayList
    @Builder.Default
    private List<Action> actions = new ArrayList<>();

    public void setActions(List<Action> actions) {
        this.actions = (actions == null) ? new ArrayList<>() : new ArrayList<>(actions);
    }

    /** Default scheduler for "Basic By Test" */
    public static Scheduler getDefaultSchedulerForBasicByTest() {
        return Scheduler.builder()
                .actions(List.of(
                        Action.initializeDefault(),
                        Action.startVusersDefault(),
                        Action.durationUntilCompletion()
                ))
                .build();
    }

    /** Default scheduler for "Real-World By Test" with total Vusers */
    public static Scheduler getDefaultSchedulerForRBTest(int totalVusers) {
        return Scheduler.builder()
                .actions(List.of(
                        Action.initializeDefault(),
                        Action.startVusers(totalVusers),
                        Action.durationRunFor(TimeInterval.builder().minutes(5).build()),
                        Action.stopVusersDefault()
                ))
                .build();
    }


    /** Default scheduler for "Basic By Group" */
    public static Scheduler getDefaultSchedulerForBasicByGroup() {
        return Scheduler.builder()
                .actions(List.of(
                        Action.startGroupDefault(),
                        Action.initializeDefault(),
                        Action.startVusersDefault(),
                        Action.durationUntilCompletion()
                ))
                .build();
    }

    /** Default scheduler for "Real-World By Group" with total Vusers */
    public static Scheduler getDefaultSchedulerForRBGrp(int totalVusers) {
        return Scheduler.builder()
                .actions(List.of(
                        Action.startGroupDefault(),
                        Action.initializeDefault(),
                        Action.startVusers(totalVusers),
                        Action.durationRunFor(TimeInterval.builder().minutes(5).build()),
                        Action.stopVusers(totalVusers)
                ))
                .build();
    }
}
