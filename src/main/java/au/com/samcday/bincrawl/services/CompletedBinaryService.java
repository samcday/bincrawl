package au.com.samcday.bincrawl.services;

import au.com.samcday.bincrawl.dao.BinaryDao;
import au.com.samcday.bincrawl.dao.ReleaseDao;
import au.com.samcday.bincrawl.dao.entities.Binary;
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

    @Inject
    public CompletedBinaryService(BinaryDao binaryDao, ReleaseDao releaseDao) {
        this.binaryDao = binaryDao;
        this.releaseDao = releaseDao;
    }

    @Override
    protected void run() throws Exception {
        while(this.isRunning()) {
            this.binaryDao.processCompletedBinary(new BinaryDao.CompletedBinaryHandler() {
                @Override
                public boolean handle(Binary completed) throws Exception {
                    return true;
                }
            });
        }
    }
}
