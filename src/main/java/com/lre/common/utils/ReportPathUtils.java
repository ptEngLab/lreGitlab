package com.lre.common.utils;

import lombok.experimental.UtilityClass;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.lre.common.constants.ConfigConstants.*;

@UtilityClass
public class ReportPathUtils {

    public static Path buildExtractedReportPath(String workspace, String reportType) {

        // Base folder: <workspace>/artifacts/LreReports/HtmlReport or AnalysedReports
        String basePath = reportType.equalsIgnoreCase(HTML_REPORTS_TYPE)
                ? String.format(HTML_REPORT_PATH, workspace, ARTIFACTS_DIR)
                : String.format(ANALYSED_RESULTS_PATH, workspace, ARTIFACTS_DIR);

        // Extracted folder name: "html_report_extracted" or "analyzed_result_extracted"
        String extractedFolderName = reportType.toLowerCase().replace(" ", "_") + "_extracted";

        return Paths.get(basePath, extractedFolderName);
    }
}
