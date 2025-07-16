package com.bzethmayr.komatsu.test.core.util;

import io.wcm.testing.mock.aem.junit5.AemContext;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.Value;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.bzethmayr.komatsu.test.core.models.AlsoByAuthorModel.FIRST_NAME_PATH;
import static com.bzethmayr.komatsu.test.core.models.AlsoByAuthorModel.LAST_NAME_PATH;
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

    public static void setUpUserManager(final AemContext context, final UserManager users) {
        // Neither service registration method of context affects adaptTo result, so
        context.registerAdapter(ResourceResolver.class, UserManager.class,
                (Function<ResourceResolver, UserManager>) x -> users);
    }

    public static void setUpFirstAndLastNames(final User fakeUser, final String expectedFirst, final String expectedLast) {
        assertDoesNotThrow(() -> setUpHasPropertyValues(fakeUser, FIRST_NAME_PATH, expectedFirst));
        assertDoesNotThrow(() -> setUpHasPropertyValues(fakeUser, LAST_NAME_PATH, expectedLast));
    }

    public static void setUpGetAuthorizable(final UserManager users, final User fakeUser, final String userName) {
        assertDoesNotThrow(() -> doReturn(fakeUser).when(users).getAuthorizable(userName));
    }
}
