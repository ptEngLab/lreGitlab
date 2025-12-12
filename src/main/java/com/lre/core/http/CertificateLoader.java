package com.lre.core.http;

import com.lre.common.exceptions.LreException;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import java.util.List;

/**
 * Loads CA certificates from the classpath.
 */
public record CertificateLoader(String caCertPath) {

    /**
     * Loads X.509 certificates from the configured path.
     *
     * @return collection of certificates
     * @throws LreException if the certificate file is missing or invalid
     */
    public List<Certificate> loadCertificates() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(caCertPath)) {
            if (is == null) throw new LreException("CA certificate file not found: " + caCertPath);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Collection<? extends Certificate> rawCertificates = cf.generateCertificates(is);
            return rawCertificates.stream().map(Certificate.class::cast).toList();
        } catch (CertificateException | IOException e) {
            throw new LreException("Failed to load certificates from: " + caCertPath, e);
        }
    }
}