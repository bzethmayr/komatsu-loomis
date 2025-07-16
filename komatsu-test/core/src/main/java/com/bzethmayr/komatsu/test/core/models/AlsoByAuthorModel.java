package com.bzethmayr.komatsu.test.core.models;

import com.day.cq.search.Predicate;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

import javax.annotation.PostConstruct;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.bzethmayr.komatsu.test.core.util.ProfileValueHelper.firstStringOrBlank;

@Model(adaptables = Resource.class)
public class AlsoByAuthorModel {

    public static final String FIRST_NAME_PATH = "profile/givenName";
    public static final String LAST_NAME_PATH = "profile/lastName";

    @SlingObject
    private Resource currentResource;

    @SlingObject
    private ResourceResolver resourceResolver;

    private NamePojo author;
    private List<LinkPojo> alsoBy;

    @PostConstruct
    protected void init() throws RepositoryException {
        // path is independently needed
        final PageManager pages = resourceResolver.adaptTo(PageManager.class);
        final Page containingPage = Optional.ofNullable(pages)
                .map(m -> m.getContainingPage(currentResource))
                .orElse(null);

        if (containingPage == null) return;

        final String pagePath = containingPage.getPath();

        final String authorUsername = containingPage.getLastModifiedBy();
        final UserManager users = resourceResolver.adaptTo(UserManager.class);
        final User authorUser = (User) Optional.ofNullable(users.getAuthorizable(authorUsername))
                .filter(a -> !a.isGroup())
                .orElse(null);


        if (authorUser != null) {
            author = new NamePojo(
                   firstStringOrBlank(authorUser, FIRST_NAME_PATH),
                   firstStringOrBlank(authorUser, LAST_NAME_PATH)
            );
            alsoBy = Collections.unmodifiableList(findAlsoBy(resourceResolver, authorUsername, pagePath));
        }
    }

    private List<LinkPojo> findAlsoBy(final ResourceResolver resolver, final String authorUserName, final String pagePath) {
        final PredicateGroup sole = new PredicateGroup();
        final Predicate isChild = new Predicate("path");
        isChild.set("path", pagePath);
        sole.add(isChild);
        final Predicate isPage = new Predicate("type");
        isPage.set("type", "cq:Page");
        sole.add(isPage);
        final Predicate wasModifiedBy = new Predicate("property");
        wasModifiedBy.set("property", "jcr:content/cq:lastModifiedBy");
        wasModifiedBy.set("value", authorUserName);
        sole.add(wasModifiedBy);

        final QueryBuilder queries = resolver.adaptTo(QueryBuilder.class);
        final Query query = queries.createQuery(sole, resourceResolver.adaptTo(Session.class));
        query.setHitsPerPage(10);
        final SearchResult result = query.getResult();

        return result.getHits().stream()
                .map(h -> pojoFromHit(h, pagePath))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private LinkPojo pojoFromHit(final Hit hit, final String prefixPath) {
        try {
            return new LinkPojo(
                    Optional.ofNullable(hit.getTitle())
                            .orElse(""),
                    Optional.ofNullable(hit.getPath())
                            .map(s -> s.substring(prefixPath.length() +
                                    (prefixPath.endsWith("/") ? 0 : 1)))
                            .orElse("")
            );
        } catch (final RepositoryException e) {
            return null;
        }
    }

    public NamePojo getAuthor() {
        return author;
    }

    public List<LinkPojo> getAlsoBy() {
        return alsoBy;
    }
}
