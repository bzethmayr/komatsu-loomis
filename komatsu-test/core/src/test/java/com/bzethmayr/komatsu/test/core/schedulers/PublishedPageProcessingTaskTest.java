package com.bzethmayr.komatsu.test.core.schedulers;

import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;
import org.apache.sling.settings.SlingSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.bzethmayr.komatsu.test.core.schedulers.PublishedPageProcessingTask.*;
import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(AemContextExtension.class)
public class PublishedPageProcessingTaskTest {

    private SlingSettingsService settings;
    private Scheduler scheduler;
    private PublishedPageProcessingTask underTest;

    @BeforeEach
    void setUpUnderTest() {
        settings = mock(SlingSettingsService.class);
        scheduler = mock(Scheduler.class);
        underTest = new PublishedPageProcessingTask(settings, scheduler);
    }

    @Test
    void onAuthor_givenNullReferences_returnsFalse() {
        underTest = new PublishedPageProcessingTask(null, null);

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
        underTest = new PublishedPageProcessingTask(null, null);

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
        underTest = new PublishedPageProcessingTask(null, null);

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
}
