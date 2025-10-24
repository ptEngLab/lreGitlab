package com.lre.core.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import com.lre.common.exceptions.LreException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Function;

@Slf4j
public class HttpRequestExecutor {

    public static String sendRequest(CloseableHttpClient httpClient, ClassicRequestBuilder requestBuilder) {
        log.debug("Sending request to: {}", requestBuilder.getUri());
        return executeRequest(httpClient, requestBuilder, entity -> {
            try {
                return entity != null ? EntityUtils.toString(entity) : null;
            } catch (IOException | ParseException e) {
                throw new LreException("Error reading response content from " + requestBuilder.getUri(), e);
            }
        });
    }

    public static boolean downloadFile(CloseableHttpClient client, ClassicRequestBuilder reqBuilder, String destPath) {
        if (StringUtils.isBlank(destPath)) {
            throw new LreException("Destination path is blank for downloading file from " + reqBuilder.getUri());
        }

        // Ensure parent directories exist
        File file = new File(destPath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new LreException("Failed to create directory: " + parentDir.getAbsolutePath());
            }
        }

        log.debug("Downloading file to: {}", destPath);
        return executeRequest(client, reqBuilder, entity -> {
            if (entity != null) {
                try (OutputStream out = new FileOutputStream(destPath)) {
                    entity.writeTo(out);
                    log.debug("File downloaded successfully to: {}", destPath);
                    return true;
                } catch (IOException e) {
                    throw new LreException("Error saving file to " + destPath, e);
                }
            } else {
                log.warn("No content received for file download from: {}", reqBuilder.getUri());
                return false;
            }
        });
    }

    private static <T> T executeRequest(CloseableHttpClient httpClient,
                                        ClassicRequestBuilder requestBuilder,
                                        Function<HttpEntity, T> entityProcessor) {
        String url = requestBuilder.getUri().toString();
        try {
            return httpClient.execute(requestBuilder.build(), response -> {
                int status = response.getCode();
                HttpEntity entity = response.getEntity();

                if (HttpErrorHandler.isErrorStatus(status)) {
                    String errorContent = null;
                    if (entity != null) {
                        try {
                            // Read entity content for error handling
                            errorContent = EntityUtils.toString(entity);
                        } catch (IOException | ParseException ex) {
                            log.warn("Failed to read error response body for {}", url, ex);
                        }
                    }
                    HttpErrorHandler.handleError(status, errorContent, response, url);
                    // If handleError doesn't throw an exception (it should), return null
                    return null;
                }

                log.debug("Request completed: {} -> Status: {}", url, status);
                return entityProcessor.apply(entity);
            });
        } catch (IOException e) {
            throw new LreException("I/O error executing request to " + url, e);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof LreException) {
                throw (LreException) e.getCause();
            }
            throw new LreException("Error executing request to " + url, e);
        }
    }
}