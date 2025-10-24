package com.lre.services.git;

import com.lre.model.git.GitLabCommit;

import java.util.*;
import java.util.stream.Collectors;

public class SyncAnalyzer {

    /**
     * Compares previous commits with current commits and determines
     * which scripts need to be uploaded, deleted, or are unchanged.
     */
    public SyncResult analyze(List<GitLabCommit> previous, List<GitLabCommit> current) {
        // Defensive null handling
        previous = previous != null ? previous : List.of();
        current = current != null ? current : List.of();

        if (previous.isEmpty() && current.isEmpty()) {
            return SyncResult.empty();
        }

        Map<String, GitLabCommit> prevMap = toMap(previous);
        Map<String, GitLabCommit> currMap = toMap(current);

        List<GitLabCommit> toUpload = new ArrayList<>();
        List<GitLabCommit> toDelete = new ArrayList<>();
        List<GitLabCommit> unchanged = new ArrayList<>();

        // Deletions
        previous.forEach(commit -> {
            if (!currMap.containsKey(commit.getPath())) {
                toDelete.add(commit);
            }
        });

        // Additions or modifications
        current.forEach(commit -> {
            GitLabCommit prev = prevMap.get(commit.getPath());
            if (prev == null || hasChanged(prev, commit)) {
                toUpload.add(commit);
            } else {
                unchanged.add(commit);
            }
        });

        return new SyncResult(toUpload, toDelete, unchanged);
    }

    private boolean hasChanged(GitLabCommit previous, GitLabCommit current) {
        if (previous == null || previous.isEmpty()) return !current.isEmpty();
        if (current == null || current.isEmpty()) return !previous.isEmpty();
        return !Objects.equals(previous.getSha(), current.getSha());
    }

    private Map<String, GitLabCommit> toMap(List<GitLabCommit> commits) {
        return commits.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(GitLabCommit::getPath, c -> c, (a, b) -> a));
    }
}
