package com.bzethmayr.komatsu.test.core.models;

import com.bzethmayr.komatsu.test.core.testcontext.AppAemContext;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.Page;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.path.PathBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;

import static com.bzethmayr.komatsu.test.core.testcontext.LocalTestingConstants.EXPECTED;
import static com.bzethmayr.komatsu.test.core.testcontext.LocalTestingConstants.LOCAL_DUMMY_TEMPLATE;
import static com.bzethmayr.komatsu.test.core.util.ProfileValueTestHelper.*;
import static com.bzethmayr.komatsu.test.core.util.QueryBuilderTestHelper.setUpQueryBuilder;
import static com.bzethmayr.komatsu.test.core.util.QueryBuilderTestHelper.setUpSomeHits;
import static com.day.cq.wcm.api.NameConstants.PN_PAGE_LAST_MOD_BY;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    private final QueryBuilder queries = mock(QueryBuilder.class);
    private SearchResult result;

    @BeforeEach
    void setUpUsers() {
        setUpUserManager(context, users);
        setUpFirstAndLastNames(authorUser, expectedFirstName, expectedLastName);
        setUpGetAuthorizable(users, authorUser, expectedUser);
        result = setUpQueryBuilder(context, queries);
    }

    Page setUpPageExists(final Map<String, Object> properties) {
        return assertDoesNotThrow(() -> context.create().page(expectedPage, LOCAL_DUMMY_TEMPLATE, properties));
    }

    @Test
    void adaptTo_fromPageFindingNoChildren_returnsModelWithAuthorPojo() {
        final Page page = setUpPageExists(singletonMap(PN_PAGE_LAST_MOD_BY, expectedUser));

        final AlsoByAuthorModel underTest = page.getContentResource().adaptTo(AlsoByAuthorModel.class);

        assertNotNull(underTest);
        final NamePojo author = underTest.getAuthor();
        assertNotNull(author);
        assertThat(author.getFirstName(), is(expectedFirstName));
        assertThat(author.getLastName(), is(expectedLastName));
        final List<LinkPojo> alsoBy = underTest.getAlsoBy();
        assertNotNull(alsoBy);
        assertThat(alsoBy, emptyCollectionOf(LinkPojo.class));
    }

    @Test
    void adaptTo_fromPageFindingChildren_returnsModelWithAuthorAndLinkPojos() {
        final Page page = setUpPageExists(singletonMap(PN_PAGE_LAST_MOD_BY, expectedUser));
        setUpSomeHits(result, expectedPage, 2);

        final AlsoByAuthorModel underTest = page.getContentResource().adaptTo(AlsoByAuthorModel.class);

        assertNotNull(underTest);
        assertThat(underTest.getAuthor().getFirstName(), is(expectedFirstName));
        final List<LinkPojo> alsoBy = underTest.getAlsoBy();
        assertThat(alsoBy, hasSize(2));
        final LinkPojo first = alsoBy.get(0);
        assertThat(first.getTitle(), is("0"));
        assertThat(first.getRelativePath(), is("hit/0"));
    }
}
