package com.lre.services.git;

import com.lre.model.git.GitLabCommit;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
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

    /**
     * Logs if there are no changes to sync.
     * @return true if no changes, false otherwise
     */
    public boolean logIfNoChanges() {
        if (!hasChanges()) {
            log.info("No changes detected - skipping sync");
            return true;
        }
        return false;
    }
}
