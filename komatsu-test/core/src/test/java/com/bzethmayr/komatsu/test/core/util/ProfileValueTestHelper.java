package com.bzethmayr.komatsu.test.core.util;

import org.apache.jackrabbit.api.security.user.User;

import javax.jcr.Value;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public final class ProfileValueTestHelper {

    public static void setUpHasProperty(final User user, final String propertyName) {
        assertDoesNotThrow(() -> doReturn(true).when(user).hasProperty(propertyName));
    }

    public static void setUpPropertyValues(final User user, final String propertyName, final String... stringValues) {
        final Value[] mockValues = Stream.of(stringValues)
                .map(s -> {
                    final Value value = mock(Value.class);
                    assertDoesNotThrow(() -> doReturn(s).when(value).getString());
                    return value;
                })
                .toArray(Value[]::new);
        assertDoesNotThrow(() -> doReturn(mockValues).when(user).getProperty(propertyName));
    }

    public static void setUpHasPropertyValues(final User user, final String propertyName, final String... stringValues) {
        setUpHasProperty(user, propertyName);
        setUpPropertyValues(user, propertyName, stringValues);
    }
}
