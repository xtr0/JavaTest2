package com.getintent.interview;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimplePageContextualizer implements PageContextualizer {
    private static final Logger LOG = LoggerFactory.getLogger(SimplePageContextualizer.class);

    private static final List<CategoryPattern> CATEGORY_PATTERNS;

    static {
        CATEGORY_PATTERNS = Collections.unmodifiableList(Arrays.asList(
                new CategoryPattern(PageCategory.AUTO, "cars?", "motors?", "vehicles?"),
                new CategoryPattern(PageCategory.FOOD, "food", "dish(?:es)?", "soups?"),
                new CategoryPattern(PageCategory.HOBBIES, "hobby", "hobbies")
        ));
    }

    private final PageLoader pageLoader = new PageLoader();

    @Override
    public PageCategory contextualize(final String url) {
        LOG.debug("contextualize: {}", url);

        try {
            final Map<PageCategory, Integer> frequencies = new HashMap<>(CATEGORY_PATTERNS.size());

            pageLoader.loadPage(url,
                    content -> CATEGORY_PATTERNS.stream().forEach(
                            entry -> frequencies.compute(entry.category, (k, v) -> {
                                final Matcher m = entry.pattern.matcher(content);

                                int count = 0;
                                while (m.find()) {
                                    count++;
                                }

                                return v == null ? count : v + count;
                            })
                    )
            );

            final Map.Entry<PageCategory, Integer> f =
                    frequencies.entrySet().stream().max(Map.Entry.comparingByValue()).get();

            LOG.debug("FQS {}", frequencies);
            final PageCategory category = f.getValue() > 0 ? f.getKey() : PageCategory.UNKNOWN;
            LOG.debug("{} : {}", category, url);

            return category;

        } catch (IOException e) {
            LOG.error("Failed to contextualize '" + url + "'", e);
            return PageCategory.UNKNOWN;
        }
    }

    private static class CategoryPattern {
        final PageCategory category;
        final Pattern pattern;

        CategoryPattern(final PageCategory category, final String... conditions) {
            this.category = category;

            final StringJoiner joiner = new StringJoiner("|");
            for (final String cond : conditions) {
                joiner.add(cond);
            }

            this.pattern =
                    Pattern.compile("\\b(?:" + joiner + ")\\b", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        }
    }
}
