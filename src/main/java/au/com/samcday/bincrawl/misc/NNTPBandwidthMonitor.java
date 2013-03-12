package au.com.samcday.bincrawl.misc;

import au.com.samcday.jnntp.bandwidth.BandwidthHandler;
import com.google.inject.Singleton;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;

import java.util.concurrent.TimeUnit;

@Singleton
public class NNTPBandwidthMonitor implements BandwidthHandler {
    private static final Meter readMeter = Metrics.newMeter(NNTPBandwidthMonitor.class, "read", "bytes", TimeUnit.SECONDS);
    private static final Meter writeMeter = Metrics.newMeter(NNTPBandwidthMonitor.class, "written", "bytes", TimeUnit.SECONDS);

    @Override
    public void update(long read, long written) {
        readMeter.mark(read);
        writeMeter.mark(written);
    }
}
