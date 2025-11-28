package com.lre.services.lre.report.renderer.html;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HtmlTemplateEngine {

    private static final String[] TEMPLATE_KEYS = {
            "TestName", "RunName", "TransactionPassed", "TransactionFailed",
            "Vusers", "TransactionPerSec", "HitsPerSec", "AvgThroughput",
            "Domain", "Project", "TestID", "TestFolder", "TestInstanceID",
            "Controller", "StartTime", "EndTime", "TestDuration", "Errors",
            "ReportLink", "RunID", "LGsUsed", "RunStatus", "RunResult", "StatusBadgeColor", "TransactionTable"
    };

    public static String generateHtmlReport(Map<String, String> testData) {
        String htmlTemplate = loadHtmlTemplate();

        for (String key : TEMPLATE_KEYS) {
            String placeholder = "${" + key + "}";
            String value = testData.getOrDefault(key, "");
            htmlTemplate = htmlTemplate.replace(placeholder, value);
        }

        return htmlTemplate;
    }

    private static String loadHtmlTemplate() {
        try (InputStream inputStream = HtmlTemplateEngine.class.getResourceAsStream("/template.html")) {
            if (inputStream == null) {
                throw new RuntimeException("Template file not found in resources");
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load HTML template", e);
        }
    }
}