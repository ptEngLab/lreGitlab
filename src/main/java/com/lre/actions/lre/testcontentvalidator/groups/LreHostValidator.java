package com.lre.actions.lre.testcontentvalidator.groups;

import com.lre.actions.apis.LreRestApis;
import com.lre.actions.exceptions.LreException;
import com.lre.model.enums.HostType;
import com.lre.model.enums.LGDistributionType;
import com.lre.model.test.testcontent.TestContent;
import com.lre.model.test.testcontent.groups.Group;
import com.lre.model.test.testcontent.groups.hosts.CloudTemplate;
import com.lre.model.test.testcontent.groups.hosts.Host;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class LreHostValidator {

    private final LreRestApis restApis;
    private final TestContent content;
    private List<CloudTemplate> cloudTemplatesCache;

    private static final Pattern AUTOMATCH_PATTERN = Pattern.compile("^LG\\d+$", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOUD_PATTERN = Pattern.compile("^cloud\\d+$", Pattern.CASE_INSENSITIVE);
    private static final Pattern DYNAMIC_PATTERN = Pattern.compile("^docker\\d+$", Pattern.CASE_INSENSITIVE);

    public LreHostValidator(LreRestApis restApis, TestContent content) {
        this.restApis = restApis;
        this.content = content;
        this.cloudTemplatesCache = getAllCloudTemplatesCache();
    }

    private List<CloudTemplate> getAllCloudTemplatesCache() {
        if (cloudTemplatesCache == null) cloudTemplatesCache = restApis.getAllCloudTemplates();
        return cloudTemplatesCache;
    }

    private void clearCache() {
        cloudTemplatesCache = null;
    }

    /**
     * Validates and populates the hosts for the given group.
     * This method mutates the provided group object directly.
     */
    public void validateAndPopulateHosts(Group group) {
        if (content.getLgDistribution().getType() == LGDistributionType.MANUAL) parseHosts(group);
        clearCache();
    }

    private void parseHosts(Group group) {
        if (group.getYamlHostname() == null || group.getYamlHostname().isEmpty()) {
            log.debug("No hosts to parse for group: {}", group.getName());
            return;
        }

        Map<String, String> hostTemplateMap = parseHostTemplates(group.getYamlHostTemplate());

        List<Host> hosts = Arrays.stream(group.getYamlHostname().split(","))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .map(hostname -> createHost(hostname, hostTemplateMap, group.getName()))
                .toList();

        group.setHosts(hosts);
    }

    private Map<String, String> parseHostTemplates(String hostTemplateRaw) {
        Map<String, String> map = new HashMap<>();
        if (hostTemplateRaw == null || hostTemplateRaw.isBlank()) return map;

        String[] entries = hostTemplateRaw.split(",");
        boolean containsColon = Arrays.stream(entries).anyMatch(e -> e.contains(":"));
        boolean containsNonColon = Arrays.stream(entries).anyMatch(e -> !e.contains(":"));

        if (containsColon && containsNonColon) {
            throw new LreException("Mixed hostTemplate syntax. Use either 'host:template' pairs or a single global template, not both.");
        }

        if (containsColon) {
            // Per-host mapping
            for (String entry : entries) {
                String[] parts = entry.split(":");
                if (parts.length == 2) {
                    String host = parts[0].trim().toLowerCase();
                    String templateId = parts[1].trim();
                    map.put(host, templateId);
                }
            }
        } else {
            // Single global template for all cloud hosts
            map.put("*", hostTemplateRaw.trim());
        }
        return map;
    }

    private Host createHost(String hostname, Map<String, String> hostTemplateMap, String groupName) {
        Host host = new Host();
        host.setName(hostname);

        if (AUTOMATCH_PATTERN.matcher(hostname).matches()) {
            host.setType(HostType.AUTOMATCH);
            validateNonCloudHostWithTemplate(hostname, hostTemplateMap, groupName, "AUTOMATCH");
        } else if (CLOUD_PATTERN.matcher(hostname).matches()) {
            host.setType(HostType.CLOUD);
            String templateKey = hostTemplateMap.getOrDefault(hostname.toLowerCase(), hostTemplateMap.get("*"));
            String templateId = assignTemplateToCloudHost(hostname, templateKey, groupName);
            host.setHostTemplateId(templateId);
        } else if (DYNAMIC_PATTERN.matcher(hostname).matches()) {
            host.setType(HostType.DYNAMIC);
            validateNonCloudHostWithTemplate(hostname, hostTemplateMap, groupName, "DYNAMIC");
        } else {
            host.setType(HostType.SPECIFIC);
            validateNonCloudHostWithTemplate(hostname, hostTemplateMap, groupName, "SPECIFIC");
        }

        return host;
    }

    private String assignTemplateToCloudHost(String hostname, String templateKey, String groupName) {
        if (templateKey == null) {
            String fetchedTemplateId = fetchDefaultTemplateFromLre(hostname, groupName);
            log.warn("No hostTemplate provided for cloud host '{}' in group '{}'. " +
                            "Automatically assigned template ID: {} from LRE",
                    hostname, groupName, fetchedTemplateId);
            return fetchedTemplateId;
        } else {
            String resolvedTemplateId = resolveTemplate(templateKey, hostname, groupName);
            log.debug("Assigned template ID: {} to cloud host: {} in group: {}", resolvedTemplateId, hostname, groupName);
            return resolvedTemplateId;
        }
    }

    private void validateNonCloudHostWithTemplate(String hostname, Map<String, String> hostTemplateMap,
                                                  String groupName, String hostType) {
        if (hostTemplateMap.containsKey(hostname.toLowerCase())) {
            log.warn("HostTemplate provided for {} host '{}' in group '{}'. This will be ignored.",
                    hostType, hostname, groupName);
        }
    }

    private String fetchDefaultTemplateFromLre(String hostname, String groupName) {
        List<CloudTemplate> templates = getAllCloudTemplatesCache();
        if (templates.isEmpty()) {
            throw new LreException("No cloud templates available in LRE for host: " + hostname +
                    " in group: " + groupName);
        }
        Optional<CloudTemplate> defaultTemplate = templates.stream()
                .filter(t -> t.getName().toLowerCase().contains("default"))
                .findFirst();
        CloudTemplate selectedTemplate = defaultTemplate.orElse(templates.get(0));

        log.info("Selected default template '{}' (ID: {}) for host: {} in group: {}",
                selectedTemplate.getName(), selectedTemplate.getId(), hostname, groupName);

        return String.valueOf(selectedTemplate.getId());
    }

    private String resolveTemplate(String templateKey, String hostname, String groupName) {
        // Numeric ID
        if (templateKey.matches("\\d+")) {
            int templateId = Integer.parseInt(templateKey);
            CloudTemplate template = restApis.getCloudTemplateById(templateId);
            if (template == null) {
                throw new LreException("No cloud template found with ID=" + templateId +
                        " for host: " + hostname + " in group: " + groupName);
            }
            return String.valueOf(template.getId());
        }

        // Template name (case-insensitive)
        return getAllCloudTemplatesCache().stream()
                .filter(t -> templateKey.equalsIgnoreCase(t.getName()))
                .findFirst()
                .map(t -> String.valueOf(t.getId()))
                .orElseThrow(() -> new LreException(
                        String.format("No cloud template found with name='%s' for host: %s in group: %s. Available templates: %s",
                                templateKey, hostname, groupName,
                                getAllCloudTemplatesCache().stream()
                                        .map(CloudTemplate::getName)
                                        .collect(Collectors.joining(", "))))
                );
    }
}
