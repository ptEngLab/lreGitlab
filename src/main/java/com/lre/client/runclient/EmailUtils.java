package com.lre.client.runclient;

import com.lre.client.runmodel.EmailConfigModel;
import com.lre.client.runmodel.LreTestRunModel;
import com.lre.common.constants.ConfigConstants;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.lre.common.constants.ConfigConstants.*;

@Slf4j
@UtilityClass
public class EmailUtils {

    public static boolean sendEmailWithPipelineArtifacts(EmailConfigModel emailConfig, LreTestRunModel lreRunModel) {

        List<String> attachments = new ArrayList<>();

        Path htmlReport = Paths.get(
                String.format(HTML_REPORT_PATH, lreRunModel.getWorkspace(), ARTIFACTS_DIR),
                String.format(HTML_REPORT_ARCHIVE_NAME, lreRunModel.getRunId())
        ).toAbsolutePath().normalize();

        Path excelFile = Paths.get(
                ConfigConstants.DEFAULT_OUTPUT_DIR, ARTIFACTS_DIR,
                String.format(EXCEL_FILE, lreRunModel.getRunId())
        ).toAbsolutePath().normalize();


        if (Files.exists(htmlReport) && Files.isRegularFile(htmlReport)) attachments.add(htmlReport.toString());
        else log.warn("HTML report not found: {}", htmlReport);

        if (Files.exists(excelFile) && Files.isRegularFile(excelFile)) attachments.add(excelFile.toString());
        else log.warn("Excel file not found: {}", excelFile);

        String dynamicSubject = buildDynamicSubject(emailConfig.getSubject(), lreRunModel);

        emailConfig = emailConfig.toBuilder()
                .attachmentPaths(attachments)
                .subject(dynamicSubject)
                .build();

        // Send email
        try (EmailClient emailClient = new EmailClient(emailConfig, lreRunModel)) {
            boolean success = emailClient.send();
            if (success) {
                log.info("Email sent successfully with {} attachments.", attachments.size());
                return true;
            } else {
                log.error("Email sending failed. Check logs for details.");
                return false;
            }
        }

    }

    private static String buildDynamicSubject(String subject, LreTestRunModel lreRunModel) {
        String base = (subject != null && !subject.isBlank()) ? subject : "(No Subject)";
        return String.format("%s - Run ID %d", base, lreRunModel.getRunId());
    }
}
