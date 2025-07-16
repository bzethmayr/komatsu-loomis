package com.bzethmayr.komatsu.test.core.servlets;

import com.bzethmayr.komatsu.test.core.testcontext.AppAemContext;
import com.day.cq.search.QueryBuilder;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.bzethmayr.komatsu.test.core.testcontext.LocalTestingConstants.EXPECTED;
import static com.bzethmayr.komatsu.test.core.testcontext.LocalTestingConstants.LOCAL_DUMMY_TEMPLATE;
import static com.bzethmayr.komatsu.test.core.util.ProfileValueTestHelper.*;
import static com.bzethmayr.komatsu.test.core.util.QueryBuilderTestHelper.setUpQueryBuilder;
import static com.day.cq.wcm.api.NameConstants.PN_PAGE_LAST_MOD_BY;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(AemContextExtension.class)
class AlsoByAuthorServletTest {

    private final AemContext context = AppAemContext.newAemContext();
    private final String expectedPage = "/content/" + EXPECTED;
    private final String expectedAuthor = "expectedUser";
    private final String expectedFirstName = "first";
    private final String expectedLastName = "last";
    private final UserManager users = mock(UserManager.class);
    private final User author = mock(User.class);
    private final QueryBuilder queries = mock(QueryBuilder.class);

    private AlsoByAuthorServlet underTest = new AlsoByAuthorServlet();

    @BeforeEach
    void setUpUsers() {
        setUpUserManager(context, users);
        setUpFirstAndLastNames(author, expectedFirstName, expectedLastName);
        setUpQueryBuilder(context, queries);
    }

    private void setUpPageExists() {
        context.create().page(expectedPage, LOCAL_DUMMY_TEMPLATE, singletonMap(
                PN_PAGE_LAST_MOD_BY, expectedAuthor));
    }

    @Test
    void doGet_whenNoAuthor_returnsNotFound() {
        setUpPageExists();
        context.currentResource(expectedPage);
        context.requestPathInfo().setExtension("json");
        MockSlingHttpServletRequest request = context.request();
        MockSlingHttpServletResponse response = context.response();

        assertDoesNotThrow(() -> underTest.doGet(request, response));

        assertThat(response.getStatus(), is(404));
    }

    @Test
    void doGet_whenPageForJson_returnsJson() {
        setUpPageExists();
        setUpGetAuthorizable(users, author, expectedAuthor);
        context.currentResource(expectedPage);
        context.requestPathInfo().setExtension("json");
        MockSlingHttpServletRequest request = context.request();
        MockSlingHttpServletResponse response = context.response();

        assertDoesNotThrow(() -> underTest.doGet(request, response));

        final String result = response.getOutputAsString();
        assertThat(result, allOf(
                startsWith("{"),
                containsString("first"),
                containsString("last"),
                endsWith("}")
        ));
        assertThat(response.getStatus(), is(200));
        assertThat(response.getContentType(), containsString("json"));
    }

    @Test
    void doGet_whenPageForXml_returnsXml() {
        setUpPageExists();
        setUpGetAuthorizable(users, author, expectedAuthor);
        context.currentResource(expectedPage);
        context.requestPathInfo().setExtension("xml");
        MockSlingHttpServletRequest request = context.request();
        MockSlingHttpServletResponse response = context.response();

        assertDoesNotThrow(() -> underTest.doGet(request, response));

        final String result = response.getOutputAsString();
        assertThat(result, allOf(
                startsWith("<"),
                containsString("first"),
                containsString("last"),
                endsWith(">")
        ));
        assertThat(response.getStatus(), is(200));
        assertThat(response.getContentType(), containsString("xml"));
    }

    @Test
    void doGet_whenNoExtension_returnsBadRequest() {
        setUpPageExists();
        setUpGetAuthorizable(users, author, expectedAuthor);
        context.currentResource(expectedPage);
        MockSlingHttpServletRequest request = context.request();
        MockSlingHttpServletResponse response = context.response();

        assertDoesNotThrow(() -> underTest.doGet(request, response));

        assertThat(response.getStatus(), is(400));
    }

}
