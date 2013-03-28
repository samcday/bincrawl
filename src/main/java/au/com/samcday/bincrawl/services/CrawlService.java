package au.com.samcday.bincrawl.services;

import au.com.samcday.bincrawl.Group;
import au.com.samcday.bincrawl.NntpWorkPool;
import au.com.samcday.bincrawl.RedisKeys;
import au.com.samcday.bincrawl.pool.BetterJedisPool;
import au.com.samcday.bincrawl.pool.PooledJedis;
import au.com.samcday.bincrawl.tasks.ArticleBackfillTask;
import au.com.samcday.bincrawl.tasks.ArticleUpdateTask;
import au.com.samcday.bincrawl.tasks.GroupInfoTask;
import au.com.samcday.bincrawl.util.AutoLockable;
import au.com.samcday.jnntp.NntpClient;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This Service is responsible for keeping a pool of crawling threads busy for the lifetime of the application.
 */
@Singleton
public class CrawlService extends AbstractExecutionThreadService {
    private static final Logger LOG = LoggerFactory.getLogger(CrawlService.class);

    private ExecutorService listenerExecutor;
    private ConcurrentLinkedQueue<String> updateGroups;
    private ConcurrentLinkedQueue<String> backfillGroups;
    private Map<String, Group> groups;
    private final Lock workLock = new ReentrantLock(true);
    private final Condition workAvailable = workLock.newCondition();
    private BetterJedisPool redisPool;
    private Provider<ArticleUpdateTask> updateTaskProvider;
    private Provider<ArticleBackfillTask> backfillTaskProvider;
    private Provider<GroupInfoTask> groupInfoTaskProvider;
    private NntpWorkPool nntpWorkPool;

    @Inject
    public CrawlService(BetterJedisPool redisPool, Provider<ArticleUpdateTask> updateTaskProvider,
                        Provider<ArticleBackfillTask> backfillTaskProvider, Provider<GroupInfoTask> groupInfoTaskProvider, NntpWorkPool nntpWorkPool) {
        this.updateTaskProvider = updateTaskProvider;
        this.redisPool = redisPool;
        this.backfillTaskProvider = backfillTaskProvider;
        this.groupInfoTaskProvider = groupInfoTaskProvider;
        this.nntpWorkPool = nntpWorkPool;
    }

    @Override
    protected void startUp() throws Exception {
        this.listenerExecutor = Executors.newSingleThreadExecutor();
        this.updateGroups = new ConcurrentLinkedQueue<>();
        this.backfillGroups = new ConcurrentLinkedQueue<>();
        this.groups = new HashMap<>();
    }

    @Override
    protected void run() throws Exception {
        this.update();

        while(this.isRunning()) {
            this.doWork();
        }
    }

    @Override
    protected void triggerShutdown() {
        this.listenerExecutor.shutdown();
        this.workLock.lock();
        this.workAvailable.signal();
        this.workLock.unlock();
    }

    private void doWork() {
        try (AutoLockable ignored = AutoLockable.lock(this.workLock))  {
            boolean doingSomething = false;

            final String updateGroup = this.updateGroups.poll();
            if(updateGroup != null) {
                // Create job to crawl some posts for this group.
                ArticleUpdateTask task = updateTaskProvider.get();
                task.configure(updateGroup, groups.get(updateGroup));
                Futures.addCallback(this.nntpWorkPool.submit(task), new FutureCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        try(AutoLockable ignored = AutoLockable.lock(workLock)) {
                            updateGroups.offer(updateGroup);
                            workAvailable.signal();
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        LOG.warn("Error updating {}.", updateGroup, t);
                    }
                }, this.listenerExecutor);
                doingSomething = true;
            }

            final String backfillGroup = this.backfillGroups.poll();
            if(backfillGroup != null) {
                ArticleBackfillTask task = backfillTaskProvider.get();
                task.configure(updateGroup, groups.get(updateGroup));
                Futures.addCallback(this.nntpWorkPool.submit(task), new FutureCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        try(AutoLockable ignored = AutoLockable.lock(workLock)) {
                            backfillGroups.offer(updateGroup);
                            workAvailable.signal();
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        LOG.warn("Error backfilling {}.", updateGroup, t);
                    }
                }, this.listenerExecutor);
                doingSomething = true;
            }

            if(doingSomething) return;

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
        }
    }

    public void update() {
        LOG.info("Updating group info.");

        try(AutoLockable ignored = AutoLockable.lock(this.workLock); PooledJedis redisClient = this.redisPool.get()) {
            // So I'm pretty much mostly sure this won't deadlock. The workLock gets picked up by listeners when work
            // pool stuff completes, but those listeners execute in a different thread, so the original worker that
            // finished something would already have been returned to the pool.
            this.nntpWorkPool.waitForAll();

            Set<String> groups = redisClient.smembers(RedisKeys.groups);
            List<ListenableFuture<Group>> futures = new ArrayList<>();
            for(String group : groups) {
                futures.add(this.nntpWorkPool.submit(this.groupInfoTaskProvider.get().configure(group)));
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

    public void addNewGroup(final String groupName) {
        // Note we don't acquire workLock initially, we first go into work pool and get the group info we need.
        // We block the calling thread until the info comes back. Then we lock. Mostly just a performance thing I guess
        Future<Group> future = this.nntpWorkPool.submit(new NntpWorkPool.NntpCallable<Group>() {
            @Override
            public Group call(NntpClient client) throws Exception {
                LOG.info("Getting group info for {}", groupName);
                return Group.of(groupName, client.group(groupName));
            }
        });

        Group group = Futures.getUnchecked(future);
        try(AutoLockable ignored = AutoLockable.lock(this.workLock); PooledJedis redisClient = this.redisPool.get()) {
            assert !groups.containsKey(groupName);
            groups.put(groupName, group);
            redisClient.hsetnx(RedisKeys.group(groupName), RedisKeys.groupEnd, Long.toString(group.high));
            redisClient.hsetnx(RedisKeys.group(groupName), RedisKeys.groupStart, Long.toString(group.high));
            redisClient.publish("groupupdates", groupName);

            boolean backfill = redisClient.hget(RedisKeys.group(groupName), RedisKeys.groupMaxAge) != null;
            if(backfill) this.backfillGroups.add(groupName);
            this.updateGroups.add(groupName);

            this.workAvailable.signal();
        }
    }
}
