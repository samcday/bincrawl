package au.com.samcday.bincrawl;

import au.com.samcday.bincrawl.pool.NntpClientPool;
import au.com.samcday.bincrawl.pool.PooledNntpClient;
import au.com.samcday.bincrawl.util.BlockingExecutorService;
import au.com.samcday.jnntp.NntpClient;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * All jobs that need to execute against an NNTP connection use this pool, so that connection limits can be enforced.
 */
@Singleton
public class NntpWorkPool extends BlockingExecutorService implements ListeningExecutorService {
    private int maxConnections;
    private ListeningExecutorService listenable;
    private NntpClientPool clientPool;

    @Inject
    public NntpWorkPool(NntpClientPool clientPool) {
        super(20 /* TODO: configurable */, new CrawlServiceThreadFactory());
        this.clientPool = clientPool;
        this.maxConnections = 20;
        this.listenable = MoreExecutors.listeningDecorator(this);
    }

    public ListenableFuture<?> submit(final NntpRunnable runnable) {
        return this.submit(new Runnable() {
            @Override
            public void run() {
                try(PooledNntpClient client = clientPool.borrow()) {
                    runnable.run(client);
                }
            }
        });
    }

    public <T> ListenableFuture<T> submit(final NntpCallable<T> callable) {
        return this.submit(new Callable<T>() {
            @Override
            public T call() throws Exception {
                try(PooledNntpClient client = clientPool.borrow()) {
                    return callable.call(client);
                }
            }
        });
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
        this.resize(maxConnections);
    }

    @Override
    public <T> ListenableFuture<T> submit(Callable<T> task) {
        return listenable.submit(task);
    }

    @Override
    public ListenableFuture<?> submit(Runnable task) {
        return listenable.submit(task);
    }

    @Override
    public <T> ListenableFuture<T> submit(Runnable task, T result) {
        return listenable.submit(task, result);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return listenable.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return listenable.invokeAll(tasks, timeout, unit);
    }

    private static final class CrawlServiceThreadFactory implements ThreadFactory {
        private ThreadGroup group;
        private AtomicInteger threadNumber = new AtomicInteger(0);

        private CrawlServiceThreadFactory() {
            this.group = Thread.currentThread().getThreadGroup();
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(this.group, r, "NntpWorkPool Worker #" + threadNumber.incrementAndGet(), 0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

    public static interface NntpRunnable {
        public void run(NntpClient client);
    }

    public static interface NntpCallable<T> {
        public T call(NntpClient client) throws Exception;
    }
}
