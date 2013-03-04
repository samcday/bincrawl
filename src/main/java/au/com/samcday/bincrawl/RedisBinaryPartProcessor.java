package au.com.samcday.bincrawl;

import au.com.samcday.bincrawl.pool.BetterJedisPool;
import com.google.common.base.Joiner;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RedisBinaryPartProcessor implements BinaryPartProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(RedisBinaryPartProcessor.class);

    private static final HashFunction SHA1 = Hashing.sha1();
    private static final Joiner PIPE_JOINER = Joiner.on("|");

    private BetterJedisPool redisPool;

    @Inject
    public RedisBinaryPartProcessor(BetterJedisPool redisPool) {
        this.redisPool = redisPool;
    }

    @Override
    public void processPart(String group, String name, Date date, int size, String messageId, int part, int totalParts) {
        Jedis redisClient = this.redisPool.getResource();

        try {
            // Create the binary in Redis if it doesn't already exist.
            String binaryHash = this.createBinary(redisClient, group, name, date, totalParts);

            String key = RedisKeys.binary(binaryHash);
            String value = PIPE_JOINER.join(size, messageId);

            redisClient.hsetnx(key, RedisKeys.binaryPart(part), value);

            int numPartsDone = redisClient.hincrBy(key, RedisKeys.binaryDone, 1).intValue();
            if(numPartsDone >= totalParts) {
                LOG.trace("Got all parts for binary with subject {} ({})", name, binaryHash);
                redisClient.rpush("binaryDone", binaryHash);
            }
        }
        finally {
            this.redisPool.returnResource(redisClient);
        }
    }

    /**
     * This method will create an in-progress binary in Redis if necessary. The insertion into Redis will be atomic.
     */
    private String createBinary(Jedis redisClient, String group, String subject, Date date, int numParts) {
        String binaryHash = SHA1.newHasher().putString(group).putString(subject).hash().toString();
        String keyName = RedisKeys.binary(binaryHash);

        Map<String, String> data = new HashMap<String, String>();
        data.put(RedisKeys.binaryGroup, group);
        data.put(RedisKeys.binarySubject, subject);
        data.put(RedisKeys.binaryTotalParts, Integer.toString(numParts));
        data.put(RedisKeys.binaryDate, Long.toString(date.getTime()));

        while(true) {
            if(redisClient.exists(keyName)) {
                return binaryHash;
            }

            redisClient.watch(keyName);
            Transaction t = redisClient.multi();
            t.hmset(keyName, data);
            t.lpush(RedisKeys.binaryProcess, binaryHash);

            if(t.exec() != null) {
                LOG.info("Created new binary with subject {} ({})", subject, binaryHash);
                return binaryHash;
            }
        }
    }
}
