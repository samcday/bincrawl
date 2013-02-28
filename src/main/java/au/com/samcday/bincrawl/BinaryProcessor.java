package au.com.samcday.bincrawl;

import au.com.samcday.bincrawl.dto.Release;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import org.apache.commons.io.IOUtils;
import org.ektorp.CouchDbConnector;
import org.ektorp.UpdateConflictException;
import org.ektorp.http.HttpResponse;
import org.ektorp.http.RestTemplate;
import org.ektorp.http.StdResponseHandler;
import org.ektorp.http.URI;
import org.ektorp.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Singleton
public class BinaryProcessor {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Splitter PIPE_SPLITTER = Splitter.on("|").limit(2);
    private final Timer processTimer = Metrics.newTimer(BinaryProcessor.class, "new-processed", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
    private final Timer doneTimer = Metrics.newTimer(BinaryProcessor.class, "done-processed", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

    private Logger LOG = LoggerFactory.getLogger(BinaryProcessor.class);

    private JedisPool redisPool;
    private BinaryClassifier classifier;
    private CouchDbConnector couchDb;

    @Inject
    public BinaryProcessor(JedisPool redisPool, BinaryClassifier classifier, CouchDbConnector couchDb) {
        this.redisPool = redisPool;
        this.classifier = classifier;
        this.couchDb = couchDb;
    }

    public boolean processBinary(String binaryHash) {
        Jedis redisClient = this.redisPool.getResource();
        TimerContext timerContext = this.processTimer.time();

        try {
            String key = RedisKeys.binary(binaryHash);
            List<String> fields = redisClient.hmget (key, RedisKeys.binaryGroup, RedisKeys.binarySubject);
            String subject = fields.get(1);
            BinaryClassifier.Classification classification = this.classifier.classify(fields.get(0), subject);

            if(classification != null) {
                Release release = this.createRelease(classification);
                redisClient.hsetnx(key, RedisKeys.binaryRelease, release.getId());
                redisClient.hsetnx(key, RedisKeys.binaryReleaseNum, Integer.toString(classification.partNum));
                return true;
            }
            else {
                LOG.info("Couldn't classify binary with subject", subject);
                return false;
            }
        }
        finally {
            this.redisPool.returnResource(redisClient);
            timerContext.stop();
        }
    }

    public boolean processCompletedBinary(String binaryHash) {
        Jedis redisClient = this.redisPool.getResource();
        TimerContext timerContext = this.doneTimer.time();

        try {
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

//            this.couchDb.callUpdateHandler("_design/bincrawl", "addbinary", releaseId, infoBuilder.build());
            this.executeBetterUpdateHandler(releaseId, infoBuilder.build());

            return true;
        }
        finally {
            timerContext.stop();
            this.redisPool.returnResource(redisClient);
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

    private Release createRelease(BinaryClassifier.Classification classification) {
        while(true) {
            Release release = this.couchDb.find(Release.class, Release.buildId(classification.name));
            if(release == null) {
                release = new Release();
                release.setId(Release.buildId(classification.name));
                release.setName(classification.name);
                release.setCount(classification.totalParts);
                try {
                    this.couchDb.create(release);
                }
                catch(UpdateConflictException uce) {
                    // Ignore, on next loop we'll correctly find the document.
                }
            }
            return release;
        }
    }
}
