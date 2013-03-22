package au.com.samcday.bincrawl.util;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.Uninterruptibles;
import org.junit.Test;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;

public class BlockingExecutorServiceTest {
    private static final int EXPECTED_WAIT_TIME = 250;
    private static final Runnable SLEEPY_RUNNABLE = new Runnable() {
        @Override
        public void run() {
            Uninterruptibles.sleepUninterruptibly(300, TimeUnit.MILLISECONDS);
        }
    };
    private static final Runnable EMPTY_RUNNABLE = new Runnable() {
        @Override
        public void run() {
        }
    };

    @Test
    public void testBoundedBlocking() {
        BlockingExecutorService svc = new BlockingExecutorService(1);

        svc.execute(SLEEPY_RUNNABLE);

        Stopwatch stopwatch = new Stopwatch().start();
        svc.execute(EMPTY_RUNNABLE);
        stopwatch.stop();

        assertThat(stopwatch.elapsed(TimeUnit.MILLISECONDS)).isGreaterThan(EXPECTED_WAIT_TIME);
    }

    @Test
    public void testWaitForAll() throws InterruptedException {
        BlockingExecutorService svc = new BlockingExecutorService(1);

        svc.execute(SLEEPY_RUNNABLE);

        Stopwatch stopwatch = new Stopwatch().start();
        svc.waitForAll();
        stopwatch.stop();
        assertThat(stopwatch.elapsed(TimeUnit.MILLISECONDS)).isGreaterThan(EXPECTED_WAIT_TIME);
    }

    @Test
    public void testDynamicIncrease() {
        final BlockingExecutorService svc = new BlockingExecutorService(1);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                svc.resize(2);
            }
        }, 10);
        svc.execute(SLEEPY_RUNNABLE);
        Stopwatch stopwatch = new Stopwatch().start();
        svc.execute(EMPTY_RUNNABLE);
        stopwatch.stop();
        assertThat(stopwatch.elapsed(TimeUnit.MILLISECONDS)).isLessThan(50);
    }

    @Test
    public void testDynamicDecrease() {
        BlockingExecutorService svc = new BlockingExecutorService(2);
        svc.execute(SLEEPY_RUNNABLE);
        svc.resize(1);

        Stopwatch stopwatch = new Stopwatch().start();
        svc.execute(EMPTY_RUNNABLE);
        stopwatch.stop();
        assertThat(stopwatch.elapsed(TimeUnit.MILLISECONDS)).isGreaterThan(EXPECTED_WAIT_TIME);
    }

    @Test
    public void testZeroSizeBlocksIndefinitely() {
        final BlockingExecutorService svc = new BlockingExecutorService(0);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                svc.resize(1);
            }
        }, 1000);
        Stopwatch stopwatch = new Stopwatch().start();
        svc.execute(EMPTY_RUNNABLE);
        stopwatch.stop();
        assertThat(stopwatch.elapsed(TimeUnit.MILLISECONDS)).isGreaterThan(EXPECTED_WAIT_TIME);
    }
}
