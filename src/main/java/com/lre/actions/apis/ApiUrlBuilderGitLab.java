package com.lre.actions.apis;

import com.lre.actions.runmodel.GitTestRunModel;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;

import java.net.URISyntaxException;

import static com.lre.actions.utils.ConfigConstants.COMMIT_HISTORY_ARTIFACT_PATH;

@Getter
public class ApiUrlBuilderGitLab {
    private final String gitlabBaseUrl;
    private final int projectId;
    private final String branch;
    private final String jobName;


    // GitLab API endpoints
    private static final String PROJECTS_ENDPOINT = "projects";
    private static final String REPOSITORY_ENDPOINT = "repository";
    private static final String TREE_ENDPOINT = "tree";
    private static final String COMMITS_ENDPOINT = "commits";
    public static final int GITLAB_PER_PAGE_RECORDS = 100;


    public ApiUrlBuilderGitLab(GitTestRunModel model) {
        this.gitlabBaseUrl = model.getGitServerUrl();
        this.projectId = model.getProjectId();
        this.branch = model.getBranch();
        this.jobName = model.getJobName();
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
        return getRepositoryTreeUrl(null, page);
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

            if (path != null && !path.trim().isEmpty()) builder.addParameter("path", path);

            return builder.build().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URL for repository tree", e);
        }
    }


    public String getCommitHistoryArtifactUrl() {
        try {
            String baseUrl = String.format("%s/jobs/artifacts/%s/raw/%s",
                    getProjectUrl(), branch, COMMIT_HISTORY_ARTIFACT_PATH);
            URIBuilder builder = new URIBuilder(baseUrl);
            builder.addParameter("job", jobName);
            return builder.build().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URL for file content", e);
        }
    }

    /**
     * Get commits for a specific path (directory or file) - returns latest commit
     */
    public String getLatestCommitUrlForPath(String path) {
        try {
            String baseUrl = String.format("%s/%s/%s", getProjectUrl(), REPOSITORY_ENDPOINT, COMMITS_ENDPOINT);
            URIBuilder builder = new URIBuilder(baseUrl);

            if (path != null && !path.trim().isEmpty()) {
                builder.addParameter("path", path);
            }

            builder.addParameter("ref_name", branch);
            builder.addParameter("per_page", "1"); // Only need latest commit

            return builder.build().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URL for commits by path", e);
        }
    }

    /**
     * Builds URL to download an archive (.zip) of the repository or a specific path.
     * Example:
     *   GET /projects/:id/repository/archive.zip?sha=abcdef&path=scripts/LoginTest
     */
    public String getRepositoryArchiveUrl(String commitSha, String path) {
        try {
            String baseUrl = String.format("%s/%s/archive.zip", getProjectUrl(), REPOSITORY_ENDPOINT);
            URIBuilder builder = new URIBuilder(baseUrl);

            if (StringUtils.isNotBlank(commitSha)) {
                builder.addParameter("sha", commitSha);
            }
            if (StringUtils.isNotBlank(path)) {
                builder.addParameter("path", path);
            }

            return builder.build().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URL for repository archive", e);
        }
    }


}