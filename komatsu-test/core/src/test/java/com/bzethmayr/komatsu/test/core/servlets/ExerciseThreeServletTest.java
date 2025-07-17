package com.bzethmayr.komatsu.test.core.servlets;

import com.bzethmayr.komatsu.test.core.testcontext.AppAemContext;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.bzethmayr.komatsu.test.core.testcontext.LocalTestingConstants.EXPECTED;
import static com.bzethmayr.komatsu.test.core.util.PageImageTestHelper.setUpImageAdapter;
import static com.bzethmayr.komatsu.test.core.util.PageImageTestHelper.setUpPageImage;
import static com.bzethmayr.komatsu.test.core.util.QueryBuilderTestHelper.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(AemContextExtension.class)
public class ExerciseThreeServletTest {

    private final AemContext context = AppAemContext.newAemContext();

    private final QueryBuilder queries = mock(QueryBuilder.class);

    private ExerciseThreeServlet underTest = new ExerciseThreeServlet();

    private final String expectedTitle = "title 0";
    private final String expectedDescription = "description 0";

    @Test
    void doGet_whenNoSearchText_returnsBadRequest() {
        MockSlingHttpServletRequest request = context.request();
        MockSlingHttpServletResponse response = context.response();

        assertDoesNotThrow(() -> underTest.doGet(request, response));

        assertThat(response.getStatus(), is(400));
    }

    @Test
    void doGet_whenSearchTextFindsNothing_returnsEmptyList() {
        setUpQueryBuilder(context, queries);
        context.request().addRequestParameter("searchText", EXPECTED);
        MockSlingHttpServletRequest request = context.request();
        MockSlingHttpServletResponse response = context.response();

        assertDoesNotThrow(() -> underTest.doGet(request, response));

        assertThat(response.getStatus(), is(200));
        assertThat(response.getOutputAsString(), is("[]"));
    }

    @Test
    void doGet_whenSearchTextFindsSomething_returnsPopulatedList() {
        final SearchResult result = setUpQueryBuilder(context, queries);
        setUpSomeHits(result, "/content", 1);
        setUpHitPages(context,"/content", 1);
        context.request().addRequestParameter("searchText", EXPECTED);
        MockSlingHttpServletRequest request = context.request();
        MockSlingHttpServletResponse response = context.response();

        assertDoesNotThrow(() -> underTest.doGet(request, response));

        assertThat(response.getStatus(), is(200));
        final String output = response.getOutputAsString();
        assertThat(output, allOf(
                startsWith("["),
                containsString(expectedTitle),
                containsString(expectedDescription),
                endsWith("]")
        ));
    }

    @Test
    void doGet_whenSearchTextFindsPageWithImage_returnsPopulatedList() {
        final SearchResult result = setUpQueryBuilder(context, queries);
        setUpSomeHits(result, "/content", 1);
        setUpHitPages(context,"/content", 1);
        setUpPageImage(context, "/content/hit/0");
        setUpImageAdapter(context);
        context.request().addRequestParameter("searchText", EXPECTED);
        MockSlingHttpServletRequest request = context.request();
        MockSlingHttpServletResponse response = context.response();

        assertDoesNotThrow(() -> underTest.doGet(request, response));

        assertThat(response.getStatus(), is(200));
        final String output = response.getOutputAsString();
        assertThat(output, allOf(
                startsWith("["),
                containsString("thumbnail"),
                containsString(expectedTitle),
                containsString(expectedDescription),
                endsWith("]")
        ));
    }

}
