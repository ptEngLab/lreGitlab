package com.lre.core.http;

import javax.net.ssl.SSLContext;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Collection;

import org.apache.hc.core5.ssl.SSLContextBuilder;

public class SslContextProvider {

    public static SSLContext buildSslContext(Collection<? extends Certificate> certificates) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);

        int index = 0;
        for (Certificate cert : certificates) {
            keyStore.setCertificateEntry("ca" + index++, cert);
        }

        return SSLContextBuilder.create()
                .loadTrustMaterial(keyStore, null)
                .build();
    }
}
