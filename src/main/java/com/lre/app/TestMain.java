package com.lre.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class TestMain {

    public static void sendTestReportEmail(Map<String, String> testData, String htmlTemplatePath) throws Exception {
        // SMTP configuration
        String host = "smtp.yourcompany.com";
        String port = "587";
        final String username = "your_email@company.com";
        final String password = "your_password";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);



        // Read HTML template from file
        String htmlTemplate = new String(Files.readAllBytes(Paths.get(htmlTemplatePath)), "UTF-8");

        // Replace placeholders
        for (Map.Entry<String, String> entry : testData.entrySet()) {
            htmlTemplate = htmlTemplate.replace("${" + entry.getKey() + "}", entry.getValue());
        }

        // Build Load Generators HTML
        StringBuilder lgHtml = new StringBuilder();

        String[] lgList = testData.get("LGs").split(";");

        System.out.println(Arrays.stream(lgList).toList());
        for (String lg : lgList) {
            String[] parts = lg.split("\\(");
            String name = parts[0].trim();
            String vusers = parts[1].replace(")", "").trim() + " Vusers";

            lgHtml.append("<div class=\"lg-chip\">")
                    .append("<span class=\"lg-name\">").append(name).append("</span>")
                    .append("<span class=\"vuser-badge\">").append(vusers).append("</span>")
                    .append("</div>")
                    .append("\n");

        }

        htmlTemplate = htmlTemplate.replace("${LGsUsed}", lgHtml.toString());

        try {
            Files.write(
                    Paths.get("report_v2.html"),
                    htmlTemplate.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
            System.out.println("File written successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws Exception {
        Map<String, String> testData = new HashMap<>();
        testData.put("TestName", "CreateTestFromYaml");
        testData.put("RunName", "AdhocRun_2025-10-14 11:14:56");
        testData.put("TransactionPassed", "379");
        testData.put("TransactionFailed", "0");
        testData.put("Vusers", "9");
        testData.put("TransactionPerSec", "1");
        testData.put("HitsPerSec", "0");
        testData.put("AvgThroughput", "0");
        testData.put("Domain", "example");
        testData.put("Project", "test-project");
        testData.put("TestID", "176");
        testData.put("TestFolder", "Subject\\yamlTest");
        testData.put("TestInstanceID", "14");
        testData.put("Controller", "vm011.net");
        testData.put("StartTime", "2025-10-14T11:15:30");
        testData.put("EndTime", "2025-10-14T11:21:46");
        testData.put("TestDuration", "00:06:16");
        testData.put("Errors", "0");
        testData.put("ReportLink", "https://lre.company.com/report/176");
        testData.put("RunID", "14");
        testData.put("RecipientEmails", "team@company.com");
        testData.put("LGs", "vmxyz012345.abcres01.net(9);vmxyz012345.abcres01.net(9);vmxyz012345.abcres01.net(9);vmxyz012342.abcres01.net(10);");


        // Path to your HTML template file
        String htmlTemplatePath = "src/main/resources/template.html";
         sendTestReportEmail(testData, htmlTemplatePath);
    }
}
