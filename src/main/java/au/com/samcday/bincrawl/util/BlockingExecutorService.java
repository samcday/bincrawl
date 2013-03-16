package au.com.samcday.bincrawl.util;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ForwardingExecutorService;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This implementation of ExecutorService will execute tasks on a ThreadPoolExecutorService, but will block requests to
 * execute work when all threads are busy. This differs from the standard ThreadPoolExecutorService, which will simply
 * throw RejectedExecutionExceptions in most RejectedExecutionHandler configurations, or will force the work to be
 * executed on the calling thread (in the case of RejectedExecutionHandler.CallerRunsPolicy).
 */
public class BlockingExecutorService extends ForwardingExecutorService {
    private ThreadPoolExecutor executor;
    private ReducibleSemaphore semaphore;
    private int numPermits;
    private AtomicInteger inProgress;
    private final Lock lock = new ReentrantLock();
    private final Condition done = lock.newCondition();
    private boolean isDone = false;

    public BlockingExecutorService(int size) {
        this(size, Executors.defaultThreadFactory());
    }

    public BlockingExecutorService(int size, ThreadFactory threadFactory) {
        this.numPermits = size;
        this.semaphore = new ReducibleSemaphore(size);
        this.inProgress = new AtomicInteger();
        this.executor = new ThreadPoolExecutor(size, size, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(),
                threadFactory) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                if(inProgress.decrementAndGet() == 0) {
                    lock.lock();
                    try {
                        isDone = true;
                        done.signalAll();
                    }
                    finally {
                        lock.unlock();
                    }
                }
                semaphore.release();
            }
        };
    }

    @Override
    protected ExecutorService delegate() {
        return this.executor;
    }

    @Override
    public void execute(final Runnable command) {
        try {
            this.semaphore.acquire();
            this.inProgress.incrementAndGet();
        }
        catch(InterruptedException ie) {
            Thread.currentThread().interrupt();
            return;
        }

        try {
            this.executor.execute(command);
        }
        catch(Exception e) {
            this.inProgress.decrementAndGet();
            this.semaphore.release();
            throw Throwables.propagate(e);
        }
    }

    /**
     * This method resizes the thread pool backing this BlockingExecutorService, it can increase or decrease the size.
     * @param newSize
     */
    public void resize(int newSize) {
        int oldSize = this.numPermits;
        this.numPermits = newSize;

        // Note the order in which we reduce/increase semaphore size and then readjust pool size. Think about it a sec
        // before you try and change it.
        if(oldSize > newSize) {
            this.semaphore.reducePermits(oldSize - newSize);

            this.executor.setCorePoolSize(newSize);
            this.executor.setMaximumPoolSize(newSize);
        }
        else {
            this.executor.setCorePoolSize(newSize);
            this.executor.setMaximumPoolSize(newSize);

            this.semaphore.release(newSize - oldSize);
        }
    }

    /**
     * Waits for all executing tasks to complete.
     */
    public void waitForAll() throws InterruptedException {
        if(this.inProgress.get() == 0) {
            return;
        }

        this.lock.lock();
        try {
            while(!this.isDone) {
                this.done.await();
            }
        }
        finally {
            this.isDone = false;
            this.lock.unlock();
        }
    }
}
