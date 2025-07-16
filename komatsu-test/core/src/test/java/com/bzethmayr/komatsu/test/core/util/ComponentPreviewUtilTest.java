package com.bzethmayr.komatsu.test.core.util;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ComponentPreviewUtilTest {
    private final String stubTitle = "Component Preview";
    private final String stubCss = ".cmp-onent {}";
    private final String stubJs = "// component js";
    private final String stubHtml = "<div>component html</div>";

    @Test
    void combineInHtml_givenStubParts_producesStubContent() {

        final String result = ComponentPreviewUtil.combineInHtml(stubTitle, stubCss, stubJs, stubHtml);

        assertThat(result, allOf(
                startsWith("<html><head><title>"),
                containsString(stubTitle),
                containsString("<style type="),
                containsString(stubCss),
                containsString("</style>"),
                containsString(stubHtml),
                containsString("<script type="),
                containsString(stubJs),
                containsString("</script>"),
                endsWith("</body></html>")
        ));
    }

    @Test
    void combineInHtml_givenComponentName_producesContent() {

        final String result = ComponentPreviewUtil.combineInHtml("exercisethree");

        assertThat(result, allOf(
                startsWith("<html><head><title>"),
                containsString("<script type="),
                endsWith("</body></html>")

                ));
    }
}