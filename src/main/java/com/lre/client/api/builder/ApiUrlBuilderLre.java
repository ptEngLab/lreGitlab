package com.lre.client.api.builder;

import com.lre.client.runmodel.LreTestRunModel;
import lombok.Getter;
import lombok.experimental.Accessors;

import static com.lre.common.constants.ConfigConstants.*;

@Getter
@Accessors(fluent = true) // Makes getters return the builder for fluent API
public final class ApiUrlBuilderLre {
    private final TestUrlBuilder tests;
    private final RunUrlBuilder runs;
    private final ScriptUrlBuilder scripts;
    private final HostUrlBuilder hosts;
    private final TemplateUrlBuilder templates;
    private final AuthUrlBuilder auth;
    private final String lreServerUrl;
    private final String domain;
    private final String project;

    /**
     * Creates a new ApiUrlBuilderLre from the given model
     */
    public ApiUrlBuilderLre(LreTestRunModel model) {
        this.lreServerUrl = model.getLreServerUrl();

        String baseUrl = String.format(LRE_API_BASE_URL, lreServerUrl);
        String lreApiUrl = String.format(LRE_API_RESOURCES, baseUrl, model.getDomain(), model.getProject());
        String lreWebUrl = String.format(LRE_API_WEB_URL, lreServerUrl);

        this.domain = model.getDomain();
        this.project = model.getProject();

        this.tests = new TestUrlBuilder(lreApiUrl);
        this.runs = new RunUrlBuilder(lreApiUrl, lreWebUrl, lreServerUrl);
        this.scripts = new ScriptUrlBuilder(lreApiUrl);
        this.hosts = new HostUrlBuilder(lreApiUrl);
        this.templates = new TemplateUrlBuilder(lreApiUrl);
        this.auth = new AuthUrlBuilder(baseUrl, lreWebUrl);
    }
}