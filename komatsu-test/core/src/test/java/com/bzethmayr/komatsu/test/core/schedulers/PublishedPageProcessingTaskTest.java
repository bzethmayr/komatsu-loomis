package com.bzethmayr.komatsu.test.core.schedulers;

import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;
import org.apache.sling.settings.SlingSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static com.bzethmayr.komatsu.test.core.schedulers.PublishedPageProcessingTask.*;
import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Create a service that will run once every 2 minutes and only on the author environment.
 * This service will find all pages that have been published and will set a property named processedDate to the current time.
 */
@ExtendWith(AemContextExtension.class)
class PublishedPageProcessingTaskTest {

    private SlingSettingsService settings;
    private Scheduler scheduler;
    private PublishedPageProcessingTask underTest;
    private ResourceResolverFactory resolvers;
    private QueryBuilder queries;

    @BeforeEach
    void setUpUnderTest() {
        settings = mock(SlingSettingsService.class);
        scheduler = mock(Scheduler.class);
        resolvers = mock(ResourceResolverFactory.class);
        queries = mock(QueryBuilder.class);
        underTest = new PublishedPageProcessingTask(settings, scheduler, resolvers, queries);
    }

    void setUpNullReferences() {
        underTest = new PublishedPageProcessingTask(null, null, null, null);
    }

    @Test
    void onAuthor_givenNullReferences_returnsFalse() {
        setUpNullReferences();

        assertThat(underTest.onAuthor(), is(false));
    }

    void setUpIsPublish() {
        doReturn(singleton("publish")).when(settings).getRunModes();
    }

    @Test
    void onAuthor_whenOnPublish_alwaysReturnsFalse() {
        setUpIsPublish();

        assertThat(underTest.onAuthor(), is(false));

        underTest.activate();

        assertThat(underTest.onAuthor(), is(false));
    }

    void setUpIsAuthor() {
        doReturn(singleton(PPP_REQUIRED_MODE)).when(settings).getRunModes();
    }

    // brittle fake to verify scheduling calls - will null out on unexpected arguments
    void setUpForScheduling() {
        final ScheduleOptions fakeOptions = mock(ScheduleOptions.class);
        doReturn(fakeOptions).when(scheduler).EXPR(PPP_CRON_STRING);
        doReturn(fakeOptions).when(fakeOptions).canRunConcurrently(false);
        doReturn(fakeOptions).when(fakeOptions).name(PPP_TASK_NAME);
    }

    @Test
    void onAuthor_whenOnAuthor_alwaysReturnsTrue() {
        setUpIsAuthor();
        setUpForScheduling();

        assertThat(underTest.onAuthor(), is(true));

        underTest.activate();

        assertThat(underTest.onAuthor(), is(true));
    }

    @Test
    void activate_givenNullReferences_doesNothing() {
        setUpNullReferences();

        underTest.activate();

        verifyNoInteractions(settings, scheduler);
    }

    @Test
    void activate_givenSchedulerExists_whenOnAuthor_schedulesTask() {
        setUpIsAuthor();
        setUpForScheduling();

        underTest.activate();

        verify(scheduler).schedule(eq(underTest), any(ScheduleOptions.class));
    }

    @Test
    void activate_givenSchedulerExists_whenNotAuthor_doesNothing() {
        setUpIsPublish();

        underTest.activate();

        verifyNoInteractions(scheduler);
    }

    @Test
    void deactivate_givenNullReferences_doesNothing() {
        setUpNullReferences();

        underTest.deactivate();

        verifyNoInteractions(settings, scheduler);
    }

    @Test
    void activateThenDeactivate_whenOnPublish_doesNothing() {

        underTest.activate();
        underTest.deactivate();

        verifyNoInteractions(scheduler);
    }

    @Test
    void activateThenDeactivate_whenOnAuthor_schedulesThenUnschedules() {
        setUpIsAuthor();
        setUpForScheduling();

        underTest.activate();
        underTest.deactivate();

        verify(scheduler).schedule(eq(underTest), any(ScheduleOptions.class));
        verify(scheduler).unschedule(PPP_TASK_NAME);
    }


    @Test
    void activateThenDeactivateThenActivate_whenOnAuthor_schedulesThenUnschedulesThenSchedules() {
        setUpIsAuthor();
        setUpForScheduling();

        underTest.activate();
        underTest.deactivate();
        underTest.activate();

        verify(scheduler, times(2)).schedule(eq(underTest), any(ScheduleOptions.class));
        verify(scheduler).unschedule(PPP_TASK_NAME);
    }

    @Test
    void run_whenNotAuthor_throwsIllegalStateException() {
        assertThrows(IllegalStateException.class, () ->
                underTest.run());
    }

    final ResourceResolver setUpOpensResolver() {
        final ResourceResolver resolver = mock(ResourceResolver.class);
        assertDoesNotThrow(() ->
                doReturn(resolver).when(resolvers).getServiceResourceResolver(anyMap()));
        return resolver;
    }

    final Query setUpCreatesQuery() {
        final Query query = mock(Query.class);
        doReturn(query).when(queries).createQuery(any(), any());
        return query;
    }

    final void setUpAdaptableResults(final Query query, final int count) {
        final SearchResult result = mock(SearchResult.class);
        final List<Hit> hits = new ArrayList<>(count);
        IntStream.range(0, count).forEach(x -> {
            final ModifiableValueMap values = mock(ModifiableValueMap.class);
            final Resource contentResource = mock(Resource.class);
            final Resource hitResource = mock(Resource.class);
            final Hit hit = mock(Hit.class);
            assertDoesNotThrow(() ->
                    doReturn(hitResource).when(hit).getResource());
            doReturn(contentResource).when(hitResource).getChild("jcr:content");
            doReturn(values).when(contentResource).adaptTo(ModifiableValueMap.class);
            hits.add(hit);
        });
        doReturn(hits).when(result).getHits();
        doReturn(result).when(query).getResult();
    }

    @Test
    void run_whenAuthor_givenSomeResults_updatesEachResult() {
        setUpIsAuthor();
        final ResourceResolver resolver = setUpOpensResolver();
        final Query query = setUpCreatesQuery();
        setUpAdaptableResults(query, 3);

        underTest.run();

        assertDoesNotThrow(() ->
                verify(resolver, times(3)).commit());
        verify(resolver).close();
    }
}
