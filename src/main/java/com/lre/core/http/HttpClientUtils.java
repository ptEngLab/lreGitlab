package com.lre.core.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;

import javax.net.ssl.SSLContext;
import java.security.cert.Certificate;
import java.util.Collection;
import java.util.List;

@Slf4j
public class HttpClientUtils {
    private static final String CA_CERT_PATH = System.getProperty("http.client.ca.path", "ca.pem");

    private HttpClientUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /** Default client without token */
    public static CloseableHttpClient createClient() {
        return createClient(CA_CERT_PATH, null);
    }

    /** Client with optional token */
    public static CloseableHttpClient createClient(String caCertPath, String gitlabToken) {
        try {
            CertificateLoader certLoader = new CertificateLoader(caCertPath);
            Collection<? extends Certificate> certs = certLoader.loadCertificates();
            SSLContext sslContext = SslContextProvider.buildSslContext(certs);

            List<Header> headers = null;
            if (gitlabToken != null && !gitlabToken.isEmpty()) {
                headers = List.of(
                        new BasicHeader("Authorization", "Bearer " + gitlabToken),
                        new BasicHeader("Content-Type", "application/json")
                );
            }

            return HttpClientFactory.createHttpClient(sslContext, headers);

        } catch (Exception e) {
            log.warn("Failed to create secure HttpClient with CA: {}. Using default insecure client. Error: {}",
                    caCertPath, e.getMessage());
            log.debug("SSL context creation failure details:", e);
            return HttpClients.createDefault();
        }
    }

    /** Convenience method for GitLab token only */
    public static CloseableHttpClient createClientWithToken(String gitlabToken) {
        return createClient(CA_CERT_PATH, gitlabToken);
    }

}