package com.bzethmayr.komatsu.test.core.models;

import com.bzethmayr.komatsu.test.core.testcontext.AppAemContext;
import com.day.cq.wcm.api.Page;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.path.PathBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;
import java.util.function.Function;

import static com.bzethmayr.komatsu.test.core.models.AlsoByAuthorModel.FIRST_NAME_PATH;
import static com.bzethmayr.komatsu.test.core.models.AlsoByAuthorModel.LAST_NAME_PATH;
import static com.bzethmayr.komatsu.test.core.testcontext.LocalTestingConstants.EXPECTED;
import static com.bzethmayr.komatsu.test.core.testcontext.LocalTestingConstants.LOCAL_DUMMY_TEMPLATE;
import static com.bzethmayr.komatsu.test.core.util.ProfileValueTestHelper.setUpHasPropertyValues;
import static com.day.cq.wcm.api.NameConstants.PN_PAGE_LAST_MOD_BY;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(AemContextExtension.class)
class AlsoByAuthorModelTest {

    private final AemContext context = AppAemContext.newAemContext();
    private final String expectedPage = new PathBuilder("/content").append(EXPECTED).toString();
    private final String expectedUser = "authorUserName";
    private final String expectedFirstName = "givenName";
    private final String expectedLastName = "familyName";
    private final UserManager users = mock(UserManager.class);
    private final User authorUser = mock(User.class);

    @BeforeEach
    void setUpUsers() {
        // Neither service registration method of context affects adaptTo result, so
        context.registerAdapter(ResourceResolver.class, UserManager.class,
                (Function<ResourceResolver, UserManager>) x -> users);
        assertDoesNotThrow(() -> doReturn(authorUser).when(users).getAuthorizable(expectedUser));
        assertDoesNotThrow(() -> setUpHasPropertyValues(authorUser, FIRST_NAME_PATH, expectedFirstName));
        assertDoesNotThrow(() -> setUpHasPropertyValues(authorUser, LAST_NAME_PATH, expectedLastName));
    }

    Page setUpPageExists(final Map<String, Object> properties) {
        return assertDoesNotThrow(() -> context.create().page(expectedPage, LOCAL_DUMMY_TEMPLATE, properties));
    }

    @Test
    void adaptTo_fromPage_returnsModelWithPojos() {
        final Page page = setUpPageExists(singletonMap(PN_PAGE_LAST_MOD_BY, expectedUser));

        AlsoByAuthorModel underTest = page.getContentResource().adaptTo(AlsoByAuthorModel.class);

        assertNotNull(underTest);
        final NamePojo author = underTest.getAuthor();
        assertNotNull(author);
        assertThat(author.getFirstName(), is(expectedFirstName));
        assertThat(author.getLastName(), is(expectedLastName));
    }
}
