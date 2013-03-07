package au.com.samcday.bincrawl.util;

import com.yammer.metrics.core.Stoppable;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

public class CloseableTimer implements AutoCloseable, Stoppable {
    private TimerContext delegee;

    public static final CloseableTimer startTimer(Timer timer) {
        return new CloseableTimer(timer.time());
    }

    public CloseableTimer(TimerContext delegee) {
        this.delegee = delegee;
    }

    @Override
    public void close() {
        this.stop();
    }

    @Override
    public void stop() {
        this.delegee.stop();
    }
}
