package com.lre.model.test.testcontent.scheduler.action;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.lre.model.enums.SchedulerDurationType;
import com.lre.model.enums.SchedulerInitializeType;
import com.lre.model.enums.SchedulerStartGroupType;
import com.lre.model.enums.SchedulerVusersType;
import com.lre.model.test.testcontent.scheduler.action.common.TimeInterval;
import com.lre.model.test.testcontent.scheduler.action.duration.Duration;
import com.lre.model.test.testcontent.scheduler.action.initialize.Initialize;
import com.lre.model.test.testcontent.scheduler.action.startgroup.StartGroup;
import com.lre.model.test.testcontent.scheduler.action.startvusers.StartVusers;
import com.lre.model.test.testcontent.scheduler.action.stopvusers.StopVusers;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.lre.common.constants.ConfigConstants.LRE_API_XMLNS;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JacksonXmlRootElement(localName = "Action", namespace = LRE_API_XMLNS)
public class Action {

    @JsonProperty("Initialize")
    @JacksonXmlProperty(localName = "Initialize", namespace = LRE_API_XMLNS)
    private Initialize initialize;

    @JsonProperty("StartVusers")
    @JacksonXmlProperty(localName = "StartVusers", namespace = LRE_API_XMLNS)
    private StartVusers startVusers;

    @JsonProperty("StopVusers")
    @JacksonXmlProperty(localName = "StopVusers", namespace = LRE_API_XMLNS)
    private StopVusers stopVusers;

    @JsonProperty("Duration")
    @JacksonXmlProperty(localName = "Duration", namespace = LRE_API_XMLNS)
    private Duration duration;

    @JsonProperty("StartGroup")
    @JacksonXmlProperty(localName = "StartGroup", namespace = LRE_API_XMLNS)
    private StartGroup startGroup;

    public static Action startGroupDefault() {
        StartGroup startGroup = new StartGroup();
        startGroup.setType(SchedulerStartGroupType.IMMEDIATELY);
        return Action.builder().startGroup(startGroup).build();
    }

    public static Action initializeDefault() {
        Initialize init = new Initialize();
        init.setType(SchedulerInitializeType.JUST_BEFORE_VUSER_RUNS);
        return Action.builder().initialize(init).build();
    }

    public static Action startVusersDefault() {
        StartVusers start = new StartVusers();
        start.setType(SchedulerVusersType.SIMULTANEOUSLY);
        return Action.builder().startVusers(start).build();

    }

    public static Action stopVusersDefault() {
        StopVusers stopVusers = new StopVusers();
        stopVusers.setType(SchedulerVusersType.SIMULTANEOUSLY);
        return Action.builder().stopVusers(stopVusers).build();
    }

    public static Action durationUntilCompletion() {
        Duration duration = new Duration();
        duration.setType(SchedulerDurationType.UNTIL_COMPLETION);
        return Action.builder().duration(duration).build();
    }

    public static Action startVusers(int totalVusers) {
        StartVusers start = new StartVusers();
        start.setType(SchedulerVusersType.SIMULTANEOUSLY);
        start.setVusers(totalVusers);
        return Action.builder().startVusers(start).build();
    }

    public static Action stopVusers(int totalVusers) {
        StopVusers stopVusers = new StopVusers();
        stopVusers.setType(SchedulerVusersType.SIMULTANEOUSLY);
        stopVusers.setVusers(totalVusers);
        return Action.builder().stopVusers(stopVusers).build();
    }


    public static Action durationRunFor(TimeInterval interval) {
        Duration duration = new Duration();
        duration.setType(SchedulerDurationType.RUN_FOR);
        duration.setTimeInterval(interval);
        return Action.builder().duration(duration).build();
    }

}
