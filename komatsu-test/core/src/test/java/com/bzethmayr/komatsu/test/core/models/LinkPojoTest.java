package com.bzethmayr.komatsu.test.core.models;

import org.junit.jupiter.api.Test;

import static junit.framework.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

class LinkPojoTest {

    @Test
    void linkPojo_constructedWithNulls_returnsNulls() {

        final LinkPojo underTest = new LinkPojo(null, null);

        assertThat(underTest.getTitle(), is(nullValue()));
        assertThat(underTest.getRelativePath(), is(nullValue()));
    }

    @Test
    void linkPojo_constructedWithStrings_returnsStrings() {
        final String expectedTitle = "title";
        final String expectedRelativePath = "relative/path";

        final LinkPojo underTest = new LinkPojo(expectedTitle, expectedRelativePath);

        assertThat(underTest.getTitle(), is(expectedTitle));
        assertThat(underTest.getRelativePath(), is(expectedRelativePath));
    }
}
