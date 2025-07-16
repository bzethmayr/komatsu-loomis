package com.bzethmayr.komatsu.test.core.models;

public class LinkPojo {
    private final String title;
    private final String relativePath;

    public LinkPojo(final String title, final String relativePath) {
        this.title = title;
        /*
         In the interest of brevity, omitting possible sanitization here.
         */
        this.relativePath = relativePath;
    }

    public String getTitle() {
        return title;
    }

    public String getRelativePath() {
        return relativePath;
    }
}
