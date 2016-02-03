package com.getintent.interview;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            pageLoader.consumePage(url,
                    content -> CATEGORY_PATTERNS.stream().forEach(
                            categoryPattern -> frequencies.compute(categoryPattern.category, (k, v) -> {
                                final Matcher m = categoryPattern.pattern.matcher(content);

                                int count = 0;
                                while (m.find()) {
                                    count++;
                                }

                                return v == null ? count : v + count;
                            })
                    )
            );

            final Map.Entry<PageCategory, Integer> mostFrequent =
                    frequencies.entrySet().stream().max(Map.Entry.comparingByValue()).get();
            LOG.debug("FQS {}", frequencies);

            final PageCategory category = mostFrequent.getValue() > 0 ? mostFrequent.getKey() : PageCategory.UNKNOWN;
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

            final String patternString = Stream.of(conditions).collect(Collectors.joining("|", "\\b(?:", ")\\b"));
            this.pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        }
    }
}
