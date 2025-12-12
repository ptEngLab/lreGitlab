package com.lre.services.git;

import com.lre.client.api.gitlab.GitLabRestApis;
import com.lre.model.git.GitLabCommit;
import com.lre.model.git.GitLabTreeItem;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.lre.client.api.builder.ApiUrlBuilderGitLab.GITLAB_PER_PAGE_RECORDS;

@Slf4j
public record GitRepositoryScanner(GitLabRestApis gitLabRestApis, int threadPoolSize) {

    public List<GitLabCommit> scanScripts() {
        Set<String> scriptDirs = findScriptDirectories();
        log.debug("Found {} script directories", scriptDirs.size());

        if (scriptDirs.isEmpty()) {
            log.info("No script directories found in repository");
            return List.of();
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        try {
            List<Future<GitLabCommit>> futures = scriptDirs.stream()
                    .map(path -> executor.submit(() -> fetchCommitForPath(path)))
                    .toList();

            List<GitLabCommit> commits = new ArrayList<>();
            for (Future<GitLabCommit> future : futures) {
                try {
                    commits.add(future.get());
                } catch (Exception e) {
                    log.warn("Failed to fetch commit for script path", e);
                }
            }

            return commits;
        } finally {
            shutdownExecutor(executor);
        }
    }

    private GitLabCommit fetchCommitForPath(String path) {
        GitLabCommit commit = gitLabRestApis.getLatestCommitForPath(path);
        if (commit == null || commit.isEmpty()) {
            log.debug("No commit found for {}", path);
            return new GitLabCommit("", "", path);
        }
        commit.setPath(path);
        return commit;
    }

    private Set<String> findScriptDirectories() {
        List<GitLabTreeItem> allItems = scanEntireRepository();
        return allItems.stream()
                .filter(i -> "blob".equals(i.getType()) && i.getName().endsWith(".usr"))
                .map(i -> i.getPath().substring(0, i.getPath().lastIndexOf('/')))
                .collect(Collectors.toSet());
    }

    private List<GitLabTreeItem> scanEntireRepository() {
        List<GitLabTreeItem> all = new ArrayList<>();
        int page = 1;
        boolean hasMoreItems = true;

        while (hasMoreItems) {
            List<GitLabTreeItem> items = Optional.ofNullable(gitLabRestApis.getRepositoryTree(page)).orElse(List.of());
            if (items.isEmpty()) hasMoreItems = false;
            else {
                all.addAll(items);
                hasMoreItems = items.size() == GITLAB_PER_PAGE_RECORDS;
                page++;
            }
        }

        return all;
    }

    private void shutdownExecutor(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }
}
