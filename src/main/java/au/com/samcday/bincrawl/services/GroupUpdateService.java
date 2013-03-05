package au.com.samcday.bincrawl.services;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.concurrent.TimeUnit;

/**
 * Periodically runs an update on the CrawlService group data.
 */
@Singleton
public class GroupUpdateService extends AbstractScheduledService {
    private CrawlService crawlService;

    @Inject
    public GroupUpdateService(CrawlService crawlService) {
        this.crawlService = crawlService;
    }

    @Override
    protected void runOneIteration() throws Exception {
        this.crawlService.update();
    }

    @Override
    protected Scheduler scheduler() {
        // TODO: configurable?
        return Scheduler.newFixedDelaySchedule(15, 15, TimeUnit.MINUTES);
    }
}
