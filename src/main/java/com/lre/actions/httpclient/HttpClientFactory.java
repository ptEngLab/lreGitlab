package com.lre.actions.httpclient;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.util.Timeout;
import org.apache.hc.client5.http.config.ConnectionConfig;

import javax.net.ssl.SSLContext;

public class HttpClientFactory {

    private static final Timeout SOCKET_TIMEOUT = Timeout.ofMinutes(3);
    private static final Timeout CONNECTION_TIMEOUT = Timeout.ofMinutes(1);
    private static final Timeout VALIDATE_AFTER_INACTIVITY = Timeout.ofMinutes(1);
    private static final int MAX_CONNECTIONS_TOTAL = 100;
    private static final int MAX_CONNECTIONS_PER_ROUTE = 20;

    public static CloseableHttpClient createHttpClient(SSLContext sslContext) {
        try {
            PoolingHttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                    .setTlsSocketStrategy(new DefaultClientTlsStrategy(sslContext))
                    .setMaxConnTotal(MAX_CONNECTIONS_TOTAL)
                    .setMaxConnPerRoute(MAX_CONNECTIONS_PER_ROUTE)
                    .setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(SOCKET_TIMEOUT).build())
                    .setDefaultConnectionConfig(ConnectionConfig.custom()
                            .setConnectTimeout(CONNECTION_TIMEOUT)
                            .setValidateAfterInactivity(VALIDATE_AFTER_INACTIVITY)
                            .build())
                    .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.STRICT)
                    .setConnPoolPolicy(PoolReusePolicy.LIFO)
                    .build();
            return HttpClients.custom().setConnectionManager(cm).build();
        } catch (Exception e) {
            // fallback to default
            return HttpClients.createDefault();
        }
    }
}
