package com.lre.client.api.gitlab;

import com.lre.client.api.base.ApiRequestExecutor;
import com.lre.client.api.builder.ApiUrlBuilderGitLab;
import com.lre.client.runmodel.GitTestRunModel;
import com.lre.core.http.HttpClientUtils;
import com.lre.model.git.GitLabCommit;
import com.lre.model.git.GitLabTreeItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import java.util.List;

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
    public List<GitLabTreeItem> getRepositoryTree(int page) {
        String url = urlBuilder.getRepositoryTreeUrl(page);
        return executor.fetchList(url, GitLabTreeItem.class, "Repository tree");
    }


    public boolean downloadGitCommitHistoryArtifact(String destPath) {
        String url = urlBuilder.getCommitHistoryArtifactUrl();
        return executor.download(url, destPath);
    }

    /**
     * Get latest commit SHA for a specific path
     */
    public GitLabCommit getLatestCommitForPath(String path) {
        String url = urlBuilder.getLatestCommitUrlForPath(path);
        List<GitLabCommit> commits = executor.fetchList(url, GitLabCommit.class, "Latest commit for path");
        return  (commits == null || commits.isEmpty()) ? new GitLabCommit(): commits.get(0);
    }

    public boolean downloadRepositoryArchive(String commitSha, String path, String destPath) {
        String url = urlBuilder.getRepositoryArchiveUrl(commitSha, path);
        return executor.download(url, destPath);
    }

    @Override
    public void close() throws Exception {
        if (httpClient != null) httpClient.close();
    }
}
