package com.lre.services.lre.report.publisher;

import com.lre.client.api.lre.LreRestApis;
import com.lre.client.runmodel.LreTestRunModel;
import com.lre.common.utils.CommonUtils;
import com.lre.model.run.LreRunResult;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static com.lre.common.constants.ConfigConstants.*;

/**
 * Responsible for fetching and publishing an LRE report (HTML or Analysed) locally.
 */
@Slf4j
public record LreReportPublisher(LreRestApis lreRestApis, LreTestRunModel model) {

    /**
     * Fetches and extracts the report (HTML or Analysed) for a given LRE run.
     *
     * @param reportType The type of report to fetch: either "HTML REPORT" or "ANALYZED RESULT"
     * @return Path to the extracted report folder, or {@code Optional.empty()} if unavailable
     */
    public Optional<Path> publish(String reportType) {
        int runId = model.getRunId();

        log.info("Publishing {} report for Run ID: {}", reportType, runId);

        // Fetch the relevant run result based on the report type
        Optional<LreRunResult> reportResult = findRunResults(runId, reportType);

        if (reportResult.isEmpty()) {
            log.warn("No {} report archive found for Run ID: {}", reportType, runId);
            return Optional.empty();
        }

        try {
            Optional<Path> extractedPathOpt = prepareAndExtractReport(runId, reportResult.get(), reportType);
            if (extractedPathOpt.isEmpty()) {
                log.warn("Failed to extract {} report for Run ID: {}", reportType, runId);
                return Optional.empty();
            }
            Path extractedPath = extractedPathOpt.get();
            if (HTML_REPORTS_TYPE.equalsIgnoreCase(reportType)) model.setHtmlReportPath(extractedPath);
            else if (ANALYSED_RESULTS_TYPE.equalsIgnoreCase(reportType)) model.setAnalysedReportPath(extractedPath);

            return Optional.of(extractedPath);
        } catch (IOException e) {
            log.error("Failed to publish LRE {} report for Run ID: {}", reportType, runId, e);
            throw new RuntimeException("Failed to publish LRE " + reportType + " report for Run ID: " + runId, e);
        }
    }

    /**
     * Fetches the list of run results and returns the one corresponding to the requested report type (HTML or Analysed).
     */
    private Optional<LreRunResult> findRunResults(int runId, String reportType) {
        List<LreRunResult> runResults = lreRestApis.fetchRunResults(runId);

        if (runResults == null || runResults.isEmpty()) {
            log.warn("No run results found for Run ID: {}", runId);
            return Optional.empty();
        }

        // Fetch the first matching result based on the report type
        return runResults.stream()
                .filter(result -> reportType.equalsIgnoreCase(result.getType()))
                .findFirst();
    }

    /**
     * Handles directory creation, report download, cleanup, and extraction for both HTML and Analysed reports.
     */
    private Optional<Path> prepareAndExtractReport(int runId, LreRunResult result, String reportType) throws IOException {
        Path reportDir;

        // Determine whether to use the HTML or Analysed report path
        if (HTML_REPORTS_TYPE.equalsIgnoreCase(reportType)) {
            reportDir = Paths.get(String.format(HTML_REPORT_PATH, model.getWorkspace(), ARTIFACTS_DIR));
        } else {
            // For analyzed report, use ANALYSED_RESULTS_PATH
            reportDir = Paths.get(String.format(ANALYSED_RESULTS_PATH, model.getWorkspace(), ARTIFACTS_DIR));
        }

        Files.createDirectories(reportDir);

        // Set report archive name based on type
        String reportArchiveName = HTML_REPORTS_TYPE.equalsIgnoreCase(reportType)
                ? String.format(HTML_REPORT_ARCHIVE_NAME, runId)
                : String.format(ANALYSED_REPORT_ARCHIVE_NAME, runId);

        Path archivePath = reportDir.resolve(reportArchiveName);
        Path extractedDir = reportDir.resolve(reportType.toLowerCase().replace(" ", "_") + "_extracted");

        log.info("Downloading result file for type: {} ", reportType);

        if (!downloadReportArchive(runId, result.getId(), archivePath)) {
            log.error("Error in downloading result file");
            return Optional.empty();
        }

        log.info("Result file has been downloaded successfully to: {}", extractedDir.toAbsolutePath());


        cleanOldReportDir(extractedDir);
        extractReport(archivePath, extractedDir);

        log.debug("{} report successfully extracted to: {}", reportType, extractedDir.toAbsolutePath());
        return Optional.of(extractedDir);
    }

    /**
     * Downloads the LRE report archive to the specified path.
     */
    private boolean downloadReportArchive(int runId, int resultId, Path destination) {
        boolean success = lreRestApis.getRunResultData(runId, resultId, destination.toString());
        if (!success) log.error("Failed to download report archive for Run ID: {}", runId);
        return success;
    }

    /**
     * Deletes any previous extracted report folder if it exists.
     */
    private void cleanOldReportDir(Path extractedDir) {
        if (Files.exists(extractedDir)) {
            log.debug("Cleaning up previous report directory: {}", extractedDir);
            CommonUtils.deleteFolder(extractedDir);
        }
    }

    /**
     * Extracts the ZIP archive into the target directory.
     */
    private void extractReport(Path archivePath, Path extractedDir) throws IOException {
        CommonUtils.unzip(archivePath.toFile(), extractedDir.toFile());
    }
}
