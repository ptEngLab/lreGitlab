package com.lre.core.http;

import com.lre.common.exceptions.LreException;
import lombok.experimental.UtilityClass;
import org.apache.hc.core5.ssl.SSLContextBuilder;

import javax.net.ssl.SSLContext;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.io.IOException;

@UtilityClass
public class SslContextProvider {

    public static SSLContext buildSslContext(Collection<? extends Certificate> certificates) {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);

            int index = 0;
            for (Certificate cert : certificates) {
                keyStore.setCertificateEntry("ca" + index++, cert);
            }

            return SSLContextBuilder.create()
                    .loadTrustMaterial(keyStore, null)
                    .build();
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            throw new LreException("Failed to build SSL context from provided certificates", e);
        } catch (Exception e) {
            throw new LreException("Unexpected error building SSL context", e);
        }
    }
}
