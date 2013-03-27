package au.com.samcday.bincrawl.dao;

import au.com.samcday.bincrawl.BinaryClassifier;
import au.com.samcday.bincrawl.RedisKeys;
import au.com.samcday.bincrawl.dao.entities.Binary;
import au.com.samcday.bincrawl.dao.entities.BinaryPart;
import au.com.samcday.bincrawl.dto.Release;
import au.com.samcday.bincrawl.pool.BetterJedisPool;
import au.com.samcday.bincrawl.pool.PooledJedis;
import au.com.samcday.jnntp.Overview;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
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

    private BetterJedisPool redisPool;
    private ObjectMapper objectMapper;

    @Inject
    public BinaryDaoRedisImpl(BetterJedisPool redisPool, ObjectMapper objectMapper) {
        this.redisPool = redisPool;
        this.objectMapper = objectMapper;
    }

    @Override
    public String addBinaryPart(String group, String subject, int partNum, int numParts, Overview overview,
                                BinaryClassifier.Classification classification) {
        final String binaryHash = HASHER.hashString(group + subject).toString();
        String keyName = RedisKeys.binary(binaryHash);

        try(PooledJedis redisClient = this.redisPool.get()) {
            Map<String, String> data = ImmutableMap.of(
                RedisKeys.binarySubject, subject,
                RedisKeys.binaryTotalParts, Integer.toString(numParts),
                RedisKeys.binaryDate, Long.toString(overview.getDate().getTime()),
                RedisKeys.binaryGroup, group
            );


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

            String binaryKey = RedisKeys.binary(binaryHash);

            boolean added = redisClient.hsetnx(binaryKey, RedisKeys.binaryPart(partNum), this.objectMapper.writeValueAsString(
                this.objectMapper.createArrayNode().add(overview.getMessageId()).add(overview.getBytes()))) == 1;

            if(added) {
                int totalParts = redisClient.hgetint(binaryKey, RedisKeys.binaryTotalParts).get();
                int numPartsDone = redisClient.hincrBy(binaryKey, RedisKeys.binaryDone, 1).intValue();

                if(numPartsDone >= totalParts) {
                    LOG.trace("Got all parts for binary {}", binaryHash);

                    String releaseId = Release.buildId(group, classification.name);

                    long releaseBinaries = redisClient.lpush(RedisKeys.releaseBinaries(releaseId), binaryHash);

                    if(releaseBinaries == classification.totalParts) {
                        redisClient.lpush(RedisKeys.releaseComplete, releaseId);
                    }
                }
            }
        }
        catch(JsonProcessingException jpe) {
            throw Throwables.propagate(jpe);
        }

        return binaryHash;
    }

    @Override
    public void deleteBinary(String binaryHash) {
        try(PooledJedis redisClient = this.redisPool.get()) {
            redisClient.del(RedisKeys.binary(binaryHash));
        }
    }

    @Override
    public Binary getBinary(String binaryHash) {
        try(PooledJedis redisClient = this.redisPool.get()) {
            Map<String, String> data = redisClient.hgetAll(RedisKeys.binary(binaryHash));
            if(data.isEmpty()) return null;

            Binary binary = new Binary();
            binary.setBinaryHash(binaryHash);
            binary.setSubject(data.get(RedisKeys.binarySubject));
            binary.setDate(new DateTime(Long.parseLong(data.get(RedisKeys.binaryDate))));
            binary.setTotalParts(Integer.parseInt(data.get(RedisKeys.binaryTotalParts)));
            binary.setGroup(data.get(RedisKeys.binaryGroup));

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
    public void processCompletedRelease(CompletedReleaseHandler handler) {
        try(PooledJedis redisClient = this.redisPool.get()) {
            String releaseId = redisClient.brpopsingle(0, RedisKeys.releaseComplete);
            LOG.info("Processing complete release {}", releaseId);

            List<String> binaryHashes = redisClient.lrange(RedisKeys.releaseBinaries(releaseId), 0, -1);
            List<Binary> binaries = new ArrayList<>(binaryHashes.size());

            for(String binaryHash : binaryHashes) {
                Binary completed = this.getBinary(binaryHash);
                if(completed == null) continue; // Shouldn't happen?
                binaries.add(completed);
            }

            boolean success = false;
            try {
                success = handler.handle(binaries);
            }
            catch(Exception e) {
                LOG.warn("Caught unhandled exception from completed binary handler.", e);
            }
            finally {
                if(success) {
                    Transaction t = redisClient.multi();
                    for(String binaryHash : binaryHashes) {
                        t.del(RedisKeys.binary(binaryHash));
                    }
                    t.del(RedisKeys.releaseBinaries(releaseId));
                    t.exec();
                }
                else {
                    redisClient.lpush(RedisKeys.releaseComplete, releaseId);
                }
            }
        }
    }
}
