package com.lre.client.runclient;

import com.lre.client.runmodel.EmailConfigModel;
import com.lre.common.constants.ConfigConstants;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static com.lre.common.constants.ConfigConstants.ARTIFACTS_DIR;

@Slf4j
public record EmailClient(EmailConfigModel emailConfig) implements AutoCloseable {

    public boolean send() {
        if (!validateConfig()) {
            return false;
        }

        try {
            Session session = createSession();
            Message message = buildMessage(session);

            Transport.send(message);
            log.info("Email sent successfully to {}", emailConfig.getTo());
            return true;

        } catch (AuthenticationFailedException e) {
            log.error("Email authentication failed: {}", e.getMessage());
            return false;
        } catch (MessagingException e) {
            log.error("Email messaging error: {}", e.getMessage());
            return false;
        } catch (IOException e) {
            log.error("File attachment error: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error sending email: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean validateConfig() {
        if (emailConfig == null) {
            log.error("Email configuration is null");
            return false;
        }

        if (emailConfig.getTo() == null || emailConfig.getTo().trim().isEmpty()) {
            log.error("Recipient email address is required");
            return false;
        }

        if (emailConfig.getSmtpHost() == null || emailConfig.getSmtpHost().trim().isEmpty()) {
            log.error("SMTP host is required");
            return false;
        }

        if (emailConfig.getFrom() == null || emailConfig.getFrom().trim().isEmpty()) {
            log.error("From address is required");
            return false;
        }

        log.debug("Email configuration validated successfully");
        return true;
    }

    private Session createSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", emailConfig.getSmtpHost());
        props.put("mail.smtp.port", emailConfig.getSmtpPort());

        // Add timeout configurations
        props.put("mail.smtp.connectiontimeout", "10000"); // 10 seconds
        props.put("mail.smtp.timeout", "30000"); // 30 seconds
        props.put("mail.smtp.writetimeout", "30000"); // 30 seconds

        // Additional properties for better handling
        props.put("mail.smtp.ssl.trust", emailConfig.getSmtpHost());
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        emailConfig.getUsername(),
                        emailConfig.getPassword()
                );
            }
        });
    }

    private Message buildMessage(Session session) throws MessagingException, IOException {
        Message message = new MimeMessage(session);

        // Set basic headers
        message.setFrom(new InternetAddress(emailConfig.getFrom()));
        message.setRecipients(Message.RecipientType.TO, parseAddresses(emailConfig.getTo()));

        // Set CC if provided
        if (emailConfig.getCc() != null && !emailConfig.getCc().trim().isEmpty()) {
            message.setRecipients(Message.RecipientType.CC, parseAddresses(emailConfig.getCc()));
        }

        // Set BCC if provided
        if (emailConfig.getBcc() != null && !emailConfig.getBcc().trim().isEmpty()) {
            message.setRecipients(Message.RecipientType.BCC, parseAddresses(emailConfig.getBcc()));
        }

        message.setSubject(emailConfig.getSubject() != null ? emailConfig.getSubject() : "(No Subject)");

        // Set content
        message.setContent(buildMultipart());

        // Set additional headers
        message.setHeader("X-Mailer", "LRE-Client");
        message.setSentDate(new java.util.Date());

        return message;
    }

    private Multipart buildMultipart() throws MessagingException, IOException {
        Multipart multipart = new MimeMultipart();

        // Add text body part (HTML or plain text)
        multipart.addBodyPart(createTextBodyPart());

        // Add attachments
        addAttachments(multipart);

        return multipart;
    }

    private MimeBodyPart createTextBodyPart() throws MessagingException {
        MimeBodyPart textBodyPart = new MimeBodyPart();

        String body = "";
        try {
            Path reportDir = Paths.get(ConfigConstants.DEFAULT_OUTPUT_DIR, ARTIFACTS_DIR, "LreReports/email.html");
            body = Files.readString(reportDir, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to read email body from file", e);
        }

        textBodyPart.setContent(body, "text/html; charset=utf-8");

        return textBodyPart;
    }

    private void addAttachments(Multipart multipart) throws MessagingException, IOException {
        if (emailConfig.getAttachmentPaths() != null && !emailConfig.getAttachmentPaths().isEmpty()) {
            for (String attachmentPath : emailConfig.getAttachmentPaths()) {
                addAttachment(multipart, attachmentPath);
            }
        }
        // Support for single attachment path for backward compatibility
        else if (emailConfig.getAttachmentPath() != null && !emailConfig.getAttachmentPath().isEmpty()) {
            addAttachment(multipart, emailConfig.getAttachmentPath());
        }
    }

    private void addAttachment(Multipart multipart, String attachmentPath) throws MessagingException, IOException {
        File attachment = new File(attachmentPath.trim());
        if (attachment.exists() && attachment.isFile()) {
            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(attachment);

            // Set filename header
            attachmentPart.setFileName(attachment.getName());

            multipart.addBodyPart(attachmentPart);
            log.info("Attached file: {}", attachment.getAbsolutePath());
        } else {
            log.warn("Attachment file not found or is not a file: {}", attachment.getAbsolutePath());
        }
    }

    private Address[] parseAddresses(String addresses) throws AddressException {
        if (addresses == null || addresses.trim().isEmpty()) {
            return new Address[0];
        }
        return InternetAddress.parse(addresses.trim());
    }

    @Override
    public void close() {
        // Can be used for cleanup in future versions
        log.debug("Email client closed");
    }
}