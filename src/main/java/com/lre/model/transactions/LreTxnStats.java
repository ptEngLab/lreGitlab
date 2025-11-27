package com.lre.model.transactions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LreTxnStats {

    private String scriptName;
    private String transactionName;
    private int transactionCount;
    private double minimum;
    private double maximum;
    private double average;
    private double stdDeviation;
    private int pass;
    private int fail;
    private double p50;
    private double p90;
    private double p95;
    private double p99;

    public LreTxnStats(
            String scriptName,
            String transactionName,
            int transactionCount,
            double minimum,
            double maximum,
            double average,
            double stdDeviation,
            int pass,
            int fail,
            double p50,
            double p90,
            double p95,
            double p99
    ) {
        this.scriptName = scriptName;
        this.transactionName = transactionName;
        this.transactionCount = transactionCount;
        this.minimum = minimum;
        this.maximum = maximum;
        this.average = average;
        this.stdDeviation = stdDeviation;
        this.pass = pass;
        this.fail = fail;
        this.p50 = p50;
        this.p90 = p90;
        this.p95 = p95;
        this.p99 = p99;
    }

}
