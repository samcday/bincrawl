package au.com.samcday.bincrawl;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.yammer.metrics.reporting.ConsoleReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.TimeUnit;

public class App {
    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    public static final void main(String... args) throws Exception {
        Injector injector = Guice.createInjector(new AppModule());

        ConsoleReporter.enable(5, TimeUnit.SECONDS);

//        Crawler crawler = injector.getInstance(Crawler.class);
//        BinaryPartProcessor partProcessor = injector.getInstance(BinaryPartProcessor.class);
//        Crawler.Result result = crawler.crawl(partProcessor, "alt.binaries.teevee", 477372648, 477572648);
//        LOG.info("Crawled {} articles with {} ignored and {} missing.", result.processed, result.ignored, result.missingArticles.size());

        BinaryProcessor processor = injector.getInstance(BinaryProcessor.class);
        Jedis redis = injector.getInstance(JedisPool.class).getResource();
//        int failed = 0;
//        for(int i = 0; i < redis.llen("binaryProcess"); i++) {
//            String binaryHash = redis.lindex("binaryProcess", i);
//            if(!processor.processBinary(binaryHash)) {
//                failed++;
//            }
//        }
//
//        LOG.info("Done. Failed: {}", failed);
//
        int failed = 0;
        for(int i = 0; i < redis.llen("binaryDone"); i++) {
            String binaryHash = redis.lindex("binaryDone", i);
            if(!processor.processCompletedBinary(binaryHash)) {
                failed++;
            }
        }

        LOG.info("Done. Failed: {}", failed);

        if(1==1) return;

        /*int total = 0;
        List<String> items = jedis.lrange("binaryProcess", 0, 1000000);
        long start = System.currentTimeMillis();
        for(String binaryHash : items) {
            total++;
            List<String> fields = jedis.hmget ("binary:" + binaryHash, "group", "subj");

            BinaryClassifier.Classification classification = proc.classify(fields.get(0), fields.get(1));
            if(classification == null) {
                System.out.println("Couldn't classify " + fields.get(1) + " :(");
            }
            else {
                System.out.println("Classified " + fields.get(1) + " (" + binaryHash + ") as " + classification.name + " part " + classification.partNum + "/" + classification.totalParts);
            }
        }*/

//        System.out.println("Done " + (((double)System.currentTimeMillis() - (double)start) / (double)total) + "ms per item");
    }
}
