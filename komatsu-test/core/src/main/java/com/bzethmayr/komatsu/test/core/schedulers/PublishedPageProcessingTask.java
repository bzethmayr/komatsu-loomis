package com.bzethmayr.komatsu.test.core.schedulers;

import com.day.cq.search.Predicate;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import org.apache.sling.api.resource.*;
import org.apache.sling.commons.scheduler.Scheduler;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.service.component.annotations.*;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Date;
import java.util.Optional;

import static java.util.Collections.singletonMap;

@Component(
        service = PublishedPageProcessingTask.class,
        immediate = true
)
public class PublishedPageProcessingTask implements Runnable {
    static final String PPP_CRON_STRING = "*/2 * * * * ?";
    static final String PPP_REQUIRED_MODE = "author";
    private final Object scheduleLock = new Object();
    static final String PPP_TASK_NAME = "process-post-publish";

    private SlingSettingsService settings;
    private Scheduler scheduler;
    private boolean isScheduled;
    // state and related methods to here are early candidates for a superclass
    private ResourceResolverFactory resolvers;
    private QueryBuilder queries;

    public PublishedPageProcessingTask(
            final SlingSettingsService settings,
            final Scheduler scheduler,
            final ResourceResolverFactory resolvers,
          final QueryBuilder queries
    ) {
        this.settings = settings;
        this.scheduler = scheduler;
        this.resolvers = resolvers;
        this.queries = queries;
    }

    boolean onAuthor() {
        return Optional.ofNullable(settings)
                .map(SlingSettingsService::getRunModes)
                .filter(s -> s.contains(PPP_REQUIRED_MODE))
                .isPresent();
    }

    @Activate
    protected void activate() {
        if (scheduler != null) {
            if (onAuthor()) {
                scheduleOnto(scheduler);
            } else {
                clearExistingFrom(scheduler);
            }
        }
    }

    /**
     * Defines the scheduled job after all pre-checks
     * @param scheduler the scheduler, non-null
     */
    private void scheduleOnto(final Scheduler scheduler) {
        synchronized (scheduleLock) {
            clearExistingFrom(scheduler);
            if (!isScheduled) {
                scheduler.schedule(this, scheduler
                        .EXPR(PPP_CRON_STRING)
                        .canRunConcurrently(false)
                        .name(PPP_TASK_NAME)
                );
                isScheduled = true;
            }
        }
    }

    @Override
    public void run() {
        // we do not expect to be scheduled at all on publish, so if it happens we throw.
        // probably our overall run cost is much much greater than checking mode
        // throwing an exception costs a lot but it is worth it in order to know if our constraints don't work
        // that said, this check should be redundant and could eventually be removed.
        if (!onAuthor()) {
            throw new IllegalStateException("This job should not get scheduled here...");
        }

        // ... on with it
        // https://developer.adobe.com/experience-manager/reference-materials/6-5/javadoc/com/day/cq/search/QueryBuilder.html
        // and I need a resource resolver. fun.
        try (final ResourceResolver resolver = resolvers.getServiceResourceResolver(singletonMap(
                ResourceResolverFactory.SUBSERVICE, "post-publish"))) {
            final SearchResult result = obtainListingHandle(resolver);
            processAllResults(result, resolver);

        } catch (final LoginException authe) {
            throw new RuntimeException(authe);
        }
    }

    protected void processAllResults(final SearchResult result, final ResourceResolver resolver) {
        result.getHits().forEach(hit -> processOneResult(hit, resolver));
    }

    protected void processOneResult(final Hit hit, final ResourceResolver resolver) {
        try {
            final Resource content = hit.getResource().getChild("jcr:content");
            final ModifiableValueMap edits = content.adaptTo(ModifiableValueMap.class);
            if (edits == null) {
                throw new RuntimeException("Cannot adapt for editing");
            }
            edits.put("lastProcessed", new Date());
            resolver.commit();
        } catch (final RepositoryException | PersistenceException re) {
            throw new RuntimeException(re);
        }
    }

    protected SearchResult obtainListingHandle(final ResourceResolver resolver) {
        // cq:Page
        // visible under /content
        // cq:Page/jcr:content.cq:lastReplicated
        // > processedDate
        // or there is no such property as processedDate at all
        // better to place new property "processedDate" in jcr:content if we can, most likely
        final PredicateGroup top = new PredicateGroup();
        top.setAllRequired(false);
        final Predicate isContent = new Predicate("path");
        isContent.set("path", "jcr:root/content");
        final Predicate isPage = new Predicate("type");
        isPage.set("type", "cq:Page");
        final Predicate recent = new Predicate("dateComparison");
        recent.set("property1", "jcr:content/cq:lastReplicated");
        recent.set("property2", "jcr:content/lastProcessed");
        final PredicateGroup newlyPublished = new PredicateGroup();
        newlyPublished.add(isContent);
        newlyPublished.add(isPage);
        newlyPublished.add(recent);
        top.add(newlyPublished);
        final PredicateGroup neverProcessed = new PredicateGroup();
        neverProcessed.add(isContent);
        neverProcessed.add(isPage);
        final Predicate notProcessed = new Predicate("property");
        notProcessed.set("property", "jcr:content/lastProcessed");
        notProcessed.set("operation", "exists");
        notProcessed.set("value", "false");
        neverProcessed.add(notProcessed);
        top.add(neverProcessed);

        final Query query = queries.createQuery(top, resolver.adaptTo(Session.class));
        query.setHitsPerPage(-1);
        final SearchResult result = query.getResult();
        return result;
    }

    /**
     * Clears job definition after all pre-checks
     * @param scheduler the scheduler, non-null
     */
    private void clearExistingFrom(final Scheduler scheduler) {
        synchronized (scheduleLock) {
            if (isScheduled) {
                scheduler.unschedule(PPP_TASK_NAME);
                isScheduled = false;
            }
        }
    }

    @Deactivate
    protected void deactivate() {
        Optional.ofNullable(scheduler)
                .ifPresent(this::clearExistingFrom);
    }
}
