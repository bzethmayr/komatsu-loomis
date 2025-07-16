package com.bzethmayr.komatsu.test.core.servlets;

import com.bzethmayr.komatsu.test.core.models.ExerciseThreePagePojo;
import com.day.cq.search.Predicate;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.service.component.annotations.Component;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(service = Servlet.class)
@SlingServletPaths(
        value = "/exercisethree.json"
)
public class ExerciseThreeServlet extends SlingSafeMethodsServlet {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(
            final SlingHttpServletRequest request,
            final SlingHttpServletResponse response
            ) throws IOException {
        final String searchText = request.getParameter("searchText");

        if (searchText == null || searchText.isBlank()) {
            response.setStatus(400);
            return;
        }

        final List<ExerciseThreePagePojo> results = lookUpResults(request.getResourceResolver(), searchText);

        mapper.writeValue(response.getWriter(), results);
    }

    private List<ExerciseThreePagePojo> lookUpResults(final ResourceResolver resolver, final String searchText) {
        final PredicateGroup top = new PredicateGroup();
        final Predicate isPage = new Predicate("type");
        isPage.set("type", "cq:Page");
        top.add(isPage);
        final PredicateGroup titleOrDescription = new PredicateGroup();
        titleOrDescription.setAllRequired(false);
        final Predicate titleContaining = new Predicate("property");
        titleContaining.set("property", "jcr:content/jcr:title");
        titleContaining.set("operation", "contains");
        titleContaining.set("value", searchText);
        titleOrDescription.add(titleContaining);
        final Predicate descriptionContaining = new Predicate("property");
        descriptionContaining.set("property", "jcr:content/jcr:description");
        descriptionContaining.set("operation", "contains");
        descriptionContaining.set("value", searchText);
        titleOrDescription.add(descriptionContaining);

        final QueryBuilder queries = resolver.adaptTo(QueryBuilder.class);
        final Query query = queries.createQuery(top, resolver.adaptTo(Session.class));
        query.setHitsPerPage(-1); // it did say all. however, pagination could be a better idea.
        final SearchResult result = query.getResult();

        final PageManager pages = resolver.adaptTo(PageManager.class);
        return result.getHits().stream()
                .map(h -> pagePojoFromHit(h, pages))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private ExerciseThreePagePojo pagePojoFromHit(final Hit hit, final PageManager pages) {
        try {
            final Page page = pages.getPage(hit.getPath());
            if (page != null) {
                return new ExerciseThreePagePojo(
                        page.getTitle(),
                        page.getDescription(),
                        "",
                        Optional.ofNullable(page.getLastModified())
                                .map(Calendar::getTime)
                                .orElse(null)
                );
            }
        } catch (final RepositoryException re) {
            throw new RuntimeException(re);
        }
        return null;
    }
}
