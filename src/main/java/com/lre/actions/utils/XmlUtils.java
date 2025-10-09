package com.lre.actions.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class XmlUtils {
    private static final XmlMapper MAPPER = new XmlMapper();

    static {
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
        MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    }

    public static String toXml(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to XML: {}", e.getMessage());
            throw new RuntimeException("XML serialization error", e);
        }
    }
}
