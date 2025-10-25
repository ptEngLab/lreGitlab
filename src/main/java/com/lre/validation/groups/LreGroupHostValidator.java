package com.lre.validation.groups;

import com.lre.client.api.lre.LreRestApis;
import com.lre.common.exceptions.LreException;
import com.lre.model.enums.HostType;
import com.lre.model.enums.LGDistributionType;
import com.lre.model.test.testcontent.TestContent;
import com.lre.model.test.testcontent.groups.hosts.CloudTemplate;
import com.lre.model.test.testcontent.groups.hosts.Host;
import com.lre.model.test.testcontent.groups.hosts.HostResponse;
import com.lre.model.yaml.YamlGroup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class LreGroupHostValidator {

    private final LreRestApis restApis;
    private final TestContent content;
    private List<CloudTemplate> cloudTemplatesCache;
    private List<HostResponse> specificLGsCache;

    private static final Pattern AUTOMATCH_PATTERN = Pattern.compile("^LG\\d+$", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOUD_PATTERN = Pattern.compile("^cloud\\d+$", Pattern.CASE_INSENSITIVE);
    private static final Pattern DYNAMIC_PATTERN = Pattern.compile("^docker\\d+$", Pattern.CASE_INSENSITIVE);

    public LreGroupHostValidator(LreRestApis restApis, TestContent content) {
        this.restApis = restApis;
        this.content = content;
        this.cloudTemplatesCache = getAllCloudTemplatesCache();
        this.specificLGsCache = getAllOnPremLGsCache();
    }

    private List<CloudTemplate> getAllCloudTemplatesCache() {
        if (cloudTemplatesCache == null) cloudTemplatesCache = restApis.fetchAllCloudTemplates();
        return cloudTemplatesCache;
    }

    private List<HostResponse> getAllOnPremLGsCache() {
        if (specificLGsCache == null) specificLGsCache = restApis.fetchLoadGenerators();
        return specificLGsCache;
    }


    public List<Host> validateAndPopulateHosts(YamlGroup group) {
        if (content.getLgDistribution().getType() == LGDistributionType.MANUAL) return parseHosts(group);
        return null;
    }

    private List<Host> parseHosts(YamlGroup group) {
        if (group.getHostnames() == null || group.getHostnames().isEmpty()) {
            throw new LreException("Group '" + group.getName() + "' has no hosts defined, " +
                    "but manual LG distribution requires explicit hostnames.");
        }


        Map<String, String> hostTemplateMap = parseHostTemplates(group.getHostTemplate());

        return Arrays.stream(group.getHostnames().split(","))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .map(hostname -> createHost(hostname, hostTemplateMap, group.getName()))
                .toList();
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
            validateOnPremLGs(hostname);
        }

        return host;
    }

    private void validateOnPremLGs(String lgName) {
        if (StringUtils.isNotEmpty(lgName)) {
            List<HostResponse> hosts = getAllOnPremLGsCache();
            boolean exists = hosts.stream().anyMatch(lg -> lgName.equalsIgnoreCase(lg.getName()));
            if (exists) log.debug("LG '{}' is available in LRE server", lgName);
            else {
                String availableHosts = hosts.stream().map(HostResponse::getName).collect(Collectors.joining(", "));
                throw new LreException(String.format("Given LG '%s' is not available on LRE. Expected one of: [%s]", lgName, availableHosts));
            }
        }
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
            CloudTemplate template = restApis.fetchCloudTemplateById(templateId);
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
