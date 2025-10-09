package com.lre.model.test.testcontent.sla.trt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.lre.actions.utils.ConfigConstants.LRE_API_XMLNS;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TxnResTimePercentile {

    @JsonProperty("Percentile")
    @JacksonXmlProperty(localName = "Percentile", namespace = LRE_API_XMLNS)
    private int percentile;

    @JsonProperty("Transactions")
    @JacksonXmlElementWrapper(localName = "Transactions", namespace = LRE_API_XMLNS)
    @JacksonXmlProperty(localName = "Transaction", namespace = LRE_API_XMLNS)
    List<Transaction> transactions;

}
