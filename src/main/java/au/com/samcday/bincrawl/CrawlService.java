package au.com.samcday.bincrawl;

import au.com.samcday.bincrawl.pool.NntpClientPool;
import au.com.samcday.bincrawl.util.BlockingExecutorService;
import au.com.samcday.jnntp.GroupInfo;
import au.com.samcday.jnntp.NntpClient;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This Service is responsible for keeping a pool of crawling threads busy for the lifetime of the application.
 */
public class CrawlService extends AbstractExecutionThreadService {
    private BlockingExecutorService pool;
    private ListeningExecutorService listenablePool;
    private ConcurrentLinkedQueue<String> updateGroups;
    private ConcurrentLinkedQueue<String> backfillGroups;
    private Map<String, GroupInfo> groups;
    private final Lock workLock = new ReentrantLock(true);
    private final Condition workAvailable = workLock.newCondition();
    private NntpClientPool nntpClientPool;

    @Inject
    public CrawlService(NntpClientPool nntpClientPool) {
        this.nntpClientPool = nntpClientPool;
    }

    @Override
    protected void startUp() throws Exception {
        this.pool = new BlockingExecutorService(20);
        this.listenablePool = MoreExecutors.listeningDecorator(this.pool);
    }

    @Override
    protected void run() throws Exception {
        while(this.isRunning()) {
            Runnable r = this.getWork();
            this.pool.execute(r);
        }
    }

    @Override
    protected void triggerShutdown() {
        this.pool.shutdown();
        this.workLock.lock();
        this.workAvailable.signal();
        this.workLock.unlock();
    }

    private Runnable getWork() {
        this.workLock.lock();
        try {
            String group = this.updateGroups.poll();
            if(group != null) {
                // Create job to crawl some posts for this group.
                return null;
            }

            group = this.backfillGroups.poll();
            if(group != null) {
                // Create job to crawl some backfill posts for this group.
                return null;
            }

            // No work. Let's wait around until something happens. This might be either:
            //  a) a job comes back and re-inserts a group back into update/backfill queues.
            //  b) the update() finishes executing, resulting in new work.
            // We loop here because of "spurious" wakeups that can occur from waiting on a Condition.
            while(this.updateGroups.isEmpty() && this.backfillGroups.isEmpty() && this.isRunning()) {
                // awaitUninterruptibly here because we don't want arbitrary interrupts fucking with our main update
                // thread. If we're legitimately shutting down hen our triggerShutdown() will be signalling this
                // Condition anyway.
                this.workAvailable.awaitUninterruptibly();
            }

            return null;
        }
        finally {
            this.workLock.unlock();
        }
    }

    private class UpdateTask implements Runnable {
        @Override
        public void run() {
            NntpClient client = nntpClientPool.borrow();

            try {

            }
            finally {
                nntpClientPool.release(client);
            }
        }
    }
}
