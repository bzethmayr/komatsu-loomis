package com.bzethmayr.komatsu.test.core.util;

import org.apache.jackrabbit.api.security.user.User;
import org.junit.jupiter.api.Test;

import static com.bzethmayr.komatsu.test.core.testcontext.LocalTestingConstants.EXPECTED;
import static com.bzethmayr.komatsu.test.core.util.ProfileValueHelper.firstStringOrBlank;
import static com.bzethmayr.komatsu.test.core.util.ProfileValueTestHelper.setUpHasProperty;
import static com.bzethmayr.komatsu.test.core.util.ProfileValueTestHelper.setUpPropertyValues;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ProfileValueHelperTest {

    @Test
    void firstStringOrBlank_whenNoUser_throws() {
        assertThrows(RuntimeException.class, () -> firstStringOrBlank(null, "profile/thing"));
    }

    @Test
    void firstStringOrBlank_whenNoSuchProperty_returnsBlank() {
        final User user = mock(User.class);

        final String result = assertDoesNotThrow(() ->
                firstStringOrBlank(user, "nothing"));

        assertThat(result, isEmptyString());
    }


    @Test
    void firstStringOrBlank_whenInconsistentStateTrue_returnsBlank() {
        final User user = mock(User.class);
        setUpHasProperty(user, "none");

        final String result = assertDoesNotThrow(() ->
                firstStringOrBlank(user, "none"));

        assertThat(result, isEmptyString());
    }

    @Test
    void firstStringOrBlank_whenEmptyValues_returnsBlank() {
        final User user = mock(User.class);
        setUpHasProperty(user, "empty");
        setUpPropertyValues(user, "empty");

        final String result = assertDoesNotThrow(() ->
                firstStringOrBlank(user, "empty"));

        assertThat(result, isEmptyString());
    }

    @Test
    void firstStringOrBlank_whenInconsistentStateFalse_returnsBlank() {
        final User user = mock(User.class);
        setUpPropertyValues(user, "empty");

        final String result = assertDoesNotThrow(() ->
                firstStringOrBlank(user, "empty"));

        assertThat(result, isEmptyString());
    }

    @Test
    void firstStringOrBlank_whenSingleValue_returnsValue() {
        final User user = mock(User.class);
        setUpHasProperty(user, "extant");
        setUpPropertyValues(user, "extant", EXPECTED);

        final String result = assertDoesNotThrow(() ->
                firstStringOrBlank(user, "extant"));

        assertThat(result, is(EXPECTED));
    }

    @Test
    void firstStringOrBlank_whenTwoValues_returnsFirst() {
        final User user = mock(User.class);
        setUpHasProperty(user, "extant");
        setUpPropertyValues(user, "extant", "first", "second");

        final String result = assertDoesNotThrow(() ->
                firstStringOrBlank(user, "extant"));

        assertThat(result, is("first"));
    }
}