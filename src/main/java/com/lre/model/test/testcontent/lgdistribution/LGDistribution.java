package com.lre.model.test.testcontent.lgdistribution;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.lre.model.enums.LGDistributionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.lre.actions.helpers.ConfigConstants.LRE_API_XMLNS;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LGDistribution {

    @JsonProperty("Type")
    @JacksonXmlProperty(localName = "Type", namespace = LRE_API_XMLNS)
    private LGDistributionType type;

    @JsonProperty("Amount")
    @JacksonXmlProperty(localName = "Amount", namespace = LRE_API_XMLNS)
    private Integer amount;


    public LGDistribution(LGDistributionType type) {
        this(type, null);
    }

}
