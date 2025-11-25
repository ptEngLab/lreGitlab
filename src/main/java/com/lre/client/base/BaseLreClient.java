package com.lre.client.base;

import com.lre.client.api.lre.LreRestApis;
import com.lre.client.runmodel.LreTestRunModel;
import com.lre.services.lre.auth.LreAuthenticationManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseLreClient implements AutoCloseable {

    protected final LreTestRunModel model;
    protected final LreRestApis lreRestApis;
    protected final LreAuthenticationManager authManager;

    protected BaseLreClient(LreTestRunModel model) {
        this.model = model;
        this.lreRestApis = new LreRestApis(model);
        this.authManager = new LreAuthenticationManager(lreRestApis, model);

        login();
    }

    private void login() {
        try {
            log.info("[{}] Logging into LRE with Domain: {}, Project: {}", this.getClass().getSimpleName(), model.getDomain(), model.getProject());
            authManager.login();
            log.info("[{}] Logged in successfully", this.getClass().getSimpleName());
        } catch (Exception e) {
            log.error("[{}] Login failed: {}", this.getClass().getSimpleName(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void close() {
        try {
            authManager.close();
            log.info("[{}] Logged out from LRE", this.getClass().getSimpleName());
        } catch (Exception e) {
            log.warn("[{}] Cleanup error: {}", this.getClass().getSimpleName(), e.getMessage());
        }
    }

    protected void trace(String msg, Object... args) {
        if (log.isDebugEnabled()) {
            log.debug("[{}] {}", this.getClass().getSimpleName(), String.format(msg, args));
        }
    }

}
