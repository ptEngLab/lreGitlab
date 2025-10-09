package com.lre.model.test.testcontent.groups.hosts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.lre.model.enums.HostType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.lre.actions.utils.ConfigConstants.LRE_API_XMLNS;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Host {

    @JsonProperty("Name")
    @JacksonXmlProperty(localName = "Name", namespace = LRE_API_XMLNS)
    private String name;

    @JsonProperty("Type")
    @JacksonXmlProperty(localName = "Type", namespace = LRE_API_XMLNS)
    private HostType type;

    @JsonProperty("HostTemplateId")
    @JacksonXmlProperty(localName = "HostTemplateId", namespace = LRE_API_XMLNS)
    private String hostTemplateId;


}
