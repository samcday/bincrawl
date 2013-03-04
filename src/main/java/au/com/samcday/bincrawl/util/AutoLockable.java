package au.com.samcday.bincrawl.util;

import java.util.concurrent.locks.Lock;

public class AutoLockable implements AutoCloseable {
    private Lock lock;

    public static final AutoLockable lock(Lock lock) {
        lock.lock();
        return new AutoLockable(lock);
    }

    private AutoLockable(Lock lock) {
        this.lock = lock;
    }

    @Override
    public void close() {
        this.lock.unlock();
    }
}
