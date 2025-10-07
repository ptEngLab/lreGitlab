package com.lre.actions.lre.testcontentvalidator.sla;

import com.lre.actions.exceptions.LreException;
import com.lre.model.enums.SlaLoadCriteria;
import com.lre.model.test.testcontent.TestContent;
import com.lre.model.test.testcontent.sla.SLA;
import com.lre.model.test.testcontent.sla.SLAConfig;
import com.lre.model.test.testcontent.sla.common.Between;
import com.lre.model.test.testcontent.sla.common.BetweenThreshold;
import com.lre.model.test.testcontent.sla.common.LoadValues;
import com.lre.model.test.testcontent.sla.common.Thresholds;
import com.lre.model.test.testcontent.sla.errors.ErrorsPerSecond;
import com.lre.model.test.testcontent.sla.hits.AvgHitsPerSecond;
import com.lre.model.test.testcontent.sla.hits.TotalHits;
import com.lre.model.test.testcontent.sla.throughput.AvgThroughput;
import com.lre.model.test.testcontent.sla.throughput.TotalThroughput;
import com.lre.model.test.testcontent.sla.trt.Transaction;
import com.lre.model.test.testcontent.sla.trt.TxnResTimeAverage;
import com.lre.model.test.testcontent.sla.trt.TxnResTimePercentile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Slf4j
public record SLAValidator(TestContent content) {

    public void validateSLA() {
        SLAConfig config = content.getSlaConfig();
        if (config == null) {
            log.info("No SLA configuration found - skipping validation");
            return;
        }

        validateMutualExclusivity(config);

        SLA sla = new SLA();

        // Average Response Time SLA
        if (hasAvgResponseTimeConfig(config)) {
            sla.setTxnResTimeAverage(createAvgResponseTimeSLA(config));
        }

        // Errors Per Second SLA
        if (hasErrorsPerSecondConfig(config)) {
            sla.setErrorsPerSecond(createErrorsPerSecondSLA(config));
        }

        // Percentile Response Time SLA
        if (hasPercentileResponseTimeConfig(config)) {
            sla.setTxnResTimePercentile(createPercentileResponseTimeSLA(config));
        }

        // Simple SLA types
        if (isNotEmpty(config.getTotalHits()) && config.getTotalHits() > 0) {
            sla.setTotalHits(createTotalHitsSLA(config));
        }

        if (isNotEmpty(config.getAvgHitsPerSecond()) && config.getAvgHitsPerSecond() > 0) {
            sla.setAvgHitsPerSecond(createAverageHitsPerSecondSLA(config));
        }

        if (isNotEmpty(config.getTotalThroughput()) && config.getTotalThroughput() > 0) {
            sla.setTotalThroughput(createTotalThroughputSLA(config));
        }

        if (isNotEmpty(config.getAvgThroughput()) && config.getAvgThroughput() > 0) {
            sla.setAvgThroughput(createAvgThroughputSLA(config));
        }

        content.setSla(sla);

    }

    private void validateMutualExclusivity(SLAConfig config) {
        boolean hasAvg = hasAvgResponseTimeConfig(config);
        boolean hasPercentile = hasPercentileResponseTimeConfig(config);

        if (hasAvg && hasPercentile) {
            throw new LreException(
                    "TransactionResponseTimeAverage and TransactionResponseTimePercentile cannot be used together. " +
                            "Please configure only one response time SLA type."
            );
        }
    }

    private boolean hasAvgResponseTimeConfig(SLAConfig config) {
        return config != null &&
                StringUtils.isNotBlank(config.getAvgResponseTimeLoadCriteria()) &&
                isNotEmpty(config.getAvgResponseTimeLoadRanges()) &&
                isNotEmpty(config.getAvgResponseTimeThresholds());
    }

    private boolean hasPercentileResponseTimeConfig(SLAConfig config) {
        return config != null &&
                isNotEmpty(config.getPercentileResponseTimeThreshold()) &&
                isNotEmpty(config.getPercentileResponseTimeTransactions());
    }

    private boolean hasErrorsPerSecondConfig(SLAConfig config) {
        return config != null &&
                StringUtils.isNotBlank(config.getErrorLoadCriteriaType()) &&
                isNotEmpty(config.getErrorLoadRanges()) &&
                isNotEmpty(config.getErrorThreshold());
    }

    private TxnResTimeAverage createAvgResponseTimeSLA(SLAConfig config) {
        LoadValues loadValues = getLoadValues(config.getAvgResponseTimeLoadRanges());
        TxnResTimeAverage avg = new TxnResTimeAverage();
        avg.setLoadValues(loadValues);
        avg.setLoadCriterion(SlaLoadCriteria.fromValue(normalize(config.getAvgResponseTimeLoadCriteria())));
        avg.setTransactions(buildTransactions(config.getAvgResponseTimeThresholds(), loadValues));
        return avg;
    }

    private TxnResTimePercentile createPercentileResponseTimeSLA(SLAConfig config) {
        TxnResTimePercentile percentile = new TxnResTimePercentile();
        percentile.setPercentile(config.getPercentileResponseTimeThreshold());
        List<Transaction> transactions = getPercentileTransactions(config.getPercentileResponseTimeTransactions());

        if (!isNotEmpty(transactions)) {
            throw new LreException("No transactions defined for Percentile Response Time SLA");
        }
        percentile.setTransactions(transactions);
        return percentile;
    }

    private ErrorsPerSecond createErrorsPerSecondSLA(SLAConfig config) {
        LoadValues loadValues = getLoadValues(config.getErrorLoadRanges());
        ErrorsPerSecond errors = new ErrorsPerSecond();
        errors.setLoadValues(loadValues);
        errors.setLoadCriterion(SlaLoadCriteria.fromValue(normalize(config.getErrorLoadCriteriaType())));
        errors.setThresholds(getThresholdsFromInt(config.getErrorThreshold()));
        return errors;
    }

    private TotalHits createTotalHitsSLA(SLAConfig config) {
        return new TotalHits(config.getTotalHits().floatValue());
    }

    private AvgHitsPerSecond createAverageHitsPerSecondSLA(SLAConfig config) {
        return new AvgHitsPerSecond(config.getAvgHitsPerSecond().floatValue());
    }

    private TotalThroughput createTotalThroughputSLA(SLAConfig config) {
        return new TotalThroughput(config.getTotalThroughput().floatValue());
    }

    private AvgThroughput createAvgThroughputSLA(SLAConfig config) {
        return new AvgThroughput(config.getAvgThroughput().floatValue());
    }

    private List<Transaction> getPercentileTransactions(Map<String, Integer> transactionThresholds) {
        if (!isNotEmpty(transactionThresholds)) {
            return Collections.emptyList();
        }

        List<Transaction> transactions = new ArrayList<>();
        for (var entry : transactionThresholds.entrySet()) {
            String name = entry.getKey();
            Integer threshold = entry.getValue();
            if (!isNotEmpty(threshold)) {
                throw new LreException("Threshold for transaction '" + name + "' cannot be null");
            }
            Transaction t = new Transaction();
            t.setName(name);
            t.setThreshold(threshold.floatValue());
            transactions.add(t);
        }
        return transactions;
    }

    private List<Transaction> buildTransactions(Map<String, List<Integer>> thresholdMatrix, LoadValues loadValues) {
        if (!isNotEmpty(thresholdMatrix) || !isNotEmpty(loadValues)) {
            return Collections.emptyList();
        }

        int expectedCount = (!isNotEmpty(loadValues.getBetween())) ? 2 : loadValues.getBetween().size() + 2;
        List<Transaction> transactions = new ArrayList<>();

        for (var entry : thresholdMatrix.entrySet()) {
            String name = entry.getKey();
            List<Integer> thresholds = entry.getValue();
            validateThresholdCount(expectedCount, thresholds, name);
            Transaction t = new Transaction();
            t.setName(name);
            t.setThresholds(getThresholdsFromInt(thresholds));
            transactions.add(t);
        }
        return transactions;
    }

    private void validateThresholdCount(int expectedCount, List<Integer> thresholds, String txnName) {
        if (!isNotEmpty(thresholds) || thresholds.size() != expectedCount) {
            String errMsg = String.format(
                    "Threshold count mismatch for transaction '%s': expected %d (based on %d load ranges), found %d",
                    txnName, expectedCount, expectedCount - 1,
                    !isNotEmpty(thresholds) ? 0 : thresholds.size()
            );
            throw new LreException(errMsg);
        }
    }

    private Thresholds getThresholdsFromInt(List<Integer> list) {
        if (!isNotEmpty(list)) throw new LreException("Threshold list cannot be empty");
        if (list.size() > 5) throw new LreException("Too many threshold zones. Max 5 values allowed.");

        Thresholds thresholds = new Thresholds();
        List<Float> vals = list.stream().map(Integer::floatValue).toList();

        thresholds.setLessThanThreshold(vals.get(0));
        thresholds.setGreaterThanOrEqualThreshold(vals.get(vals.size() - 1));

        BetweenThreshold betweenThreshold = new BetweenThreshold();
        if (vals.size() > 2) {
            betweenThreshold.setThreshold(vals.subList(1, vals.size() - 1));
        } else {
            betweenThreshold.setThreshold(Collections.emptyList());
        }
        thresholds.setBetweenThreshold(betweenThreshold);

        return thresholds;
    }

    private LoadValues getLoadValues(List<Integer> loadRanges) {
        if (!isNotEmpty(loadRanges)) throw new LreException("LoadRanges cannot be empty");

        // Validate ascending order
        for (int i = 1; i < loadRanges.size(); i++) {
            if (loadRanges.get(i) <= loadRanges.get(i - 1)) {
                throw new LreException("Load ranges must be in ascending order: " + loadRanges);
            }
        }

        List<Float> vals = loadRanges.stream().map(Integer::floatValue).toList();
        LoadValues load = new LoadValues();
        load.setLessThan(vals.get(0));
        load.setGreaterThanOrEqual(vals.get(vals.size() - 1));

        if (vals.size() > 1) {
            List<Between> between = new ArrayList<>();
            for (int i = 0; i < vals.size() - 1; i++) {
                between.add(new Between(vals.get(i), vals.get(i + 1)));
            }
            load.setBetween(between);
        } else {
            load.setBetween(Collections.emptyList());
        }

        return load;
    }


    private String normalize(String s) {
        return s == null ? null : s.trim().toLowerCase(Locale.ROOT).replace(" ", "_");
    }
}