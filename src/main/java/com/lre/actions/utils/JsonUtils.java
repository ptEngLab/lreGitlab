package com.lre.actions.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class JsonUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static <T> T fromJson(String json, Class<T> valueType) {
        try {
            return MAPPER.readValue(json, valueType);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON to {}: {}", valueType.getSimpleName(), e.getMessage());
            throw new RuntimeException("JSON deserialization error", e);
        }
    }

    public static String toJson(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to JSON: {}", e.getMessage());
            throw new RuntimeException("JSON serialization error", e);
        }
    }

    public static <T> List<T> fromJsonArray(String json, Class<T> clazz) {
        try {
            JavaType type = MAPPER.getTypeFactory().constructCollectionType(List.class, clazz);
            return MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON array to List<{}>: {}", clazz.getSimpleName(), e.getMessage());
            throw new RuntimeException("JSON array deserialization error", e);
        }
    }

    public static JsonNode parseField(String json, String fieldName) {
        try {
            JsonNode rootNode = MAPPER.readTree(json);
            JsonNode fieldNode = rootNode.path(fieldName);
            if (fieldNode.isMissingNode()) {
                log.warn("Field '{}' not found in JSON", fieldName);
            }
            return fieldNode;
        } catch (JsonProcessingException e) {
            log.error("Failed to parse field '{}' from JSON: {}", fieldName, e.getMessage());
            throw new RuntimeException("JSON field parsing error", e);
        }
    }
}
