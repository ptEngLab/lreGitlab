package com.lre.services.git;

import com.lre.model.git.GitLabCommit;

import java.util.List;

public record SyncResult(
        List<GitLabCommit> scriptsToUpload,
        List<GitLabCommit> scriptsToDelete,
        List<GitLabCommit> unchangedScripts
) {

    public int totalScripts() {
        return scriptsToUpload.size() + scriptsToDelete.size() + unchangedScripts.size();
    }

    public boolean hasChanges() {
        return !scriptsToUpload.isEmpty() || !scriptsToDelete.isEmpty();
    }

    public static SyncResult empty() {
        return new SyncResult(List.of(), List.of(), List.of());
    }
}
