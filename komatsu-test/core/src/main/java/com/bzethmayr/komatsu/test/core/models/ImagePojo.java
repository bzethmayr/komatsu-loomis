package com.bzethmayr.komatsu.test.core.models;

import com.adobe.cq.wcm.core.components.models.Image;

import java.util.Optional;

/**
 * Flat DTO subset of Image
 * Too big for a public constructor
 */
public class ImagePojo {
    private final String src;
    private final String srcset;
    private final boolean lazyEnabled;
    private final String width;
    private final String height;
    private final String sizes;
    private final String alt;
    private final String title;

    private ImagePojo(
            final String src, final String srcset, final boolean lazyEnabled, final String width, final String height,
            final String sizes, final String alt, final String title
    ) {
        this.src = src;
        this.srcset = srcset;
        this.lazyEnabled = lazyEnabled;
        this.width = width;
        this.height = height;
        this.sizes = sizes;
        this.alt = alt;
        this.title = title;
    }

    public String getSrc() {
        return src;
    }

    public String getSrcset() {
        return srcset;
    }

    public boolean isLazyEnabled() {
        return lazyEnabled;
    }

    public String getWidth() {
        return width;
    }

    public String getHeight() {
        return height;
    }

    public String getSizes() {
        return sizes;
    }

    public String getAlt() {
        return alt;
    }

    public String getTitle() {
        return title;
    }

    public static class Builder {
        private String src;
        private String srcset;
        private boolean lazyEnabled;
        private String width;
        private String height;
        private String sizes;
        private String alt;
        private String title;

        public ImagePojo build() {
            return new ImagePojo(src, srcset, lazyEnabled, width, height, sizes, alt, title);
        }

        public Builder setSrc(String src) {
            this.src = src;
            return this;
        }

        public Builder setSrcset(String srcset) {
            this.srcset = srcset;
            return this;
        }

        public Builder setLazyEnabled(boolean lazyEnabled) {
            this.lazyEnabled = lazyEnabled;
            return this;
        }

        public Builder setWidth(String width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(String height) {
            this.height = height;
            return this;
        }

        public Builder setSizes(String sizes) {
            this.sizes = sizes;
            return this;
        }

        public Builder setAlt(String alt) {
            this.alt = alt;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ImagePojo fromImage(final Image image) {
        return builder()
                .setSrc(image.getSrc())
                .setSrcset(image.getSrcset())
                .setLazyEnabled(image.isLazyEnabled())
                .setWidth(image.getWidth())
                .setHeight(image.getHeight())
                .setSizes(image.getSizes())
                .setAlt(Optional.ofNullable(image.getAlt()).orElse("true"))
                .setTitle(image.displayPopupTitle() ? image.getTitle() : "")
                .build();
    }
}
