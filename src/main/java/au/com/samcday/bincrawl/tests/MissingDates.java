package au.com.samcday.bincrawl.tests;

import au.com.samcday.bincrawl.AppModule;
import au.com.samcday.bincrawl.pool.NntpClientPool;
import au.com.samcday.bincrawl.pool.PooledNntpClient;
import au.com.samcday.jnntp.GroupInfo;
import au.com.samcday.jnntp.Overview;
import au.com.samcday.jnntp.OverviewList;
import com.google.common.util.concurrent.RateLimiter;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.reporting.ConsoleReporter;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class MissingDates {
    public static final void main(String... args) throws Exception {
        Injector injector = Guice.createInjector(new AppModule());

        final NntpClientPool pool = injector.getInstance(NntpClientPool.class);

        final int numWorkers = 4;
        final int numToCrawl = 50000000; // 50 million.
        final int workerToCrawl = numToCrawl / numWorkers;
        final Meter crawlMeter = Metrics.newMeter(MissingDates.class, "crawl", "crawl", TimeUnit.SECONDS);

        ExecutorService service = Executors.newFixedThreadPool(numWorkers);

        final AtomicLong numMissingArticles = new AtomicLong();
        final AtomicLong numMissingDates = new AtomicLong();
        final AtomicReference<Date> lowestDate = new AtomicReference<>();
        final AtomicReference<Date> highestDate = new AtomicReference<>();

        long high;
        try(PooledNntpClient client = pool.borrow()) {
            GroupInfo inf = client.group("alt.binaries.hdtv");
            high = inf.high;
        }

        for (int i = 0; i < numWorkers; i++) {
            final long start = high - workerToCrawl;
            final long end = high;
            high -= workerToCrawl;
            service.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    try(PooledNntpClient client = pool.borrow()) {
                        GroupInfo inf = client.group("alt.binaries.hdtv");
                        long current = start;
                        while(current < end) {
                            long amount = Math.min(1000, end - current);
                            OverviewList list = client.overview(current, current + amount);
                            for(Overview item : list) {
                                crawlMeter.mark();

                                if(item.getDate() == null) {
                                    numMissingDates.incrementAndGet();
                                }
                                else {
                                    while(true) {
                                        Date currentLowestDate = lowestDate.get();
                                        if(currentLowestDate == null || item.getDate().compareTo(currentLowestDate) < 0) {
                                            if(lowestDate.compareAndSet(currentLowestDate, item.getDate())) {
                                                break;
                                            }
                                        }
                                        else {
                                            break;
                                        }
                                    }
                                    while(true) {
                                        Date currentHighestDate = highestDate.get();
                                        if(currentHighestDate == null || item.getDate().compareTo(currentHighestDate) > 0) {
                                            if(highestDate.compareAndSet(currentHighestDate, item.getDate())) {
                                                break;
                                            }
                                        }
                                        else {
                                            break;
                                        }
                                    }
                                }
                            }
                            numMissingArticles.getAndAdd(list.getMissingArticles().size());
                            current += amount;
                        }
                    }

                    return null;
                }
            });
        }

        ConsoleReporter.enable(5, TimeUnit.SECONDS);

        Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);

        RateLimiter reportRateLimiter = RateLimiter.create(1);
        service.shutdown();
        while(!service.isTerminated()) {
            reportRateLimiter.acquire(5);
            System.out.printf("%d missing articles. %d missing dates. Ranged from %s to %s\n", numMissingArticles.get(), numMissingDates.get(),
                lowestDate.get().toGMTString(), highestDate.get().toGMTString());
        }

        pool.close();
    }
}
