package com.lre.services.lre.summary.run;

import com.lre.model.transactions.LreTxnStats;

import java.util.Collections;
import java.util.List;

public class TransactionHtmlBuilder {

    public static String generateWithThreshold(List<LreTxnStats> allStats, List<LreTxnStats> top5Stats) {
        if (allStats == null) allStats = Collections.emptyList();
        if (top5Stats == null) top5Stats = Collections.emptyList();

        boolean usingTop5 = allStats.size() > 5;
        String htmlContent = usingTop5 ? generateStatsHtml(top5Stats) : generateStatsHtml(allStats);

        if (usingTop5) {
            String header = String.format("""
                    <h3 style='color:#2c3e50; margin-bottom:12px;'>TOP 5 Slowest Transactions</h3>
                    <table width='100%%' cellpadding='8' cellspacing='0' border='0'
                           style='background-color:#fff3cd; border-left:4px solid #ffeeba;
                                  margin-bottom:15px; font-size:12px; color:#856404;'>
                        <tr>
                            <td>Only the top 5 slowest transactions are displayed. Total transactions: <b>%d</b>. Refer to the report for details.</td>
                        </tr>
                    </table>
                    """, allStats.size());
            return header + htmlContent;
        }

        return "<h3 style='color:#2c3e50; margin-bottom:12px;'>Transaction Summary</h3>" + htmlContent;
    }

    private static String generateStatsHtml(List<LreTxnStats> stats) {
        StringBuilder html = new StringBuilder();
        html.append("""
                <table width='100%' cellpadding='8' cellspacing='0' style='border-collapse: collapse; font-size:12px; background:#fff;'>
                <thead>
                    <tr style='background:#6f2b8f; color:#fff; text-align:left;'>
                        <th>Transaction</th><th>Min (s)</th><th>Avg (s)</th><th>Max (s)</th>
                        <th>Pass</th><th>Fail</th><th>P90 (s)</th><th>P95 (s)</th>
                    </tr>
                </thead>
                <tbody>
                """);

        boolean alt = false;
        for (var t : stats) {
            String bg = alt ? "#f7f7f7" : "#ffffff";
            alt = !alt;
            html.append("<tr style='background:").append(bg).append("'>")
                    .append("<td>").append(t.getTransactionName()).append("</td>")
                    .append("<td>").append(t.getMinimum()).append("</td>")
                    .append("<td>").append(t.getAverage()).append("</td>")
                    .append("<td>").append(t.getMaximum()).append("</td>")
                    .append("<td style='color:#28a745;'>").append(t.getPass()).append("</td>")
                    .append("<td style='color:#dc3545;'>").append(t.getFail()).append("</td>")
                    .append("<td>").append(t.getP90()).append("</td>")
                    .append("<td>").append(t.getP95()).append("</td>")
                    .append("</tr>");
        }
        html.append("</tbody></table>");
        return html.toString();
    }
}
