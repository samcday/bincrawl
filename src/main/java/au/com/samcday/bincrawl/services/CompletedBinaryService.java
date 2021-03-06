package au.com.samcday.bincrawl.services;

import au.com.samcday.bincrawl.dao.BinaryDao;
import au.com.samcday.bincrawl.dao.ReleaseDao;
import au.com.samcday.bincrawl.dao.entities.Binary;
import au.com.samcday.bincrawl.util.CloseableTimer;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
public class CompletedBinaryService extends AbstractIdleService {
    private static final Logger LOG = LoggerFactory.getLogger(CompletedBinaryService.class);
    private static final int NUM_WORKERS = 5;
    private static final Timer processingTimer = Metrics.newTimer(CompletedBinaryService.class, "processing");

    private BinaryDao binaryDao;
    private ReleaseDao releaseDao;
    private ExecutorService executorService;

    @Inject
    public CompletedBinaryService(BinaryDao binaryDao, ReleaseDao releaseDao) {
        this.binaryDao = binaryDao;
        this.releaseDao = releaseDao;
    }

    @Override
    protected void startUp() throws Exception {
        this.executorService = Executors.newCachedThreadPool();
        for(int i = 0; i < NUM_WORKERS; i++) {
            this.executorService.execute(new Worker());
        }
    }

    private void doProcess() {
        this.binaryDao.processCompletedRelease(new BinaryDao.CompletedReleaseHandler() {
            @Override
            public boolean handle(List<Binary> completed) throws Exception {
                try(CloseableTimer ignored = CloseableTimer.startTimer(processingTimer)) {
                    String releaseId = releaseDao.createReleaseFromBinaries(completed);
                    LOG.info("Processed completed binaries for release {}", releaseId);
                    return true;
                }
            }
        });
    }

    // TODO: shutdown probably won't work as workers will be blpopping redis and if it's empty, they'll block
    // indefinitely. Probably need to use shutdownNow and make sure that Jedis blpop handles interrupt signal correctly
    // and also that our actual completed binary logic is resilient to interrupts.
    @Override
    protected void shutDown() throws Exception {
        this.executorService.shutdown();
    }

    private class Worker implements Runnable {
        @Override
        public void run() {
            while(!executorService.isShutdown()) {
                doProcess();
            }
        }
    }
}
