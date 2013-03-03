package au.com.samcday.bincrawl.util;

import java.util.concurrent.Semaphore;

/**
 * This Semaphore subclass simply increases visibility of reducePermits to public.
 */
public class ReducibleSemaphore extends Semaphore {
    public ReducibleSemaphore(int permits) {
        super(permits);
    }

    public ReducibleSemaphore(int permits, boolean fair) {
        super(permits, fair);
    }

    @Override
    public void reducePermits(int reduction) {
        super.reducePermits(reduction);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
