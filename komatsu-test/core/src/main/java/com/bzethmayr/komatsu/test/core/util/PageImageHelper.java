package com.bzethmayr.komatsu.test.core.util;

import com.adobe.cq.wcm.core.components.models.Image;
import com.bzethmayr.komatsu.test.core.models.ImagePojo;
import com.day.cq.wcm.api.Page;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.Optional;

public final class PageImageHelper {
    private PageImageHelper() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param resolver
     * @param page
     * @return data for page image, or empty
     */
    public static ImagePojo resolvePageImage(final ResourceResolver resolver, final Page page) {
        return Optional.ofNullable(page.getContentResource("image"))
                .map(r -> r.adaptTo(Image.class))
                .map(ImagePojo::fromImage)
                .orElse(null);
    }
}
