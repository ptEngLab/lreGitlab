package com.lre.core.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import javax.net.ssl.SSLContext;
import java.security.cert.Certificate;
import java.util.Collection;

@Slf4j
public class HttpClientUtils {
    private static final String CA_CERT_PATH = System.getProperty("http.client.ca.path", "ca.pem");

    private HttpClientUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static CloseableHttpClient createClient() {
        return createClient(CA_CERT_PATH);
    }


    public static CloseableHttpClient createClient(String caCertPath) {
        try {
            CertificateLoader certLoader = new CertificateLoader(caCertPath);
            Collection<? extends Certificate> certs = certLoader.loadCertificates();
            SSLContext sslContext = SslContextProvider.buildSslContext(certs);
            return HttpClientFactory.createHttpClient(sslContext);

        } catch (Exception e) {
            log.warn("Failed to create secure HttpClient with CA: {}. Using default insecure client. Error: {}",
                    caCertPath, e.getMessage());
            log.debug("SSL context creation failure details:", e);
            return HttpClients.createDefault();
        }
    }

}