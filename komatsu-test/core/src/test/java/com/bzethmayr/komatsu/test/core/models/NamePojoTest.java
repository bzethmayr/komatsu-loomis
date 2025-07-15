package com.bzethmayr.komatsu.test.core.models;

import org.junit.jupiter.api.Test;

import static junit.framework.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class NamePojoTest {

    @Test
    void namePojo_constructedWithNulls_returnsNulls() {

        final NamePojo underTest = new NamePojo(null, null);

        assertNull(underTest.getFirstName());
        assertNull(underTest.getLastName());
    }

    @Test
    void namePojo_constructedWithStrings_returnsStrings() {
        final String expectedFirst = "first";
        final String expectedLast = "last";

        final NamePojo underTest = new NamePojo(expectedFirst, expectedLast);

        assertThat(underTest.getFirstName(), is(expectedFirst));
        assertThat(underTest.getLastName(), is(expectedLast));
    }

}
