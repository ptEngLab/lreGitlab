package com.lre.services;

import com.lre.actions.apis.LreRestApis;
import com.lre.actions.runmodel.LreTestRunModel;
import com.lre.actions.utils.CommonUtils;
import com.lre.model.run.LreRunResult;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static com.lre.actions.utils.ConfigConstants.*;

/**
 * Responsible for fetching and publishing an LRE HTML report locally.
 */
@Slf4j
public record LreReportPublisher(LreRestApis lreRestApis, LreTestRunModel model) {

    /**
     * Fetches and extracts the HTML report for a given LRE run.
     *
     * @return Path to extracted HTML report folder, or {@code null} if unavailable
     */

    public Path publish() {
        int runId = model.getRunId();
        Path reportDir = Paths.get(String.format(HTML_REPORT_PATH, model.getWorkspace(), ARTIFACTS_DIR));

        log.info("Publishing report for Run ID: {}", runId);
        log.info("Report directory: {}", reportDir.toAbsolutePath());

        Optional<LreRunResult> htmlReportResult = findHtmlReportResult(runId);
        if (htmlReportResult.isEmpty()) {
            log.warn("No LRE HTML report archive found for Run ID: {}", runId);
            return null;
        }

        try {
            return prepareAndExtractReport(runId, htmlReportResult.get(), reportDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to publish LRE report for Run ID: " + runId, e);
        }
    }

    /**
     * Fetches the list of run results and returns the one corresponding
     * to the HTML report archive, if present.
     */
    private Optional<LreRunResult> findHtmlReportResult(int runId) {
        List<LreRunResult> runResults = lreRestApis.fetchRunResults(runId);

        if (runResults == null || runResults.isEmpty()) {
            log.warn("No run results found for Run ID: {}", runId);
            return Optional.empty();
        }

        return runResults.stream()
                .filter(result -> LRE_REPORT_ARCHIVE_NAME.equalsIgnoreCase(result.getName()))
                .findFirst();
    }

    /**
     * Handles directory creation, report download, cleanup, and extraction.
     */
    private Path prepareAndExtractReport(int runId, LreRunResult result, Path reportDir) throws IOException {
        Files.createDirectories(reportDir);

        Path archivePath = reportDir.resolve(LRE_REPORT_ARCHIVE_NAME);
        Path extractedDir = reportDir.resolve("html_report");

        if (!downloadReportArchive(runId, result.getId(), archivePath)) return null;

        cleanOldReportDir(extractedDir);
        extractReport(archivePath, extractedDir);

        log.debug("HTML report successfully extracted to: {}", extractedDir.toAbsolutePath());
        return extractedDir;
    }

    /**
     * Downloads the LRE HTML report archive to the specified path.
     */
    private boolean downloadReportArchive(int runId, int resultId, Path destination) {
        boolean success = lreRestApis.getRunResultData(runId, resultId, destination.toString());
        if (!success) {
            log.error("Failed to download report archive for Run ID: {}", runId);
        }
        return success;
    }

    /**
     * Deletes any previous extracted report folder if it exists.
     */
    private void cleanOldReportDir(Path extractedDir) throws IOException {
        if (Files.exists(extractedDir)) {
            log.debug("Cleaning up previous report directory: {}", extractedDir);
            CommonUtils.deleteDirectoryRecursively(extractedDir);
        }
    }

    /**
     * Extracts the ZIP archive into the target directory.
     */
    private void extractReport(Path archivePath, Path extractedDir) throws IOException {
        CommonUtils.unzip(archivePath.toFile(), extractedDir.toFile());
    }
}
