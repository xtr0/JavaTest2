package com.getintent.interview;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PageContextualizerTest {
    Logger LOG = LoggerFactory.getLogger(PageContextualizerTest.class);
    PageContextualizer contextualizer;

    @BeforeClass
    public void setUp() {
        contextualizer = new SimplePageContextualizer();
    }

    @Test
    public void testUrls() {
        Assert.assertEquals(PageCategory.AUTO, contextualizer.contextualize("https://en.wikipedia.org/wiki/Volkswagen_Golf"));
        Assert.assertEquals(PageCategory.FOOD, contextualizer.contextualize("https://en.wikipedia.org/wiki/Borscht"));
        Assert.assertEquals(PageCategory.FOOD, contextualizer.contextualize("https://en.wikipedia.org/wiki/Peking_duck"));
        Assert.assertEquals(PageCategory.AUTO, contextualizer.contextualize("https://ru.wikipedia.org/wiki/Ferrari"));
    }
}