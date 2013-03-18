package au.com.samcday.bincrawl.dao;

import au.com.samcday.bincrawl.RedisKeys;
import au.com.samcday.bincrawl.dao.entities.Binary;
import au.com.samcday.bincrawl.pool.BetterJedisPool;
import au.com.samcday.bincrawl.pool.PooledJedis;
import au.com.samcday.jnntp.Overview;
import au.com.samcday.jnntp.Xref;
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
            RedisKeys.binarySubject, subject,
            RedisKeys.binaryTotalParts, Integer.toString(numParts),
            RedisKeys.binaryDate, Long.toString(overview.getDate().getTime())
        );

        try(PooledJedis redisClient = this.redisPool.get()) {
            while(true) {
                if(redisClient.exists(keyName)) {
                    break;
                }

                redisClient.watch(keyName);
                Transaction t = redisClient.multi();
                t.hmset(keyName, data);
                t.lpush(RedisKeys.binaryProcess, binaryHash);

                if(t.exec() != null) {
                    LOG.info("Created new binary with subject {} ({})", subject, binaryHash);
                    break;
                }
            }

            Xref xref = overview.getXref();
            if(xref != null) {
                for(Xref.Location loc : xref.getLocations()) {
                    redisClient.sadd(RedisKeys.binaryGroups(binaryHash), loc.getGroup());
                }
            }

            return binaryHash;
        }
    }

    @Override
    public void addBinaryPart(String binaryHash, int partNum, Overview overview) {
        try(PooledJedis redisClient = this.redisPool.get()) {
            String binaryKey = RedisKeys.binary(binaryHash);
            redisClient.hsetnx(binaryKey, RedisKeys.binaryPart(partNum),
                PIPE_JOINER.join(overview.getMessageId(), overview.getBytes()));

            int totalParts = redisClient.hgetint(binaryKey, RedisKeys.binaryTotalParts).get();
            int numPartsDone = redisClient.hincrBy(binaryKey, RedisKeys.binaryDone, 1).intValue();

            if(numPartsDone >= totalParts) {
                    LOG.trace("Got all parts for binary {}", binaryHash);
                    redisClient.lpush(RedisKeys.binaryComplete, binaryHash);
            }
        }
    }

    @Override
    public void deleteBinary(String binaryHash) {

    }

    @Override
    public Binary getBinary(String binaryHash) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
