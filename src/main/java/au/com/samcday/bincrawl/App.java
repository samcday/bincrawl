package au.com.samcday.bincrawl;

import com.google.inject.Guice;
import com.google.inject.Injector;
import redis.clients.jedis.Jedis;

import java.util.List;

public class App {
    public static final void main(String... args) throws Exception {
        Injector injector = Guice.createInjector(new AppModule());

        Crawler crawler = injector.getInstance(Crawler.class);

        crawler.crawl("alt.binaries.teevee", 477372648, 477572648);

        if(1==1) return;

        Jedis jedis = new Jedis("localhost");
        jedis.select(5);
//        CrawlTask task = new CrawlTask("alt.binaries.teevee", jedis);
//        task.call();
//
//        if(1==1) return;



        BinaryClassifier proc = new BinaryClassifier();

        int total = 0;
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
        }

//        System.out.println("Done " + (((double)System.currentTimeMillis() - (double)start) / (double)total) + "ms per item");
    }
}
