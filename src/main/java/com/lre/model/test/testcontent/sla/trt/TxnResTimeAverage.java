package com.lre.model.test.testcontent.sla.trt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.lre.model.enums.SlaLoadCriteria;
import com.lre.model.test.testcontent.sla.common.LoadValues;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.lre.actions.helpers.ConfigConstants.LRE_API_XMLNS;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TxnResTimeAverage {

    @JsonProperty("LoadCriterion")
    @JacksonXmlProperty(localName = "LoadCriterion", namespace = LRE_API_XMLNS)
    private SlaLoadCriteria loadCriterion;

    @JsonProperty("LoadValues")
    @JacksonXmlProperty(localName = "LoadValues", namespace = LRE_API_XMLNS)
    private LoadValues loadValues;

    @JsonProperty("Transactions")
    @JacksonXmlElementWrapper(localName = "Transactions", namespace = LRE_API_XMLNS)
    @JacksonXmlProperty(localName = "Transaction", namespace = LRE_API_XMLNS)
    private List<Transaction> transactions;


}
