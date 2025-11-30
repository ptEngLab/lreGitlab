package com.lre.client.runmodel;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@Builder(toBuilder = true)
@ToString(exclude = {"password"})
public class EmailConfigModel {
    private String smtpHost;
    private int smtpPort;
    private String username;
    private String password;
    private String from;
    private String to;
    private String cc;
    private String bcc;
    private String subject;
    private String body;
    private String attachmentPath; // single attachment (backward compatibility)
    private List<String> attachmentPaths; // multiple attachments
    private String replyTo;
}
