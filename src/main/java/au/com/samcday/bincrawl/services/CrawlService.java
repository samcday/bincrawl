package au.com.samcday.bincrawl.services;

import au.com.samcday.bincrawl.Group;
import au.com.samcday.bincrawl.RedisKeys;
import au.com.samcday.bincrawl.pool.BetterJedisPool;
import au.com.samcday.bincrawl.pool.PooledJedis;
import au.com.samcday.bincrawl.tasks.ArticleUpdateTask;
import au.com.samcday.bincrawl.tasks.GroupInfoTask;
import au.com.samcday.bincrawl.util.AutoLockable;
import au.com.samcday.bincrawl.util.BlockingExecutorService;
import com.google.common.util.concurrent.*;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This Service is responsible for keeping a pool of crawling threads busy for the lifetime of the application.
 */
@Singleton
public class CrawlService extends AbstractExecutionThreadService {
    private static final Logger LOG = LoggerFactory.getLogger(CrawlService.class);

    private BlockingExecutorService pool;
    private ListeningExecutorService listenablePool;
    private ConcurrentLinkedQueue<String> updateGroups;
    private ConcurrentLinkedQueue<String> backfillGroups;
    private Map<String, Group> groups;
    private final Lock workLock = new ReentrantLock(true);
    private final Condition workAvailable = workLock.newCondition();
    private BetterJedisPool redisPool;
    private Provider<ArticleUpdateTask> updateTaskProvider;
    private Provider<GroupInfoTask> groupInfoTaskProvider;

    @Inject
    public CrawlService(BetterJedisPool redisPool, Provider<ArticleUpdateTask> updateTaskProvider,
            Provider<GroupInfoTask> groupInfoTaskProvider) {
        this.updateTaskProvider = updateTaskProvider;
        this.redisPool = redisPool;
        this.groupInfoTaskProvider = groupInfoTaskProvider;
    }

    @Override
    protected void startUp() throws Exception {
        this.updateGroups = new ConcurrentLinkedQueue<>();
        this.backfillGroups = new ConcurrentLinkedQueue<>();
        this.groups = new HashMap<>();
        this.pool = new BlockingExecutorService(20);
        this.listenablePool = MoreExecutors.listeningDecorator(this.pool);
    }

    @Override
    protected void run() throws Exception {
        this.update();

        while(this.isRunning()) {
            Runnable r = this.getWork();
            if(r != null) {
                this.pool.execute(r);
            }
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
                LOG.trace("I guess we could crawl {}?", group);
                return new UpdateTaskWrapper(group);
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
            LOG.info("No work for me. Hibernating until update() or completed work gives me something to work with.");
            while(this.updateGroups.isEmpty() && this.backfillGroups.isEmpty() && this.isRunning()) {
                // awaitUninterruptibly here because we don't want arbitrary interrupts fucking with our main update
                // thread. If we're legitimately shutting down then our triggerShutdown() will be signalling this
                // Condition anyway.
                this.workAvailable.awaitUninterruptibly();
            }

            return null;
        }
        finally {
            this.workLock.unlock();
        }
    }

    public void update() {
        LOG.info("Updating group info.");

        try(AutoLockable ignored = AutoLockable.lock(this.workLock); PooledJedis redisClient = this.redisPool.get()) {
            this.pool.waitForAll();


            Set<String> groups = redisClient.smembers(RedisKeys.groups);
            List<ListenableFuture<Group>> futures = new ArrayList<>();
            for(String group : groups) {
                futures.add(this.listenablePool.submit(this.groupInfoTaskProvider.get().configure(group)));
            }

            List<Group> groupData = Futures.allAsList(futures).get();
            this.groups.clear();
            this.updateGroups.clear();
            this.backfillGroups.clear();

            for(Group group : groupData) {
                this.groups.put(group.name, group);
                this.backfillGroups.offer(group.name);
                this.updateGroups.offer(group.name);
            }
        }
        catch(InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        catch(ExecutionException ee) {
            LOG.warn("Couldn't update group info", ee);
        }
    }

    private class UpdateTaskWrapper implements Runnable {
        private ArticleUpdateTask task;
        private String group;

        public UpdateTaskWrapper(String group) {
            this.group = group;
            this.task = updateTaskProvider.get();
            this.task.configure(group, groups.get(group));
        }

        @Override
        public void run() {
            try {
                if(this.task.call()) {
                    try(AutoLockable ignored = AutoLockable.lock(workLock)) {
                        updateGroups.offer(this.group);
                        workAvailable.signal();
                    }
                }
            }
            catch(Exception e) {
                LOG.error("Unhandled Exception during update task.", e);
            }
        }
    }

}
