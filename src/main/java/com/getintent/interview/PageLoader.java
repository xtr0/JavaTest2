package com.getintent.interview;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Consumer;

public class PageLoader {
    private final CloseableHttpClient httpClient;

    public PageLoader() {
        final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        httpClient = HttpClients.custom().setConnectionManager(cm).build();
    }

    public void loadPage(final String url, final Consumer<String> consumer) throws IOException {
        final HttpGet httpGet = new HttpGet(url);
        final HttpContext context = new BasicHttpContext();

        try (final CloseableHttpResponse response = httpClient.execute(httpGet, context)) {
            final HttpEntity entity = response.getEntity();

            try (final BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()))) {

                String line;
                while ((line = br.readLine()) != null) {
                    consumer.accept(line);
                }
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            httpClient.close();
        } finally {
            super.finalize();
        }
    }
}
