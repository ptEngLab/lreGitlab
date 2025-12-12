package com.lre.client.api.builder;

import com.lre.common.exceptions.LreException;
import lombok.experimental.UtilityClass;
import org.apache.hc.core5.net.URIBuilder;

import java.net.URISyntaxException;
import java.util.Map;

/**
 * Utility class for URL construction operations.
 * All methods are static - cannot be instantiated.
 */
@UtilityClass
public final class UrlUtils {

    /**
     * Joins a base path with a resource path
     */
    public static String path(String base, String resource) {
        return String.format("%s/%s", base, resource);
    }
    
    /**
     * Creates a path with an ID parameter: base/resource/id
     */
    public static String pathWithId(String base, String resource, int id) {
        return String.format("%s/%s/%d", base, resource, id);
    }
    
    /**
     * Creates a path with a sub-resource: base/resource/id/subResource
     */
    public static String pathWithSubResource(String base, String resource, int id, String subResource) {
        return String.format("%s/%s/%d/%s", base, resource, id, subResource);
    }
    
    /**
     * Creates a path with two IDs and a sub-resource: base/resource/id1/subResource/id2/data
     */
    public static String pathWithSubResourceAndData(String base, String resource, int id1, String subResource, int id2) {
        return String.format("%s/%s/%d/%s/%d/data", base, resource, id1, subResource, id2);
    }
    
    /**
     * Adds a single query parameter to a URL
     */
    public static String withQuery(String baseUrl, String key, String value) {
        try {
            URIBuilder builder = new URIBuilder(baseUrl);
            builder.addParameter(key, value);
            return builder.build().toString();
        } catch (URISyntaxException e) {
            throw new LreException("Invalid URL: " + baseUrl, e);
        }
    }
    
    /**
     * Adds multiple query parameters to a URL
     */
    public static String withQueryParams(String baseUrl, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return baseUrl;
        }
        
        try {
            URIBuilder builder = new URIBuilder(baseUrl);
            params.forEach(builder::addParameter);
            return builder.build().toString();
        } catch (URISyntaxException e) {
            throw new LreException("Invalid URL: " + baseUrl, e);
        }
    }
    
    /**
     * Adds a complex query string (for LRE-specific queries)
     */
    public static String withComplexQuery(String baseUrl, String queryString) {
        return withQuery(baseUrl, "query", queryString);
    }
}