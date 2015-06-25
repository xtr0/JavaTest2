package com.getintent.interview;

/**
 * Gets a page category from page.
 */
public interface PageContextualizer {
    public PageCategory contextualize(String url);
}
