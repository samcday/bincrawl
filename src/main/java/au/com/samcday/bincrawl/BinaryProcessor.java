package au.com.samcday.bincrawl;

import au.com.samcday.bincrawl.dao.BinaryDao;
import au.com.samcday.bincrawl.dao.ReleaseDao;
import au.com.samcday.bincrawl.dao.entities.Binary;
import au.com.samcday.bincrawl.dto.Release;
import au.com.samcday.bincrawl.pool.BetterJedisPool;
import au.com.samcday.bincrawl.pool.PooledJedis;
import au.com.samcday.bincrawl.util.CloseableTimer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import org.ektorp.CouchDbConnector;
import org.ektorp.http.RestTemplate;
import org.ektorp.http.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static au.com.samcday.bincrawl.util.CloseableTimer.startTimer;

/**
 * This class handles processing recently discovered and recently completed binaries.
 */
@Singleton
public class BinaryProcessor {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Splitter PIPE_SPLITTER = Splitter.on("|").limit(2);
    private final Timer processTimer = Metrics.newTimer(BinaryProcessor.class, "new-processed", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
    private final Timer doneTimer = Metrics.newTimer(BinaryProcessor.class, "done-processed", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

    private Logger LOG = LoggerFactory.getLogger(BinaryProcessor.class);

    private BetterJedisPool redisPool;
    private BinaryClassifier classifier;
    private CouchDbConnector couchDb;
    private BinaryDao binaryDao;
    private ReleaseDao releaseDao;

    @Inject
    public BinaryProcessor(BetterJedisPool redisPool, BinaryClassifier classifier, CouchDbConnector couchDb, BinaryDao binaryDao, ReleaseDao releaseDao) {
        this.redisPool = redisPool;
        this.classifier = classifier;
        this.couchDb = couchDb;
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
        try(PooledJedis redisClient = this.redisPool.get(); CloseableTimer ignored = startTimer(this.doneTimer)) {
            String key = RedisKeys.binary(binaryHash);
            String releaseId = redisClient.hget(key, RedisKeys.binaryRelease);
            if(releaseId == null) {
                // Probably got crawled so fast that binaryProcess hasn't been picked up yet.
                return false;
            }

            Map<String, String> binaryInfo = redisClient.hgetAll (key);
            ImmutableMap.Builder<String, String> infoBuilder = ImmutableMap.<String,String>builder()
                .put("hash", binaryHash)
                .put("num", binaryInfo.get(RedisKeys.binaryTotalParts))
                .put("group", binaryInfo.get(RedisKeys.binaryGroup))
                .put("name", binaryInfo.get(RedisKeys.binarySubject))
                .put("date", binaryInfo.get(RedisKeys.binaryDate))
                .put("releaseNum", binaryInfo.get(RedisKeys.binaryReleaseNum));

            for(int i = 1, n = Integer.parseInt(binaryInfo.get(RedisKeys.binaryTotalParts)); i <= n; i++) {
                Iterator<String> parts = PIPE_SPLITTER.split(binaryInfo.get(RedisKeys.binaryPart(i))).iterator();
                infoBuilder.put("part" + i + "_size", parts.next());
                infoBuilder.put("part" + i + "_id", parts.next());
            }

            this.executeBetterUpdateHandler(releaseId, infoBuilder.build());

            return true;
        }
    }

    /**
     * Ektorp CouchDbConnector supports update handlers, but doesn't let you send a body.
     */
    private void executeBetterUpdateHandler(String releaseId, Map<String, String> body) {
        URI uri = URI.of(this.couchDb.path()).append("_design/bincrawl").append("_update").append("addbinary")
            .append(releaseId);

        try {
            new RestTemplate(this.couchDb.getConnection()).put(uri.toString(), MAPPER.writeValueAsString(body));
        }
        catch(JsonProcessingException jpe) {
            throw new RuntimeException(jpe);
        }
    }
}
