package com.lre.core.http;

import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Collection;

public record CertificateLoader(String caCertPath) {

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
