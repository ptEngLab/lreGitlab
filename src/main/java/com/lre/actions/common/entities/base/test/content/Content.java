package com.lre.actions.common.entities.base.test.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import static com.lre.actions.helpers.ConfigConstants.LRE_API_XMLNS;
@Data
public class Content {
    private String xmlns = LRE_API_XMLNS;
    @JsonProperty("Controller")
    private String controller;
}
