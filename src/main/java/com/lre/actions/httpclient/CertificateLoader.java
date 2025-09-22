package com.lre.actions.httpclient;

import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Collection;

public class CertificateLoader {

    private final String caCertPath;

    public CertificateLoader(String caCertPath) {
        this.caCertPath = caCertPath;
    }

    public Collection<? extends Certificate> loadCertificates() throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(caCertPath)) {
            if (is == null) {
                throw new RuntimeException("CA certificate file not found: " + caCertPath);
            }
            return cf.generateCertificates(is);
        }
    }
}
