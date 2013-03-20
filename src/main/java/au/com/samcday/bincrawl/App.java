package au.com.samcday.bincrawl;

import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.yammer.metrics.reporting.ConsoleReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class App {
    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    public static final void main(String... args) throws Exception {
        Injector injector = Guice.createInjector(new AppModule());

//        NntpClientPool pool = injector.getInstance(NntpClientPool.class);
//
//        FileOutputStream fout = new FileOutputStream("/tmp/blah");
//        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fout));
//        Crawler crawler = new Crawler(pool, new BinaryDao() {
//            @Override
//            public String createOrUpdateBinary(String group, String processedSubject, int numParts, Overview overview) {
//                try {
//                    writer.write(processedSubject + "\n");
//                    writer.flush();
//                }
//                catch(IOException ioe) {
//                    ioe.printStackTrace();
//                }
//
//                return null;
//            }
//
//            @Override
//            public void addBinaryPart(String binaryHash, int partNum, Overview overview) {
//                //To change body of implemented methods use File | Settings | File Templates.
//            }
//
//            @Override
//            public void deleteBinary(String binaryHash) {
//                //To change body of implemented methods use File | Settings | File Templates.
//            }
//
//            @Override
//            public void setReleaseInfo(String binaryHash, String releaseId, int num) {
//                //To change body of implemented methods use File | Settings | File Templates.
//            }
//
//            @Override
//            public Binary getBinary(String binaryHash) {
//                return null;  //To change body of implemented methods use File | Settings | File Templates.
//            }
//        }, null);
//
//
//        crawler.crawl("alt.binaries.hdtv", 2987896601l - 100000, 2987896601l);
//        writer.close();
//        fout.close();

        BinaryClassifier classifier = injector.getInstance(BinaryClassifier.class);
        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream("/tmp/blah")));
        List<String> lines = new ArrayList<>();
        String read;
        while((read = r.readLine()) != null) {
            lines.add(read);
        }

        System.out.printf("%d items. %d unique items.\n", lines.size(), Sets.newHashSet(lines).size());

        ConsoleReporter.enable(1, TimeUnit.SECONDS);
        int total = 0;
        int matched = 0;
        long start = System.currentTimeMillis();
        for(int i = 0; i < 100; i++) {
            for(String line : lines) {
                if(classifier.classify("alt.binaries.hdtv", line) != null) matched++;
                total++;
            }
        }

        long took = System.currentTimeMillis() - start;
        System.out.printf("Took %dms to process %d items. Matched %d. %f per item\n", took, total, matched, (double) took / (double) total);


        if(1==1) return;

//        Crawler crawler = injector.getInstance(Crawler.class);
//        Crawler.Result res = crawler.crawl("alt.binaries.hdtv", 2972783365l, 2972784237l);
//        System.out.println(res.dateRange);

//        NntpClientPool pool = injector.getInstance(NntpClientPool.class);
//        NntpClient client = pool.borrow();
//        GroupInfo info = client.group("alt.binaries.hdtv");
//        OverviewList list = client.overview(info.high - 100000, info.high);
//        for(Overview overview : list) {
//            System.out.println(overview.getSubject());
//        }
//
////        BinaryProcessor processor = injector.getInstance(BinaryProcessor.class);
////        System.out.println(processor.processBinary("ad17f16b"));
//
//        if(1==1) return;

//        BinaryClassifier classifier = injector.getInstance(BinaryClassifier.class);
//        BinaryClassifier.Classification res = classifier.classify("alt.binaries.teevee", "[128471]-[FULL]-[#a.b.teevee]-[ Jimmy.Kimmel.2013.02.24.After.the.Oscars.Special.720p.HDTV.x264-2HD ]-[17/46] - \"jimmy.kimmel.2013.02.24.after.the.oscars.special.720p.hdtv.x264-2hd.r06\" yEnc");
//
//        if(1==1) return;

        ConsoleReporter.enable(5, TimeUnit.SECONDS);
//
//        Jedis redis = injector.getInstance(JedisPool.class).getResource();
////        processor = injector.getInstance(BinaryProcessor.class);
//
////        Crawler crawler = injector.getInstance(Crawler.class);
//////        Crawler.Result result = crawler.crawl(partProcessor, "alt.binaries.teevee", 477372648, 477572648);
////        Crawler.Result result = crawler.crawl(partProcessor, "alt.binaries.teevee", 477522648, 477572648);
////        LOG.info("Crawled {} articles with {} ignored and {} missing.", result.processed, result.ignored, result.missingArticles.size());
//
//
//        int failed = 0;
//        for(int i = 0; i < redis.llen("binaryProcess"); i++) {
//            String binaryHash = redis.lindex("binaryProcess", i);
//            if(!processor.processBinary(binaryHash)) {
//                failed++;
//            }
//        }
//        LOG.info("Done. Failed: {}", failed);
//
//        failed = 0;
//        for(int i = 0; i < redis.llen("binaryDone"); i++) {
//            String binaryHash = redis.lindex("binaryDone", i);
//            if(!processor.processCompletedBinary(binaryHash)) {
//                failed++;
//            }
//        }
//        LOG.info("Done. Failed: {}", failed);
//
//        if(1==1) return;
//
//        /*int total = 0;
//        List<String> items = jedis.lrange("binaryProcess", 0, 1000000);
//        long start = System.currentTimeMillis();
//        for(String binaryHash : items) {
//            total++;
//            List<String> fields = jedis.hmget ("binary:" + binaryHash, "group", "subj");
//
//            BinaryClassifier.Classification classification = proc.classify(fields.get(0), fields.get(1));
//            if(classification == null) {
//                System.out.println("Couldn't classify " + fields.get(1) + " :(");
//            }
//            else {
//                System.out.println("Classified " + fields.get(1) + " (" + binaryHash + ") as " + classification.name + " part " + classification.partNum + "/" + classification.totalParts);
//            }
//        }*/
//
//        System.out.println("Done " + (((double)System.currentTimeMillis() - (double)start) / (double)total) + "ms per item");
    }
}
