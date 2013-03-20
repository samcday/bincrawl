package au.com.samcday.bincrawl.dao;

import au.com.samcday.bincrawl.RedisKeys;
import au.com.samcday.bincrawl.dao.entities.Binary;
import au.com.samcday.bincrawl.dao.entities.BinaryPart;
import au.com.samcday.bincrawl.pool.BetterJedisPool;
import au.com.samcday.bincrawl.pool.PooledJedis;
import au.com.samcday.jnntp.Overview;
import au.com.samcday.jnntp.Xref;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    public String createOrUpdateBinary(String group, String subject, int numParts, Overview overview) {
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

                if(t.exec() != null) {
                    LOG.info("Created new binary with subject {} ({})", subject, binaryHash);
                    break;
                }
            }

            redisClient.sadd(RedisKeys.binaryGroups(binaryHash), group);

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

            redisClient.hsetnx(binaryKey, RedisKeys.binaryPart(partNum), this.objectMapper.writeValueAsString(
                this.objectMapper.createArrayNode().add(overview.getMessageId()).add(overview.getBytes())));

            int totalParts = redisClient.hgetint(binaryKey, RedisKeys.binaryTotalParts).get();
            int numPartsDone = redisClient.hincrBy(binaryKey, RedisKeys.binaryDone, 1).intValue();

            if(numPartsDone >= totalParts) {
                LOG.trace("Got all parts for binary {}", binaryHash);
                redisClient.lpush(RedisKeys.binaryComplete, binaryHash);
            }
        }
        catch(JsonProcessingException jpe) {
            throw Throwables.propagate(jpe);
        }
    }

    @Override
    public void deleteBinary(String binaryHash) {

    }

    @Override
    public Binary getBinary(String binaryHash) {
        try(PooledJedis redisClient = this.redisPool.get()) {
            Map<String, String> data = redisClient.hgetAll(RedisKeys.binary(binaryHash));
            if(data.isEmpty()) return null;

            Binary binary = new Binary();
            binary.setBinaryHash(binaryHash);
            binary.setReleaseId(data.get(RedisKeys.binaryRelease));
            binary.setReleaseNum(Ints.tryParse(data.get(RedisKeys.binaryReleaseNum)));
            binary.setSubject(data.get(RedisKeys.binarySubject));
            binary.setReleaseId(data.get(RedisKeys.binaryRelease));
            binary.setReleaseNum(Ints.tryParse(RedisKeys.binaryReleaseNum));
            binary.setDate(new DateTime(Long.parseLong(data.get(RedisKeys.binaryDate))));
            binary.setTotalParts(Integer.parseInt(data.get(RedisKeys.binaryTotalParts)));
            binary.setGroups(redisClient.smembers(RedisKeys.binaryGroups(binaryHash)));

            List<BinaryPart> parts = new ArrayList<>();
            for(Map.Entry<String, String> entry : data.entrySet()) {
                if(entry.getKey().startsWith("p:")) {
                    int partNum = Integer.parseInt(entry.getKey().substring(2));
                    ArrayNode partData = this.objectMapper.readValue(entry.getValue(), ArrayNode.class);
                    parts.add(new BinaryPart(partNum, partData.get(0).textValue(), partData.get(1).intValue()));
                }
            }

            binary.setParts(parts);

            return binary;
        }
        catch(IOException ioe) {
            throw Throwables.propagate(ioe);
        }
    }

    @Override
    public void setReleaseInfo(String binaryHash, String releaseId, int num) {
        try(PooledJedis redisClient = this.redisPool.get()) {
            redisClient.hsetnx(RedisKeys.binary(binaryHash), RedisKeys.binaryRelease, releaseId);
            redisClient.hsetnx(RedisKeys.binary(binaryHash), RedisKeys.binaryReleaseNum, Integer.toString(num));
        }
    }
}
