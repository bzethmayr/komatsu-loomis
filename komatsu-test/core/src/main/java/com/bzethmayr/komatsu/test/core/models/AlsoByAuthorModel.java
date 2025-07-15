package com.bzethmayr.komatsu.test.core.models;

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
import java.util.List;
import java.util.Optional;

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
        final PageManager pageManager = resourceResolver.adaptTo(PageManager.class);

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
        }
    }

    public NamePojo getAuthor() {
        return author;
    }

    public List<LinkPojo> getAlsoBy() {
        return alsoBy;
    }
}
