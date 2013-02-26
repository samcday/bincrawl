package au.com.samcday.bincrawl;

import au.com.samcday.bincrawl.dto.Release;
import com.google.inject.Inject;
import org.ektorp.CouchDbConnector;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;

public class BinaryProcessor {
    private JedisPool redisPool;
    private BinaryClassifier classifier;
    private CouchDbConnector couchDb;

    @Inject
    public BinaryProcessor(JedisPool redisPool, BinaryClassifier classifier, CouchDbConnector couchDb) {
        this.redisPool = redisPool;
        this.classifier = classifier;
        this.couchDb = couchDb;
    }

    public void processBinary(String binaryHash) {
        Jedis redisClient = this.redisPool.getResource();

        try {
            List<String> fields = redisClient.hmget ("binary:" + binaryHash, "group", "subj");
            BinaryClassifier.Classification classification = this.classifier.classify(fields.get(0), fields.get(1));

            if(classification != null) {
                this.createRelease(classification);
            }
        }
        finally {
            this.redisPool.returnResource(redisClient);
        }
    }

    private Release createRelease(BinaryClassifier.Classification classification) {
        return null;
    }
}
