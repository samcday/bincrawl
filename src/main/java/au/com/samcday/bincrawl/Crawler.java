package au.com.samcday.bincrawl;

import au.com.samcday.bincrawl.dao.BinaryDao;
import au.com.samcday.bincrawl.pool.NntpClientPool;
import au.com.samcday.bincrawl.pool.PooledNntpClient;
import au.com.samcday.jnntp.Overview;
import au.com.samcday.jnntp.OverviewList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import org.joda.time.Interval;
import org.joda.time.MutableDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is responsible for crawling a fixed number of articles from a group. It is safe to use by multiple threads
 */
@Singleton
public class Crawler {
    private static final Logger LOG = LoggerFactory.getLogger(Crawler.class);
    private final Timer crawlTimer = Metrics.newTimer(Crawler.class, "posts", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

    private static final Pattern PART_REGEX = Pattern.compile("\\((\\d+)[\\\\/](\\d+)\\)");

    private NntpClientPool nntpClientPool;
    private BinaryDao binaryDao;
    private BinaryClassifier binaryClassifier;

    @Inject
    public Crawler(NntpClientPool nntpClientPool, BinaryDao binaryDao, BinaryClassifier binaryClassifier) {
        this.nntpClientPool = nntpClientPool;
        this.binaryDao = binaryDao;
        this.binaryClassifier = binaryClassifier;
    }

    public Result crawl(String group, long from, long to) throws Exception {
        try(PooledNntpClient nntpClient = this.nntpClientPool.borrow()) {
            Counter crawledArticleCounter = Metrics.newCounter(Crawler.class, "crawled", group);

            LOG.info("Starting crawl of group {} of articles {} - {}", group, from, to);
            nntpClient.group(group);

            Result result = new Result();

            OverviewList overviewList = nntpClient.overview(from, to);
            MutableDateTime earliest = new MutableDateTime(Long.MAX_VALUE), latest = new MutableDateTime(0);

            for(Overview overview : overviewList) {
                LOG.trace("Processing group {} article {}", group, overview.getArticle());

                TimerContext ctx = this.crawlTimer.time();
                try {
                    if(!this.processItem(group, overview)) {
                        result.ignored++;
                    }
                    Date date = overview.getDate();
                    if(date != null) {
                        long dateInstant = date.getTime();
                        if(earliest.isAfter(dateInstant)) {
                            earliest.setMillis(dateInstant);
                        }
                        if(latest.isBefore(dateInstant)) {
                            latest.setMillis(dateInstant);
                        }

                    }
                    result.processed++;
                    crawledArticleCounter.inc();
                }
                finally {
                    ctx.stop();
                }
            }

            result.missingArticles = overviewList.getMissingArticles();
            result.dateRange = new Interval(earliest, latest);
            return result;
        }
    }

    private boolean processItem(String group, Overview overview) {
        // First step is to determine what part number this post is.

        // We phase thru the matches first, and save off the last matching position we found. If we don't find a match,
        // it's *probably* not a binary.
        Matcher partMatcher = PART_REGEX.matcher(overview.getSubject());
        int lastMatch = -1;
        while(partMatcher.find()) {
            lastMatch = partMatcher.start();
        }

        if(lastMatch == -1) {
            LOG.info("Couldn't match {}", overview.getSubject());
            return false;
        }

        // Now we go back through the matches again, and only capture + remove the last match, as it *could* have one
        // before that is the binary number of the release.
        StringBuffer buf = new StringBuffer();
        partMatcher.reset();
        partMatcher.find(lastMatch);

        partMatcher.appendReplacement(buf, "");
        partMatcher.appendTail(buf);

        int partNum, totalParts;
        try {
            partNum = Integer.parseInt(partMatcher.group(1));
            totalParts = Integer.parseInt(partMatcher.group(2));
        }
        catch(NumberFormatException nfe) {
            LOG.warn("Error parsing a binary part number for article {} of {}", overview.getArticle(), group, nfe);
            return false;
        }

        String parsedSubject = buf.toString();
        LOG.trace("Found binary part {} of {} for binary with subject {}", partNum, totalParts, parsedSubject);

        if(this.binaryClassifier.classify(group, parsedSubject) != null) {
            String binaryHash = this.binaryDao.createOrUpdateBinary(group, parsedSubject, totalParts, overview);
            this.binaryDao.addBinaryPart(binaryHash, partNum, overview);
        }
        else {
            LOG.trace("Skipping {} of {} as we couldn't classify it.", overview.getArticle(), group);
        }

        return true;
    }

    public static class Result {
        /**
         * Number of articles that did not come back from server during crawl.
         */
        public List<Long> missingArticles;

        /**
         * Number of articles processed.
         */
        public int processed;

        /**
         * Number of articles ignored.
         */
        public int ignored;

        /**
         * Lowest and highest dates that were crawled.
         */
        public Interval dateRange;
    }
}
