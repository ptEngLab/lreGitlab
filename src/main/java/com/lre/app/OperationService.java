package com.lre.app;

import com.lre.client.runclient.EmailUtils;
import com.lre.client.runclient.GitSyncClient;
import com.lre.client.runclient.LreRunClient;
import com.lre.client.runclient.ResultsExtractionClient;
import com.lre.client.runmodel.EmailConfigModel;
import com.lre.client.runmodel.GitTestRunModel;
import com.lre.client.runmodel.LreTestRunModel;
import com.lre.common.exceptions.LreException;
import com.lre.common.exceptions.OperationExecutionException;

public class OperationService {

    public boolean runLreTest(LreTestRunModel model) throws OperationExecutionException {
        if (!model.isRunTestFromGitlab()) {
            return true;
        }

        try (LreRunClient client = new LreRunClient(model)) {
            client.startRun();
            client.printRunSummary();
            return true;

        } catch (LreException e) {
            throw new OperationExecutionException("LRE test execution failed", e);
        } catch (RuntimeException e) {
            throw new OperationExecutionException("Unexpected runtime error during LRE test", e);
        }
    }

    public boolean syncGitlabWithLre(GitTestRunModel git, LreTestRunModel lre) throws OperationExecutionException {
        try (GitSyncClient client = new GitSyncClient(git, lre)) {
            return client.sync();
        } catch (LreException e) {
            throw new OperationExecutionException("GitLab ↔ LRE synchronization failed", e);
        } catch (RuntimeException e) {
            throw new OperationExecutionException("Unexpected runtime error during GitLab ↔ LRE sync", e);
        }
    }

    public boolean sendEmail(EmailConfigModel email, LreTestRunModel lre) throws OperationExecutionException {
        try {
            return EmailUtils.sendEmailWithPipelineArtifacts(email, lre);
        } catch (RuntimeException e) {
            throw new OperationExecutionException("Email sending failed", e);
        }
    }

    public boolean extractResults(LreTestRunModel lre) throws OperationExecutionException {
        try (ResultsExtractionClient client = new ResultsExtractionClient(lre)) {
            client.fetchRunDetails();
            client.publishHtmlReportIfFinished();
            client.publishAnalysedReportIfFinished();
            client.extractRunReportsToExcel();
            client.createRunResultsForEmail();
            return true;

        } catch (LreException e) {
            throw new OperationExecutionException("Results extraction failed", e);
        } catch (RuntimeException e) {
            throw new OperationExecutionException("Unexpected runtime error during results extraction", e);
        }
    }
}
