package com.lre.services.lre.report.renderer.html;

import com.lre.model.report.LreTxnStats;

import java.util.List;

public class TransactionHtmlBuilder {

    private static final int THRESHOLD = 5;

    private TransactionHtmlBuilder() {}

    public static String generateWithThreshold(List<LreTxnStats> allStats, List<LreTxnStats> top5Stats) {

        List<LreTxnStats> full = allStats == null ? List.of() : allStats;
        List<LreTxnStats> top = top5Stats == null ? List.of() : top5Stats;

        boolean usingTop5 = full.size() > THRESHOLD;

        StringBuilder sb = new StringBuilder(1500);

        if (usingTop5) {
            sb.append(TOP5_HEADER.formatted(full.size()));
            sb.append(generateStatsHtml(top));
        } else {
            sb.append("<h3 style='color:#2c3e50; margin-bottom:12px;'>Transaction Summary</h3>");
            sb.append(generateStatsHtml(full));
        }

        return sb.toString();
    }

    private static final String TOP5_HEADER = """
            <h3 style='color:#2c3e50; margin-bottom:12px;'>TOP 5 Slowest Transactions</h3>
            <table width='100%%' cellpadding='8' cellspacing='0' border='0'
                   style='background-color:#fff3cd; border-left:4px solid #ffeeba;
                          margin-bottom:15px; font-size:12px; color:#856404;'>
                <tr>
                    <td>
                        Only the top 5 slowest transactions are displayed.
                        Total transactions: <b>%d</b>. Refer to the report for details.
                    </td>
                </tr>
            </table>
            """;

    private static final String TABLE_HEADER = """
            <table width='100%' cellpadding='8' cellspacing='0'
                   style='border-collapse: collapse; font-size:12px; background:#fff;'>
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

    private static String escapeHtml(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 10);
        for (char c : s.toCharArray()) {
            switch (c) {
                case '<'  -> sb.append("&lt;");
                case '>'  -> sb.append("&gt;");
                case '&'  -> sb.append("&amp;");
                case '"'  -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default   -> sb.append(c);
            }
        }
        return sb.toString();
    }
}
