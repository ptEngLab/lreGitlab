package com.lre.model.test.testcontent.sla;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.lre.model.test.testcontent.sla.errors.ErrorsPerSecond;
import com.lre.model.test.testcontent.sla.throughput.AvgThroughput;
import com.lre.model.test.testcontent.sla.hits.AvgHitsPerSecond;
import com.lre.model.test.testcontent.sla.hits.TotalHits;
import com.lre.model.test.testcontent.sla.throughput.TotalThroughput;
import com.lre.model.test.testcontent.sla.trt.TxnResTimeAverage;
import com.lre.model.test.testcontent.sla.trt.TxnResTimePercentile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.lre.actions.helpers.ConfigConstants.LRE_API_XMLNS;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SLA {

    @JsonProperty("TransactionResponseTimePercentile")
    @JacksonXmlProperty(localName = "TransactionResponseTimePercentile", namespace = LRE_API_XMLNS)
    private TxnResTimePercentile txnResTimePercentile;

    @JsonProperty("TransactionResponseTimeAverage")
    @JacksonXmlProperty(localName = "TransactionResponseTimeAverage", namespace = LRE_API_XMLNS)
    private TxnResTimeAverage txnResTimeAverage;

    @JsonProperty("ErrorsPerSecond")
    @JacksonXmlProperty(localName = "ErrorsPerSecond", namespace = LRE_API_XMLNS)
    private ErrorsPerSecond errorsPerSecond;

    @JsonProperty("TotalHits")
    @JacksonXmlProperty(localName = "TotalHits", namespace = LRE_API_XMLNS)
    private TotalHits totalHits;

    @JsonProperty("AverageHitsPerSecond")
    @JacksonXmlProperty(localName = "AverageHitsPerSecond", namespace = LRE_API_XMLNS)
    private AvgHitsPerSecond avgHitsPerSecond;

    @JsonProperty("TotalThroughput")
    @JacksonXmlProperty(localName = "TotalThroughput", namespace = LRE_API_XMLNS)
    private TotalThroughput totalThroughput;

    @JsonProperty("AverageThroughput")
    @JacksonXmlProperty(localName = "AverageThroughput", namespace = LRE_API_XMLNS)
    private AvgThroughput avgThroughput;

}
