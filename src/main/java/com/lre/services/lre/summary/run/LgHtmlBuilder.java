package com.lre.services.lre.summary.run;

public class LgHtmlBuilder {
    public static String generate(String lgsData) {
        if (lgsData == null || lgsData.isEmpty()) return "";

        StringBuilder html = new StringBuilder();
        String[] lgList = lgsData.split(";");

        for (String lg : lgList) {
            String[] parts = lg.split("\\(");
            String name = parts[0].trim();
            String vusers = parts.length > 1 ? parts[1].replace(")", "").trim() : "0";

            html.append("<tr>")
                    .append("<td bgcolor=\"#f8f9fa\" style=\"border-left:4px solid #9c27b0; padding:14px;\">")
                    .append("<div style=\"font-size:10px; color:#6c757d; font-weight:600; text-transform:uppercase;\">Load Generator</div>")
                    .append("<div style=\"font-size:13px; color:#9c27b0; font-weight:600; margin-top:6px;\">")
                    .append(name)
                    .append("</div></td>")
                    .append("<td width=\"2%\"></td>")
                    .append("<td bgcolor=\"#f8f9fa\" style=\"border-left:4px solid #9c27b0; padding:14px;\">")
                    .append("<div style=\"font-size:10px; color:#6c757d; font-weight:600; text-transform:uppercase;\">Vusers Allocated</div>")
                    .append("<div style=\"font-size:13px; color:#9c27b0; font-weight:600; margin-top:6px;\">")
                    .append(vusers)
                    .append("</div></td></tr>")
                    .append("<tr><td colspan=\"3\" height=\"8\"></td></tr>");
        }

        return html.toString();
    }
}
