package com.lre.core.http;

import com.lre.common.utils.JsonUtils;
import com.lre.model.errors.LreErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.commons.lang3.StringUtils;
import com.lre.common.exceptions.LreException;

import java.util.List;

@Slf4j
public class HttpErrorHandler {

    private static final List<Integer> VALID_STATUS_CODES = List.of(200, 201, 202, 204, 206, 304);

    public static boolean isErrorStatus(int statusCode) {
        return !VALID_STATUS_CODES.contains(statusCode);
    }

    public static void handleError(int status, String content, HttpResponse response, String url) {
        String reason = response != null ? response.getReasonPhrase() : "Unknown";

        if (StringUtils.isNotBlank(content)) {
            try {
                LreErrorResponse errorResponse = JsonUtils.fromJson(content, LreErrorResponse.class);
                if (errorResponse != null && StringUtils.isNotBlank(errorResponse.getExceptionMessage())) {
                    throw new LreException(String.format(
                            "Request failed: Status=%d (%s), URL=%s, ErrorCode=%s, Message=%s, RawContent=%s",
                            status, reason, url,
                            errorResponse.getErrorCode(),
                            errorResponse.getExceptionMessage(),
                            content
                    ));
                }
            } catch (Exception parseEx) {
                log.debug("Failed to parse error response as JSON: {}", content, parseEx);
            }
        }

        throw new LreException(String.format(
                "Request failed: URL=%s, Status=%d (%s), Content=%s",
                url, status, reason, content
        ));
    }
}
