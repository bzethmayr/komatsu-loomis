package com.bzethmayr.komatsu.test.core.util;

import com.adobe.cq.wcm.core.components.models.Image;
import io.wcm.testing.mock.aem.junit5.AemContext;
import org.apache.sling.api.resource.Resource;

import java.util.Map;
import java.util.function.Function;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public final class PageImageTestHelper {
    private PageImageTestHelper() {
        throw new UnsupportedOperationException();
    }

    public static void setUpPageImage(final AemContext context, final String pagePath) {
        final String imagePath = pagePath + "/jcr:content/image";
        context.create().resource(imagePath, Map.of(
                "jcr:resourceType", "komatsutest/components/image",
                "fileReference", "/content/dam/komatsutest/asset.jpg"
        ));
    }

    public static Image setUpImageAdapter(final AemContext context) {
        final Image image = mock(Image.class);
        doReturn("/content/dam/komatsutest/asset.jpg/cq5dam.thumbnail.48.48.png")
                .when(image).getSrc();
        doReturn("48").when(image).getWidth();
        doReturn("48").when(image).getHeight();
        context.registerAdapter(Resource.class, Image.class, (Function<Resource, Image>) x -> image);
        return image;
    }
}
