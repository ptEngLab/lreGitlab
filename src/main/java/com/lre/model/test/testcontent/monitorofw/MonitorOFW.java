package com.lre.model.test.testcontent.monitorofw;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.lre.actions.helpers.ConfigConstants.LRE_API_XMLNS;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonitorOFW {

    @JsonProperty("ID")
    @JacksonXmlProperty(localName = "ID", namespace = LRE_API_XMLNS)
    private Integer id;

}
