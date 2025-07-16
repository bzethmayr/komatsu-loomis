package com.bzethmayr.komatsu.test.core.util;

import org.apache.jackrabbit.api.security.user.User;

import javax.jcr.RepositoryException;
import javax.jcr.Value;

public final class ProfileValueHelper {
    private ProfileValueHelper() {
        throw new UnsupportedOperationException();
    }

    public static String firstStringOrBlank(final User user, final String propertyName) throws RepositoryException {
        if (user.hasProperty(propertyName)) {
            final Value[] values = user.getProperty(propertyName);
            if (values != null && values.length > 0) {
                return values[0].getString(); // we'd rather get the consequences if we have the type wrong.
            }
        }
        return "";
    }
}
