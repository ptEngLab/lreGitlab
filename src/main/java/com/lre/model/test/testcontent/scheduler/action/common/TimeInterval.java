package com.lre.model.test.testcontent.scheduler.action.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.lre.actions.utils.ConfigConstants.LRE_API_XMLNS;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeInterval {

    @JsonProperty("Days")
    @JacksonXmlProperty(localName = "Days", namespace = LRE_API_XMLNS)
    private Integer days;

    @JsonProperty("Hours")
    @JacksonXmlProperty(localName = "Hours", namespace = LRE_API_XMLNS)
    private Integer hours;

    @JsonProperty("Minutes")
    @JacksonXmlProperty(localName = "Minutes", namespace = LRE_API_XMLNS)
    private Integer minutes;

    @JsonProperty("Seconds")
    @JacksonXmlProperty(localName = "Seconds", namespace = LRE_API_XMLNS)
    private Integer seconds;

    public static TimeInterval parseTimeInterval(String timeStr) {
        TimeInterval interval = new TimeInterval();
        if (StringUtils.isBlank(timeStr)) return interval;

        Pattern pattern = Pattern.compile("(\\d+)([dhms])");
        Matcher matcher = pattern.matcher(timeStr);
        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            switch (matcher.group(2)) {
                case "d" -> interval.setDays(value);
                case "h" -> interval.setHours(value);
                case "m" -> interval.setMinutes(value);
                case "s" -> interval.setSeconds(value);
            }
        }
        return interval;
    }

}
