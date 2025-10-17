package com.lre.actions.apis;

import com.lre.actions.runmodel.GitTestRunModel;
import com.lre.core.http.HttpClientUtils;
import com.lre.model.git.GitLabCommit;
import com.lre.model.git.GitLabTreeItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import java.util.ArrayList;
import java.util.List;

import static com.lre.actions.apis.ApiUrlBuilderGitLab.GITLAB_PER_PAGE_RECORDS;

@Slf4j
public class GitLabRestApis implements AutoCloseable {

    private final CloseableHttpClient httpClient;
    private final ApiUrlBuilderGitLab urlBuilder;
    private final ApiRequestExecutor executor;

    public GitLabRestApis(GitTestRunModel model) {
        this.httpClient = HttpClientUtils.createClientWithToken(model.getGitlabToken());
        this.urlBuilder = new ApiUrlBuilderGitLab(model);
        this.executor = new ApiRequestExecutor(httpClient);
    }

    /**
     * Get the entire repository tree recursively from the root
     */
    public List<GitLabTreeItem> getRepositoryTreeRecursively() {
        List<GitLabTreeItem> allItems = new ArrayList<>();
        int page = 1;
        boolean hasMorePages = true;

        while (hasMorePages) {
            String url = urlBuilder.getRepositoryTreeUrl(page);
            List<GitLabTreeItem> pageItems = executor.fetchList(url, GitLabTreeItem.class, "Repository tree");

            if (pageItems.isEmpty()) hasMorePages = false;
            else {
                allItems.addAll(pageItems);
                page++;
            }

            // Stop if we get fewer items than per_page
            if (pageItems.size() < GITLAB_PER_PAGE_RECORDS) hasMorePages = false;

        }

        log.debug("Retrieved {} items from repository tree", allItems.size());
        return allItems;
    }

    /**
     * Get file content as string
     */
    public String getFileContent(String filePath) {
        String url = urlBuilder.getFileContentUrl(filePath);
        return executor.fetchById(url, String.class, "File content");
    }

    /**
     * Download a file to local path
     */
    public boolean downloadFile(String filePath, String destPath) {
        String url = urlBuilder.getFileContentUrl(filePath);
        return executor.download(url, destPath);
    }

    /**
     * Get latest commit SHA for a specific path
     */
    /**
     * Get latest commit SHA for a specific path
     */
    public String getLatestCommitShaForPath(String path) {
        String url = urlBuilder.getCommitsForPathUrl(path);
        List<GitLabCommit> commits = executor.fetchList(url, GitLabCommit.class, "Commits for path");
        return commits.stream()
                .findFirst()
                .map(GitLabCommit::getSha)
                .orElse(null);
    }

    @Override
    public void close() throws Exception {
        if (httpClient != null) httpClient.close();
    }
}
