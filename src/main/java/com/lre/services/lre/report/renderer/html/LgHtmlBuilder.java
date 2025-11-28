package com.lre.services.lre.report.renderer.html;

public class LgHtmlBuilder {

    public static String generate(String lgsData) {
        if (lgsData == null || lgsData.isBlank()) return "";

        StringBuilder html = new StringBuilder(lgsData.length() * 8);

        for (String lg : lgsData.split(";")) {
            String[] parts = lg.split("\\(");
            String name = parts[0].trim();
            String vusers = (parts.length > 1 ? parts[1].replace(")", "") : "0").trim();

            html.append(LG_ROW_TEMPLATE.formatted(name, vusers));
        }

        return html.toString();
    }

    private static final String LG_ROW_TEMPLATE = """
        <tr>
            <td bgcolor="#f8f9fa" style="border-left:4px solid #9c27b0; padding:14px;">
                <div style="font-size:10px; color:#6c757d; font-weight:600; text-transform:uppercase;">Load Generator</div>
                <div style="font-size:13px; color:#9c27b0; font-weight:600; margin-top:6px;">%s</div>
            </td>
            <td width="2%%"></td>
            <td bgcolor="#f8f9fa" style="border-left:4px solid #9c27b0; padding:14px;">
                <div style="font-size:10px; color:#6c757d; font-weight:600; text-transform:uppercase;">Vusers Allocated</div>
                <div style="font-size:13px; color:#9c27b0; font-weight:600; margin-top:6px;">%s</div>
            </td>
        </tr>
        <tr><td colspan="3" height="8"></td></tr>
        """;
}
