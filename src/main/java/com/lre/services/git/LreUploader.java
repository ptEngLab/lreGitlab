package com.lre.services.git;

import com.lre.actions.apis.LreRestApis;
import com.lre.actions.runmodel.LreTestRunModel;
import com.lre.services.LreTestManager;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

/**
 * Handles uploading prepared script zips to LoadRunner Enterprise.
 */
@Slf4j
public record LreUploader(LreRestApis lreRestApis) {

    public void upload(LreTestRunModel lreModel, Path scriptZip) {
        new LreTestManager(lreModel, lreRestApis).uploadScriptsFromGitToLre(scriptZip);
        log.debug("Uploaded script {}: zip: {}", lreModel.getTestName(), scriptZip.getFileName());
    }
}
