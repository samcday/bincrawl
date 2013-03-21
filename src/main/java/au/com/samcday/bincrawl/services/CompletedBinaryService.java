package au.com.samcday.bincrawl.services;

import au.com.samcday.bincrawl.BinaryClassifier;
import au.com.samcday.bincrawl.dao.BinaryDao;
import au.com.samcday.bincrawl.dao.ReleaseDao;
import au.com.samcday.bincrawl.dao.entities.Binary;
import au.com.samcday.bincrawl.dto.Release;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CompletedBinaryService extends AbstractExecutionThreadService {
    private static final Logger LOG = LoggerFactory.getLogger(CompletedBinaryService.class);

    private BinaryDao binaryDao;
    private ReleaseDao releaseDao;
    private BinaryClassifier binaryClassifier;

    @Inject
    public CompletedBinaryService(BinaryDao binaryDao, ReleaseDao releaseDao, BinaryClassifier binaryClassifier) {
        this.binaryDao = binaryDao;
        this.releaseDao = releaseDao;
        this.binaryClassifier = binaryClassifier;
    }

    @Override
    protected void run() throws Exception {
        while(this.isRunning()) {
            this.binaryDao.processCompletedBinary(new BinaryDao.CompletedBinaryHandler() {
                @Override
                public boolean handle(Binary completed) throws Exception {
                    BinaryClassifier.Classification classification = binaryClassifier.classify(completed.getGroup(), completed.getSubject());
                    Release release = releaseDao.createRelease(completed.getGroup(), classification);
                    releaseDao.addCompletedBinary(release.getId(), completed);
                    LOG.info("Processed completed binary {}", completed.getBinaryHash());
                    return true;
                }
            });
        }
    }
}
