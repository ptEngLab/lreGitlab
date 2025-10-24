package com.lre.services.lre.auth;

import com.lre.client.api.lre.LreRestApis;
import com.lre.client.runmodel.LreTestRunModel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LreAuthenticationManager {

    private final LreRestApis lreRestApis;
    private final LreTestRunModel model;
    private boolean loggedIn;

    public LreAuthenticationManager(LreRestApis lreRestApis, LreTestRunModel model) {
        this.lreRestApis = lreRestApis;
        this.model = model;
        this.loggedIn = false;
    }

    public void login() {
        if(loggedIn) return;
        loggedIn = lreRestApis.login(model.getUserName(), model.getPassword(), model.isAuthenticateWithToken());
    }

    public void logout() {
        if (!loggedIn)  return;
        lreRestApis.logout();
        loggedIn = false;
    }

    public void close() {
        logout();
        try {
            lreRestApis.close();
        } catch (Exception e) {
            log.error("Error closing LRE REST API client: {}", e.getMessage());
        }
    }
}
