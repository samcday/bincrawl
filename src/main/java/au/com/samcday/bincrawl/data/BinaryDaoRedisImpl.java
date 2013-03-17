package au.com.samcday.bincrawl.data;

import au.com.samcday.bincrawl.RedisKeys;
import au.com.samcday.bincrawl.pool.BetterJedisPool;
import au.com.samcday.bincrawl.pool.PooledJedis;
import au.com.samcday.jnntp.Overview;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Transaction;

import java.util.Map;

@Singleton
public class BinaryDaoRedisImpl implements BinaryDao {
    private static final Logger LOG = LoggerFactory.getLogger(BinaryDaoRedisImpl.class);
    private static final HashFunction HASHER = Hashing.murmur3_32();
    private static final Joiner PIPE_JOINER = Joiner.on("|");

    private BetterJedisPool redisPool;
    private ObjectMapper objectMapper;

    @Inject
    public BinaryDaoRedisImpl(BetterJedisPool redisPool, ObjectMapper objectMapper) {
        this.redisPool = redisPool;
        this.objectMapper = objectMapper;
    }

    @Override
    public String createBinary(String group, String subject, int numParts, Overview overview) {
        final String binaryHash = HASHER.hashString(subject).toString();
        String keyName = RedisKeys.binary(binaryHash);

        Map<String, String> data = ImmutableMap.of(
            RedisKeys.binaryGroup, group,
            RedisKeys.binarySubject, subject,
            RedisKeys.binaryTotalParts, Integer.toString(numParts),
            RedisKeys.binaryDate, Long.toString(overview.getDate().getTime())
        );

        try(PooledJedis redisClient = this.redisPool.get()) {
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

    @Override
    public void addBinaryPart(String binaryHash, int partNum, Overview overview) {
        try(PooledJedis redisClient = this.redisPool.get()) {
            redisClient.hsetnx(RedisKeys.binary(binaryHash), RedisKeys.binaryPart(partNum),
                PIPE_JOINER.join(overview.getMessageId(), overview.getBytes()));

            int numPartsDone = redisClient.hincrBy(key, RedisKeys.binaryDone, 1).intValue();
            if(numPartsDone >= totalParts) {
                LOG.trace("Got all parts for binary with subject {} ({})", name, binaryHash);
                redisClient.lpush(RedisKeys.binaryComplete, binaryHash);
        }
        catch(JsonProcessingException jse) {

        }(redisClient);
        }
    }
}
