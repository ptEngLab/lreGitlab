package com.lre.services.lre.report.renderer.html;

import com.lre.model.report.LreTxnStats;
import com.lre.services.lre.report.fetcher.ReportDataService;

import java.util.List;

import static com.lre.common.utils.CommonUtils.escapeHtml;

public class TransactionHtmlBuilder {

    private TransactionHtmlBuilder() {}

    /**
     * Generates HTML for transaction stats. If the number of transactions exceeds TOP_N,
     * only the top N the slowest transactions are displayed with a warning header.
     */
    public static String generateWithThreshold(List<LreTxnStats> allStats, List<LreTxnStats> topStats) {
        List<LreTxnStats> full = allStats == null ? List.of() : allStats;
        List<LreTxnStats> top = topStats == null ? List.of() : topStats;

        boolean usingTopN = full.size() > ReportDataService.TOP_N;

        StringBuilder sb = new StringBuilder(1500);

        if (usingTopN) {
            sb.append(TOPN_HEADER.formatted(ReportDataService.TOP_N, ReportDataService.TOP_N, full.size()));
            sb.append(generateStatsHtml(top));
        } else {
            sb.append("<h3 style='color:#2c3e50; margin-bottom:12px;'>Transaction Summary</h3>");
            sb.append(generateStatsHtml(full));
        }

        return sb.toString();
    }

    private static final String TOPN_HEADER = """
            <h3 style='color:#2c3e50; margin-bottom:12px;'>TOP %d Slowest Transactions</h3>
            <table width='100%%' cellpadding='8' cellspacing='0' border='0'
                   style='background-color:#fff3cd; border-left:4px solid #ffeeba;
                          margin-bottom:15px; font-size:12px; color:#856404;'>
                <tr>
                    <td>
                        Only the top %d slowest transactions are displayed.
                        Total transactions: <b>%d</b>. Refer to the report for details.
                    </td>
                </tr>
            </table>
            """;

    private static final String TABLE_HEADER = """
            <table width='100%' cellpadding='8' cellspacing='0'
                   style='border-collapse: collapse; font-size:13px; background:#fff;'>
                <thead>
                    <tr style='background:#6f2b8f; color:#fff; text-align:left;'>
                        <th>Transaction</th>
                        <th>Min (s)</th>
                        <th>Avg (s)</th>
                        <th>Max (s)</th>
                        <th>Pass</th>
                        <th>Fail</th>
                        <th>P90 (s)</th>
                        <th>P95 (s)</th>
                    </tr>
                </thead>
                <tbody>
            """;

    private static final String TABLE_CLOSE = "</tbody></table>";

    private static String generateStatsHtml(List<LreTxnStats> stats) {
        StringBuilder sb = new StringBuilder(TABLE_HEADER.length() + stats.size() * 160);

        sb.append(TABLE_HEADER);

        for (int i = 0; i < stats.size(); i++) {
            sb.append(buildRow(stats.get(i), i % 2 != 0));
        }

        sb.append(TABLE_CLOSE);
        return sb.toString();
    }

    private static String buildRow(LreTxnStats t, boolean alt) {
        String bg = alt ? "#f7f7f7" : "#ffffff";

        return """
            <tr style='background:%s'>
                <td>%s</td>
                <td>%s</td>
                <td>%s</td>
                <td>%s</td>
                <td style='color:#28a745;'>%s</td>
                <td style='color:#dc3545;'>%s</td>
                <td>%s</td>
                <td>%s</td>
            </tr>
            """.formatted(
                bg,
                safe(t.getTransactionName()),
                safe(t.getMinimum()),
                safe(t.getAverage()),
                safe(t.getMaximum()),
                safe(t.getPass()),
                safe(t.getFail()),
                safe(t.getP90()),
                safe(t.getP95())
        );
    }

    private static String safe(Object o) {
        return escapeHtml(o == null ? "" : o.toString());
    }


}
