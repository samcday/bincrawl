package au.com.samcday.bincrawl;

import au.com.samcday.bincrawl.dao.BinaryDao;
import au.com.samcday.bincrawl.dao.ReleaseDao;
import au.com.samcday.bincrawl.dao.entities.Binary;
import au.com.samcday.bincrawl.dto.Release;
import au.com.samcday.bincrawl.util.CloseableTimer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static au.com.samcday.bincrawl.util.CloseableTimer.startTimer;

/**
 * This class handles processing recently discovered and recently completed binaries.
 */
@Singleton
public class BinaryProcessor {
    private final Timer processTimer = Metrics.newTimer(BinaryProcessor.class, "new-processed", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
    private final Timer doneTimer = Metrics.newTimer(BinaryProcessor.class, "done-processed", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

    private Logger LOG = LoggerFactory.getLogger(BinaryProcessor.class);

    private BinaryClassifier classifier;
    private BinaryDao binaryDao;
    private ReleaseDao releaseDao;

    @Inject
    public BinaryProcessor(BinaryClassifier classifier, BinaryDao binaryDao, ReleaseDao releaseDao) {
        this.classifier = classifier;
        this.binaryDao = binaryDao;
        this.releaseDao = releaseDao;
    }

    public boolean processBinary(String binaryHash) {
        try(CloseableTimer ignored = startTimer(this.processTimer)) {
            Binary binary = this.binaryDao.getBinary(binaryHash);
            BinaryClassifier.Classification classification = null;

            for(String group : binary.getGroups()) {
                classification = this.classifier.classify(group, binary.getSubject());
                if(classification != null) break;
            }

            if(classification != null) {
                Release release = this.releaseDao.createRelease(classification);
                this.binaryDao.setReleaseInfo(binaryHash, release.getId(), classification.partNum);
                return true;
            }
            else {
                LOG.info("Couldn't classify binary with subject", binary.getSubject());
                return false;
            }
        }
    }

    public boolean processCompletedBinary(String binaryHash) {
        try(CloseableTimer ignored = startTimer(this.doneTimer)) {
            Binary binary = this.binaryDao.getBinary(binaryHash);

            if(binary.getReleaseId() == null || binary.getReleaseNum() == null) {
                // Probably got crawled so fast that binaryProcess hasn't been picked up yet.
                return false;
            }

            this.releaseDao.addCompletedBinary(binary);

            return true;
        }
    }
}
