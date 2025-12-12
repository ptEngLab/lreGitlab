package com.lre.validation.trending;

import com.lre.common.exceptions.LreException;
import com.lre.model.test.testcontent.TestContent;
import com.lre.model.test.testcontent.automatictrending.AutomaticTrending;
import com.lre.model.yaml.YamlAutomaticTrending;
import com.lre.model.yaml.YamlTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public record AutomaticTrendingValidator(TestContent content, YamlTest yamlTest) {

    public void validateAutomaticTrending() {
        YamlAutomaticTrending yamlTrending = yamlTest.getAutomaticTrending();
        if (yamlTrending == null) return;
        AutomaticTrending automaticTrending = createAutomaticTrendingFromYaml(yamlTrending);
        content.setAutomaticTrending(automaticTrending);
    }

    private AutomaticTrending createAutomaticTrendingFromYaml(YamlAutomaticTrending yamlTrending) {
        AutomaticTrending automaticTrending = new AutomaticTrending();
        setBasicProperties(automaticTrending, yamlTrending);
        setMaxRunsOption(automaticTrending, yamlTrending);
        setTrendRangeAndTimes(automaticTrending, yamlTrending);
        return automaticTrending;
    }

    private void setBasicProperties(AutomaticTrending automaticTrending, YamlAutomaticTrending yamlTrending) {
        if (yamlTrending.getReportId() != null) automaticTrending.setReportId(yamlTrending.getReportId());
        if (yamlTrending.getMaxRuns() != null) automaticTrending.setMaxRuns(yamlTrending.getMaxRuns());

    }


    private void setMaxRunsOption(AutomaticTrending automaticTrending, YamlAutomaticTrending yamlTrending) {
        String value = yamlTrending.getOnMaxRuns();

        if (StringUtils.isNotBlank(value)) {
            try {
                AutomaticTrending.MaxRunsReachedOption onMaxRuns = AutomaticTrending.MaxRunsReachedOption.valueOf(value.trim());
                automaticTrending.setOnMaxRuns(onMaxRuns);
            } catch (IllegalArgumentException e) {
                throw new LreException("Invalid max runs option: " + yamlTrending.getOnMaxRuns());
            }
        }
    }

    private void setTrendRangeAndTimes(AutomaticTrending automaticTrending, YamlAutomaticTrending yamlTrending) {
        if (StringUtils.isNotBlank(yamlTrending.getTrendRange())) {
            AutomaticTrending.TrendRangeType trendRange = parseTrendRange(yamlTrending.getTrendRange());
            automaticTrending.setTrendRange(trendRange);

            if (trendRange == AutomaticTrending.TrendRangeType.COMPLETE_RUN) {
                automaticTrending.setStartTime(null);
                automaticTrending.setEndTime(null);
            } else applyTimeSettings(automaticTrending, yamlTrending);
        }
    }

    private AutomaticTrending.TrendRangeType parseTrendRange(String trendRangeValue) {
        try {
            return AutomaticTrending.TrendRangeType.valueOf(trendRangeValue);
        } catch (IllegalArgumentException e) {
            throw new LreException("Invalid trend range type: " + trendRangeValue);
        }
    }


    private void applyTimeSettings(AutomaticTrending automaticTrending, YamlAutomaticTrending yamlTrending) {
        if (yamlTrending.getStartTime() != null) automaticTrending.setStartTime(yamlTrending.getStartTime());
        if (yamlTrending.getEndTime() != null) automaticTrending.setEndTime(yamlTrending.getEndTime());
        if (automaticTrending.getStartTime() != null && automaticTrending.getEndTime() != null
                && automaticTrending.getStartTime() >= automaticTrending.getEndTime()) {
            throw new LreException("StartTime must be less than EndTime for PartOfRun trend range");
        }
        log.debug("Applied time settings for PartOfRun trend range");
    }
}
