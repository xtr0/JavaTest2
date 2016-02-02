package com.getintent.interview;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimplePageContextualizer implements PageContextualizer {
    private static final Logger LOG = LoggerFactory.getLogger(SimplePageContextualizer.class);

    private static final Map<PageCategory, Pattern> CATEGORY_PATTERNS;

    static {
        final Map<PageCategory, Pattern> patterns = new HashMap<>();

        Function<String[], Pattern> patternCreator = conditions -> {
            final StringJoiner joiner = new StringJoiner("|");
            for (final String cond : conditions) {
                joiner.add(cond);
            }

            return Pattern.compile("\\b(?:" + joiner + ")\\b", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        };

        patterns.put(PageCategory.AUTO, patternCreator.apply(new String[]{"cars?", "motors?", "vehicles?"}));
        patterns.put(PageCategory.FOOD, patternCreator.apply(new String[]{"food", "dish(?:es)?", "soups?"}));
        patterns.put(PageCategory.HOBBIES, patternCreator.apply(new String[]{"hobby", "hobbies"}));

        CATEGORY_PATTERNS = Collections.unmodifiableMap(patterns);
    }

    @Override
    public PageCategory contextualize(String url) {
        LOG.debug("contextualize: {}", url);

        try {
            final String content = getPageContent(url);
            final PageCategory category = detectContentCategory(content);

            LOG.debug("URL {} = {}", url, category);

            return category;

        } catch (IOException e) {
            LOG.error("Failed to contextualize '" + url + "'", e);

            return PageCategory.UNKNOWN;
        }
    }

    private PageCategory detectContentCategory(final String content) {
        class Frequency {
            final PageCategory category;
            final int value;

            Frequency(final Map.Entry<PageCategory, Pattern> entry) {
                category = entry.getKey();

                final Matcher m = entry.getValue().matcher(content);
                int count = 0;
                while (m.find()) {
                    count++;
                }

                value = count;
            }
        }

        final Frequency f =
                CATEGORY_PATTERNS.entrySet().parallelStream()
                        .map(entry -> new Frequency(entry))
                        .max((f1, f2) -> Integer.compare(f1.value, f2.value))
                        .get();

        return f.value > 0 ? f.category : PageCategory.UNKNOWN;
    }

    private String getPageContent(final String pageUrl) throws IOException {
        final URL url = new URL(pageUrl);
        final URLConnection conn = url.openConnection();

        try (final BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {

            final StringBuilder content = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                content.append(line);
            }

            return content.toString();
        }
    }
}
