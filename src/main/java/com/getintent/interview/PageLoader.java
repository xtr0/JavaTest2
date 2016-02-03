package com.getintent.interview;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Consumer;

public class PageLoader {

    public void consumePage(final String url, final Consumer<String> consumer) throws IOException {
        final HttpGet httpGet = new HttpGet(url);
        final RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();

        try (
                final CloseableHttpClient httpClient =
                        HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
                final CloseableHttpResponse response = httpClient.execute(httpGet);
        ) {
            final HttpEntity entity = response.getEntity();

            try (final BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()))) {

                String line;
                while ((line = br.readLine()) != null) {
                    consumer.accept(line);
                }
            }
        }
    }
}
