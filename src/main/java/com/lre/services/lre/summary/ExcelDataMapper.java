package com.lre.services.lre.summary;

import com.lre.client.runmodel.LreTestRunModel;
import com.lre.excel.ExcelDashboardWriter;
import com.lre.model.run.LreRunStatusExtended;
import org.apache.commons.lang3.tuple.Pair;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExcelDataMapper {
    private static final String SECTION_TEST_METADATA = "Test Metadata";
    private static final String SECTION_RUN_DETAILS = "Run Details";
    private static final String SECTION_PERFORMANCE = "Performance";

    public static List<ExcelDashboardWriter.Section> createSections(
            LreTestRunModel model, LreRunStatusExtended runStatus, ThresholdResult thresholds) {

        ExcelDashboardWriter.Section testMetadataSection = createTestMetadataSection(model, runStatus, thresholds);
        ExcelDashboardWriter.Section runDetailsSection = createRunDetailsSection(model, runStatus, thresholds);
        ExcelDashboardWriter.Section performanceSection = createPerformanceSection(runStatus);

        return Arrays.asList(testMetadataSection, runDetailsSection, performanceSection);
    }

    private static ExcelDashboardWriter.Section createTestMetadataSection(
            LreTestRunModel model, LreRunStatusExtended runStatus, ThresholdResult thresholds) {

        List<String> metaKeys1 = Arrays.asList("Domain", "Project", "Test Name", "Test Id");
        List<Object> metaValues1 = Arrays.asList(
                safeString(model.getDomain()),
                safeString(model.getProject()),
                safeString(model.getTestName()),
                runStatus.getTestId()
        );

        List<String> metaKeys2 = Arrays.asList("Start Time", "End Time", "Test Duration", "Run Status");
        List<Object> metaValues2 = Arrays.asList(
                formatDateTime(runStatus.getStart()),
                formatDateTime(runStatus.getEnd()),
                calculateTestDuration(runStatus),
                safeString(runStatus.getState()) + ", Result: " + thresholds.runResult()
        );

        return new ExcelDashboardWriter.Section(
                SECTION_TEST_METADATA, "Execution Time", metaKeys1, metaValues1, metaKeys2, metaValues2
        );
    }

    private static ExcelDashboardWriter.Section createRunDetailsSection(
            LreTestRunModel model, LreRunStatusExtended runStatus, ThresholdResult thresholds) {

        List<String> runKeys1 = Arrays.asList("Test Folder", "Test Instance Id", "Run Name", "Controller used");
        List<Object> runValues1 = Arrays.asList(
                safeString(model.getTestFolderPath()),
                runStatus.getTestInstanceId(),
                safeString(runStatus.getName()),
                safeString(runStatus.getController())
        );

        List<String> runKeys2 = Arrays.asList("Transaction Passed", "Transaction Failed", "Errors", "Timeslot ID");
        List<Object> runValues2 = Arrays.asList(
                runStatus.getTransPassed(),
                thresholds.failedTxnStr(),
                thresholds.errorStr(),
                runStatus.getReservationId()
        );

        return new ExcelDashboardWriter.Section(
                SECTION_RUN_DETAILS, "High-Level Metrics", runKeys1, runValues1, runKeys2, runValues2
        );
    }

    private static ExcelDashboardWriter.Section createPerformanceSection(LreRunStatusExtended runStatus) {
        // Performance metrics
        List<String> performanceKeys = List.of(
                "Transaction per Sec",
                "Hits per Sec",
                "Throughput (avg)",
                "Vusers involved"
        );

        List<Object> performanceValues = List.of(
                runStatus.getTransPerSec(),
                runStatus.getHitsPerSec(),
                runStatus.getThroughputAvg(),
                runStatus.getVusersInvolved()
        );

        // Parse LG entries into headers and values
        Pair<List<String>, List<Object>> lgData = parseLgEntries(runStatus.getLgs());

        return new ExcelDashboardWriter.Section(
                SECTION_PERFORMANCE,
                "VUsers per LG Count",
                performanceKeys,
                performanceValues,
                lgData.getLeft(),
                lgData.getRight()
        );
    }

    private static Pair<List<String>, List<Object>> parseLgEntries(String lgsString) {
        if (lgsString == null || lgsString.isBlank()) {
            return Pair.of(List.of(), List.of());
        }

        List<String> headers = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        Arrays.stream(lgsString.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(entry -> parseLgEntry(entry, headers, values));

        return Pair.of(headers, values);
    }


    private static void parseLgEntry(String entry, List<String> lgHeaders, List<Object> lgValues) {
        int start = entry.indexOf('(');
        int end = entry.indexOf(')');
        if (start > 0 && end > start) {
            String host = entry.substring(0, start);
            String valueStr = entry.substring(start + 1, end);
            try {
                int value = Integer.parseInt(valueStr);
                lgHeaders.add(host);
                lgValues.add(value);
            } catch (NumberFormatException e) {
                lgHeaders.add(entry);
                lgValues.add(0);
            }
        } else {
            lgHeaders.add(entry);
            lgValues.add(0);
        }
    }

    private static String calculateTestDuration(LreRunStatusExtended runStatus) {
        LocalDateTime start = runStatus.getStart();
        LocalDateTime end = runStatus.getEnd();

        if (start == null || end == null) return "N/A";

        Duration duration = Duration.between(start, end);
        return String.format("%02d:%02d:%02d",
                duration.toHours(),
                duration.toMinutesPart(),
                duration.toSecondsPart());
    }

    private static String formatDateTime(LocalDateTime dt) {
        if (dt == null) return "N/A";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");
        return dt.format(formatter);
    }

    private static String safeString(Object obj) {
        return obj != null ? obj.toString() : "N/A";
    }
}
