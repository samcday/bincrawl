package au.com.samcday.bincrawl.services;

import au.com.samcday.bincrawl.BinaryProcessor;
import au.com.samcday.bincrawl.RedisKeys;
import au.com.samcday.bincrawl.pool.BetterJedisPool;
import au.com.samcday.bincrawl.pool.PooledJedis;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CompletedBinaryService extends AbstractExecutionThreadService {
    private static final Logger LOG = LoggerFactory.getLogger(CompletedBinaryService.class);

    private BetterJedisPool redisPool;
    private BinaryProcessor binaryProcessor;

    @Inject
    public CompletedBinaryService(BetterJedisPool redisPool, BinaryProcessor binaryProcessor) {
        this.redisPool = redisPool;
        this.binaryProcessor = binaryProcessor;
    }

    @Override
    protected void run() throws Exception {
        try(PooledJedis redisClient = this.redisPool.get()) {
            while(this.isRunning()) {
                String binaryHash = redisClient.brpopsingle(0, RedisKeys.binaryComplete);
                LOG.info("Processing complete binary {}", binaryHash);

                if(binaryProcessor.processCompletedBinary(binaryHash)) {
                    redisClient.del(RedisKeys.binary(binaryHash));
                }
                else {
                    redisClient.lpush(RedisKeys.binaryComplete, binaryHash);
                }
            }
        }
    }
}
