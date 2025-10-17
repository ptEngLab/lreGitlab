package com.lre.actions.apis;

import com.lre.actions.runmodel.GitTestRunModel;
import lombok.Getter;
import org.apache.hc.core5.net.URIBuilder;

import java.net.URISyntaxException;

@Getter
public class ApiUrlBuilderGitLab {
    private final String gitlabBaseUrl;
    private final int projectId;
    private final String branch;

    // GitLab API endpoints
    private static final String PROJECTS_ENDPOINT = "projects";
    private static final String REPOSITORY_ENDPOINT = "repository";
    private static final String TREE_ENDPOINT = "tree";
    private static final String FILES_ENDPOINT = "files";
    private static final String COMMITS_ENDPOINT = "commits";
    public static final int GITLAB_PER_PAGE_RECORDS = 100;

    public ApiUrlBuilderGitLab(GitTestRunModel model) {
        this.gitlabBaseUrl = model.getGitServerUrl();
        this.projectId = model.getProjectId();
        this.branch = model.getBranch();
    }

    /**
     * Base project URL
     */
    public String getProjectUrl() {
        return String.format("%s/api/v4/%s/%d", gitlabBaseUrl, PROJECTS_ENDPOINT, projectId);
    }

    /**
     * Repository tree with recursive, branch, and pagination parameters
     */
    public String getRepositoryTreeUrl(int page) {
        try {
            String baseUrl = String.format("%s/%s/%s", getProjectUrl(), REPOSITORY_ENDPOINT, TREE_ENDPOINT);
            URIBuilder builder = new URIBuilder(baseUrl);

            builder.addParameter("recursive", "true");
            builder.addParameter("ref", branch);
            builder.addParameter("per_page", String.valueOf(GITLAB_PER_PAGE_RECORDS));
            builder.addParameter("page", String.valueOf(page));

            return builder.build().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URL for repository tree", e);
        }
    }

    /**
     * Repository tree for specific path
     */
    public String getRepositoryTreeUrl(String path, int page) {
        try {
            String baseUrl = String.format("%s/%s/%s", getProjectUrl(), REPOSITORY_ENDPOINT, TREE_ENDPOINT);
            URIBuilder builder = new URIBuilder(baseUrl);

            builder.addParameter("recursive", "true");
            builder.addParameter("ref", branch);
            builder.addParameter("per_page", String.valueOf(GITLAB_PER_PAGE_RECORDS));
            builder.addParameter("page", String.valueOf(page));

            if (path != null && !path.trim().isEmpty()) {
                builder.addParameter("path", path);
            }

            return builder.build().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URL for repository tree", e);
        }
    }

    /**
     * Get specific file content
     */
    public String getFileContentUrl(String filePath) {
        try {
            String baseUrl = String.format("%s/%s/%s/%s/raw",
                    getProjectUrl(), REPOSITORY_ENDPOINT, FILES_ENDPOINT, filePath);
            URIBuilder builder = new URIBuilder(baseUrl);
            builder.addParameter("ref", branch);
            return builder.build().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URL for file content", e);
        }
    }

    /**
     * Get commits for a specific path (directory or file) - returns latest commit
     */
    public String getCommitsForPathUrl(String path) {
        try {
            String baseUrl = String.format("%s/%s", getProjectUrl(), COMMITS_ENDPOINT);
            URIBuilder builder = new URIBuilder(baseUrl);

            if (path != null && !path.trim().isEmpty()) {
                builder.addParameter("path", path);
            }

            builder.addParameter("ref_name", branch);
            builder.addParameter("per_page", "1"); // We only need the latest commit
            return builder.build().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URL for commits by path", e);
        }
    }
}