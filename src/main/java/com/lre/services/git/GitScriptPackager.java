package com.lre.services.git;

import com.lre.actions.apis.GitLabRestApis;
import com.lre.actions.exceptions.LreException;
import com.lre.actions.utils.CommonUtils;
import com.lre.model.git.GitLabCommit;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.lre.actions.utils.CommonUtils.deleteFolder;

/**
 * Handles packaging GitLab scripts into a zip file for upload to LRE.
 * Structure:
 * <temp-directory>/lre_sync/<commit-folder>/
 *     packaged/<zip-file>
 * Archive and extracted folders are deleted after packaging.
 */
@Slf4j
public record GitScriptPackager(GitLabRestApis gitLabRestApis) {

    private static final Path ROOT_SYNC_DIR = Paths.get(System.getProperty("java.io.tmpdir"), "lre_sync");

    /**
     * Prepares a packaged zip for a commit.
     *
     * @param commit the GitLab commit
     * @return path to packaged zip ready for upload
     */
    public Path prepare(GitLabCommit commit) throws IOException {
        Path commitTempDir = null;

        try {
            // Ensure root directory exists
            Files.createDirectories(ROOT_SYNC_DIR);

            // Create a commit-specific folder using last segment of commit.path
            commitTempDir = createCommitTempDir(commit);

            // Prepare subfolders
            Path archiveDir = commitTempDir.resolve("archive");
            Path extractedDir = commitTempDir.resolve("extracted");
            Path packagedDir = commitTempDir.resolve("packaged");
            Files.createDirectories(archiveDir);
            Files.createDirectories(extractedDir);
            Files.createDirectories(packagedDir);

            // Download archive
            Path archivePath = archiveDir.resolve("gitlab-archive.zip");
            log.debug("Downloading repository archive for commit {} into {}", commit.getSha(), archivePath);
            boolean success = gitLabRestApis.downloadRepositoryArchive(
                    commit.getSha(), commit.getPath(), archivePath.toString()
            );
            if (!success) {
                throw new LreException("Failed to download repository archive for: " + commit.getPath());
            }

            // Extract archive
            CommonUtils.unzip(archivePath.toFile(), extractedDir.toFile());

            // Find the .usr directory
            Path usrDir = findUsrParent(extractedDir);

            // Package into ZIP
            Path packagedZip = packagedDir.resolve(generateZipFileName(commit));
            CommonUtils.createZipFile(usrDir, packagedZip);

            log.debug("Packaged {} (commit {}) into {} ({} bytes)",
                    commit.getPath(), commit.getSha(), packagedZip, Files.size(packagedZip));

            // Delete intermediate folders
            deleteFolder(archiveDir);
            deleteFolder(extractedDir);

            return packagedZip;

        } catch (Exception e) {
            if (commitTempDir != null) deleteFolder(commitTempDir);
            throw e;
        }
    }

    /**
     * Creates a lowercase commit-specific temp directory under ROOT_SYNC_DIR
     * using the last segment of commit.path.
     */
    private Path createCommitTempDir(GitLabCommit commit) {
        String path = commit.getPath();
        String lastSegment = path.contains("/") ? path.substring(path.lastIndexOf("/") + 1) : path;
        String safeName = lastSegment.replaceAll("[^a-zA-Z0-9._-]", "_").toLowerCase();
        return ROOT_SYNC_DIR.resolve(safeName);
    }

    private Path findUsrParent(Path root) throws IOException {
        try (var stream = Files.walk(root)) {
            return stream
                    .filter(p -> p.toString().endsWith(".usr"))
                    .findFirst()
                    .map(Path::getParent)
                    .orElseThrow(() -> new IOException(".usr file not found in extracted archive: " + root));
        }
    }

    private String generateZipFileName(GitLabCommit commit) {
        String path = commit.getPath();
        String lastSegment = path.contains("/") ? path.substring(path.lastIndexOf("/") + 1) : path;
        String safePath = lastSegment.replaceAll("[^a-zA-Z0-9._-]", "_").toLowerCase().replace(".usr", "");
        String shaShort = commit.getSha().length() > 8 ? commit.getSha().substring(0, 8).toLowerCase() : commit.getSha().toLowerCase();
        return String.format("%s-%s.zip", safePath, shaShort);
    }

    public void cleanupCommitTempDir(Path commitTempDir) {
        deleteFolder(commitTempDir);
    }
}
