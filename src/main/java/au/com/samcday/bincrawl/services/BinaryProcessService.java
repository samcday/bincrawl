package au.com.samcday.bincrawl.services;

import au.com.samcday.bincrawl.BinaryProcessor;
import au.com.samcday.bincrawl.dao.BinaryDao;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BinaryProcessService extends AbstractExecutionThreadService {
    private Logger LOG = LoggerFactory.getLogger(BinaryProcessService.class);

    private BinaryDao binaryDao;
    private BinaryProcessor binaryProcessor;

    @Inject
    public BinaryProcessService(BinaryDao binaryDao, BinaryProcessor binaryProcessor) {
        this.binaryDao = binaryDao;
        this.binaryProcessor = binaryProcessor;
    }

    @Override
    protected void run() throws Exception {
        while(this.isRunning()) {
            
        }

        /*
        try(PooledJedis redisClient = this.redisPool.get()) {
            while(this.isRunning()) {
                String binaryHash = redisClient.brpopsingle(0, RedisKeys.binaryProcess);
                LOG.info("Processing binary {}", binaryHash);
                boolean success = binaryProcessor.processBinary(binaryHash);

                if(!success) {
                    redisClient.lpush(RedisKeys.binaryProcessFailed, binaryHash);
                }
            }
        }*/
    }
}
