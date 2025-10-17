package com.lre.services.git;

import com.lre.model.git.GitLabTreeItem;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class GitLabRepoUtils {

    private GitLabRepoUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Get only folders (directories) from a repository tree
     */
    public static List<GitLabTreeItem> getFolders(List<GitLabTreeItem> allItems) {
        return allItems.stream()
                .filter(item -> "tree".equals(item.getType()))
                .collect(Collectors.toList());
    }

    /**
     * Get only files from a repository tree
     */
    public static List<GitLabTreeItem> getFiles(List<GitLabTreeItem> allItems) {
        return allItems.stream()
                .filter(item -> "blob".equals(item.getType()))
                .collect(Collectors.toList());
    }

    /**
     * Find parent directories containing .usr files
     */
    public static Set<String> findUsrParentDirs(List<GitLabTreeItem> allItems) {
        Set<String> parentDirs = new HashSet<>();

        for (GitLabTreeItem item : allItems) {
            if ("blob".equals(item.getType()) && item.getName().endsWith(".usr")) {
                String path = item.getPath();
                int lastSlash = path.lastIndexOf('/');
                String parentDir = lastSlash >= 0 ? path.substring(0, lastSlash) : "";
                parentDirs.add(parentDir);
            }
        }

        log.info("Found {} parent directories containing .usr files", parentDirs.size());
        return parentDirs;
    }
}
