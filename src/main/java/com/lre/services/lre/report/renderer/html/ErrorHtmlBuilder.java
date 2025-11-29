package com.lre.services.lre.report.renderer.html;

import com.lre.model.report.LreErrorStats;
import com.lre.services.lre.report.fetcher.ReportDataService;

import java.util.List;

import static com.lre.common.utils.CommonUtils.escapeHtml;

public class ErrorHtmlBuilder {

    private ErrorHtmlBuilder() {}

    /**
     * Generates HTML for error stats. If the number of errors exceeds TOP_N,
     * only the top N errors are displayed with a warning header.
     */
    public static String generateWithThreshold(List<LreErrorStats> allStats, List<LreErrorStats> topStats) {
        List<LreErrorStats> full = allStats == null ? List.of() : allStats;
        List<LreErrorStats> top = topStats == null ? List.of() : topStats;

        boolean usingTopN = full.size() > ReportDataService.TOP_N;

        StringBuilder sb = new StringBuilder(1500);

        if (usingTopN) {
            sb.append(TOPN_HEADER.formatted(
                    ReportDataService.TOP_N,
                    ReportDataService.TOP_N,
                    full.size()
            ));
            sb.append(generateStatsHtml(top));
        } else {
            sb.append("<h3 style='color:#2c3e50; margin-bottom:12px;'>Error Summary</h3>");
            sb.append(generateStatsHtml(full));
        }

        return sb.toString();
    }

    private static final String TOPN_HEADER = """
            <h3 style='color:#2c3e50; margin-bottom:12px;'>TOP %d Errors</h3>
            <table width='100%%' cellpadding='8' cellspacing='0' border='0'
                   style='background-color:#fff3cd; border-left:4px solid #ffeeba;
                          margin-bottom:15px; font-size:12px; color:#856404;'>
                <tr>
                    <td>
                        Only the top %d errors are displayed.
                        Total errors: <b>%d</b>. Refer to the report for more details.
                    </td>
                </tr>
            </table>
            """;

    private static final String TABLE_HEADER = """
            <table width='100%' cellpadding='8' cellspacing='0'
                   style='border-collapse: collapse; font-size:13px; background:#fff;'>
                <thead>
                    <tr style='background:#6f2b8f; color:#fff; text-align:left;'>
                        <th>Script</th>
                        <th>Injector</th>
                        <th>Error Code</th>
                        <th>Error Message</th>
                        <th>Total Count</th>
                        <th>Affected Vusers</th>
                    </tr>
                </thead>
                <tbody>
            """;

    private static final String TABLE_CLOSE = "</tbody></table>";

    private static String generateStatsHtml(List<LreErrorStats> stats) {
        StringBuilder sb = new StringBuilder(TABLE_HEADER.length() + stats.size() * 160);

        sb.append(TABLE_HEADER);

        for (int i = 0; i < stats.size(); i++) {
            sb.append(buildRow(stats.get(i), i % 2 != 0));
        }

        sb.append(TABLE_CLOSE);
        return sb.toString();
    }

    private static String buildRow(LreErrorStats e, boolean alt) {
        String bg = alt ? "#f7f7f7" : "#ffffff";

        return """
            <tr style='background:%s'>
                <td>%s</td>
                <td>%s</td>
                <td>%s</td>
                <td>%s</td>
                <td style='color:#dc3545;'>%s</td>
                <td>%s</td>
            </tr>
            """.formatted(
                bg,
                safe(e.getScriptName()),
                safe(e.getInjectorName()),
                safe(e.getErrorCode()),
                safe(e.getErrorMessage()),
                safe(e.getTotalErrorCount()),
                safe(e.getAffectedVusers())
        );
    }

    private static String safe(Object o) {
        return escapeHtml(o == null ? "" : o.toString());
    }

}
