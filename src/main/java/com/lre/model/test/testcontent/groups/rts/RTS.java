package com.lre.model.test.testcontent.groups.rts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.lre.model.test.testcontent.groups.rts.javavm.JavaVM;
import com.lre.model.test.testcontent.groups.rts.jmeter.JMeter;
import com.lre.model.test.testcontent.groups.rts.log.Log;
import com.lre.model.test.testcontent.groups.rts.pacing.Pacing;
import com.lre.model.test.testcontent.groups.rts.selenium.Selenium;
import com.lre.model.test.testcontent.groups.rts.thinktime.ThinkTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.lre.common.constants.ConfigConstants.LRE_API_XMLNS;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RTS {

    @JsonProperty("Name")
    @JacksonXmlProperty(localName = "Name", namespace = LRE_API_XMLNS)
    private String name;

    @JsonProperty("Pacing")
    @JacksonXmlProperty(localName = "Pacing", namespace = LRE_API_XMLNS)
    private Pacing pacing;

    @JsonProperty("ThinkTime")
    @JacksonXmlProperty(localName = "ThinkTime", namespace = LRE_API_XMLNS)
    private ThinkTime thinkTime;

    @JsonProperty("Log")
    @JacksonXmlProperty(localName = "Log", namespace = LRE_API_XMLNS)
    private Log lreLog;

    @JsonProperty("JavaVM")
    @JacksonXmlProperty(localName = "JavaVM", namespace = LRE_API_XMLNS)
    private JavaVM javaVM;

    @JsonProperty("JMeter")
    @JacksonXmlProperty(localName = "JMeter", namespace = LRE_API_XMLNS)
    private JMeter jmeterSettings;

    @JsonProperty("Selenium")
    @JacksonXmlProperty(localName = "Selenium", namespace = LRE_API_XMLNS)
    private Selenium seleniumSettings;

}