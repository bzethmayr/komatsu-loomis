package com.bzethmayr.komatsu.test.core.schedulers;

import org.apache.sling.commons.scheduler.Scheduler;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.service.component.annotations.*;

import java.util.Optional;

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

    public PublishedPageProcessingTask(
            final SlingSettingsService settings,
            final Scheduler scheduler
    ) {
        this.settings = settings;
        this.scheduler = scheduler;
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
