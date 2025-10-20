package com.lre.services.git;

import com.lre.model.git.GitLabCommit;

import java.util.*;
import java.util.stream.Collectors;

public class SyncAnalyzer {

    public SyncResult analyze(List<GitLabCommit> previous, List<GitLabCommit> current) {
        // Defensive null handling
        if (previous == null) previous = List.of();
        if (current == null) current = List.of();

        if (previous.isEmpty() && current.isEmpty()) {
            return SyncResult.empty();
        }

        Map<String, GitLabCommit> prevMap = toMap(previous);
        Map<String, GitLabCommit> currMap = toMap(current);

        List<GitLabCommit> toUpload = new ArrayList<>();
        List<GitLabCommit> toDelete = new ArrayList<>();
        List<GitLabCommit> unchanged = new ArrayList<>();

        // Detect deletions
        prevMap.forEach((path, commit) -> {
            if (!currMap.containsKey(path)) {
                toDelete.add(commit);
            }
        });

        // Detect additions or modifications
        currMap.forEach((path, curr) -> {
            GitLabCommit prev = prevMap.get(path);
            if (prev == null || hasChanged(prev, curr)) {
                toUpload.add(curr);
            } else {
                unchanged.add(curr);
            }
        });

        return new SyncResult(toUpload, toDelete, unchanged);
    }

    /**
     * Determines if a file has changed based on commit SHA.
     * Handles empty commit objects gracefully.
     */
    private boolean hasChanged(GitLabCommit previous, GitLabCommit current) {
        if (previous == null || previous.isEmpty()) return !current.isEmpty();
        if (current == null || current.isEmpty()) return !previous.isEmpty();
        return !Objects.equals(previous.getSha(), current.getSha());
    }

    /**
     * Creates a map of path → commit for fast lookup.
     */
    private Map<String, GitLabCommit> toMap(List<GitLabCommit> commits) {
        return commits.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(GitLabCommit::getPath, c -> c, (a, b) -> a));
    }
}
