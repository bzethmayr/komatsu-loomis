package com.bzethmayr.komatsu.test.core.util;

import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import io.wcm.testing.mock.aem.junit5.AemContext;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

import static com.bzethmayr.komatsu.test.core.testcontext.LocalTestingConstants.LOCAL_DUMMY_TEMPLATE;
import static com.day.cq.wcm.api.NameConstants.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public final class QueryBuilderTestHelper {
    private QueryBuilderTestHelper() {
        throw new UnsupportedOperationException();
    }

    public static SearchResult setUpQueryBuilder(final AemContext context, final QueryBuilder queries) {
        final Query fakeQuery = mock(Query.class);
        final SearchResult fakeResult = mock(SearchResult.class);

        context.registerAdapter(ResourceResolver.class, QueryBuilder.class,
                (Function<ResourceResolver, QueryBuilder>) x -> queries);
        doReturn(fakeQuery).when(queries).createQuery(any(), any());
        doReturn(fakeResult).when(fakeQuery).getResult();
        return fakeResult;
    }

    private static String hitPath(final String root, final int index) {
        return root + "/hit/" + index;
    }

    public static void setUpSomeHits(final SearchResult result, final String root, final int count) {
        final List<Hit> hits = new ArrayList<>(count);
        IntStream.range(0, count).forEach(x -> {
            final ModifiableValueMap values = mock(ModifiableValueMap.class);
            final Resource contentResource = mock(Resource.class);
            final Resource hitResource = mock(Resource.class);
            final Hit hit = mock(Hit.class);
            assertDoesNotThrow(() ->
                    doReturn(hitResource).when(hit).getResource());
            doReturn(contentResource).when(hitResource).getChild("jcr:content");
            assertDoesNotThrow(() ->
                    doReturn("" + x).when(hit).getTitle());
            assertDoesNotThrow(() ->
                    doReturn(hitPath(root, x)).when(hit).getPath());
            doReturn(values).when(contentResource).adaptTo(ModifiableValueMap.class);
            hits.add(hit);
        });
        doReturn(hits).when(result).getHits();
    }

    public static void setUpHitPages(final AemContext context, final String root, final int count) {
        IntStream.range(0, count).forEach(x -> {
            context.create().page(hitPath(root, x), LOCAL_DUMMY_TEMPLATE, Map.of(
                    PN_TITLE, "title " + x,
                    PN_DESCRIPTION, "description " + x,
                    PN_PAGE_LAST_MOD, Calendar.getInstance()
            ));
        });
    }
}
