package au.com.samcday.bincrawl;

import au.com.samcday.jnntp.NntpClient;
import au.com.samcday.jnntp.Overview;
import au.com.samcday.jnntp.OverviewList;
import com.google.common.base.Joiner;
import com.google.common.hash.Hashing;
import com.google.inject.Inject;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crawler {
    private static final Logger LOG = LoggerFactory.getLogger(Crawler.class);
    private final Timer crawlTimer = Metrics.newTimer(Crawler.class, "posts", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

    private static final Joiner PIPE_JOINER = Joiner.on("|");
    private static final Pattern PART_REGEX = Pattern.compile("\\((\\d+)[\\\\/](\\d+)\\)");

    private JedisPool redisPool;
    private NntpClientPool nntpClientPool;

    @Inject
    public Crawler(JedisPool redisPool, NntpClientPool nntpClientPool) {
        this.redisPool = redisPool;
        this.nntpClientPool = nntpClientPool;
    }

    public Result crawl(String group, int from, int to) throws Exception {
        // TODO: checked exceptions from nntpclientpool lease...
        NntpClient nntpClient = this.nntpClientPool.borrowObject();
        Jedis redisClient = this.redisPool.getResource();

        LOG.info("Starting crawl of group {} of articles {} - {}", group, from, to);
        nntpClient.group(group);

        OverviewList overviewList = nntpClient.overview(from, to);
        for(Overview overview : overviewList) {
            LOG.trace("Processing group {} article {}", group, overview.getArticle());

            TimerContext ctx = this.crawlTimer.time();
            try {
                this.processItem(redisClient, group, overview);
            }
            finally {
                ctx.stop();
            }
        }

        return null;
    }

    private void processItem(Jedis redisClient, String group, Overview overview) {
        // First step is to determine what part number this post is.

        // We phase thru the matches first, and save off the last matching position we found. If we don't find a match,
        // it's *probably* not a binary.
        Matcher partMatcher = PART_REGEX.matcher(overview.getSubject());
        int lastMatch = -1;
        while(partMatcher.find()) {
            lastMatch = partMatcher.start();
        }

        if(lastMatch == -1) {
            // TODO: some kind of reporting on this?
            System.out.println("Couldn't match " + overview.getSubject());
            return;
        }

        // Now we go back through the matches again, and only capture + remove the last match, as it *could* have one
        // before that is the binary number of the release.
        StringBuffer buf = new StringBuffer();
        partMatcher.reset();
        partMatcher.find(lastMatch);

        partMatcher.appendReplacement(buf, "");
        partMatcher.appendTail(buf);

        // TODO: probably should make sure these parseInts don't fail.
        int partNum = Integer.parseInt(partMatcher.group(1));
        int totalParts = Integer.parseInt(partMatcher.group(2));

        String actualSubject = buf.toString();

        String binaryHash = this.createBinary(redisClient, group, actualSubject, overview.getDate(), totalParts);
        LOG.trace("Found binary part {} of {} for binary with subject {} ({})", partNum, totalParts, actualSubject, binaryHash);

        String key = "binary:" + binaryHash;
        String value = PIPE_JOINER.join(overview.getBytes(), overview.getMessageId());
        redisClient.hsetnx(key, "part:"+partNum, value);

        int numPartsDone = redisClient.hincrBy(key, "done", 1).intValue();

        if(numPartsDone >= totalParts) {
            LOG.trace("Got all parts for binary with subject {} ({})", actualSubject, binaryHash);
            redisClient.rpush("binaryDone", binaryHash);
        }
    }

    private String createBinary(Jedis redisClient, String group, String subject, Date date, int numParts) {
        String binaryHash = Hashing.sha1().newHasher().putString(group).putString(subject).hash().toString();
        String keyName = "binary:" + binaryHash;

        Map<String, String> data = new HashMap<String, String>();
        data.put("group", group);
        data.put("subj", subject);
        data.put("num", Integer.toString(numParts));
        data.put("date", Long.toString(date.getTime()));

        while(true) {
            if(redisClient.exists(keyName)) {
                return binaryHash;
            }

            redisClient.watch(keyName);
            Transaction t = redisClient.multi();
            t.hmset(keyName, data);
            t.lpush("binaryProcess", binaryHash);

            if(t.exec() != null) {
                return binaryHash;
            }
        }
    }

    public static class Result {
        /**
         * Posts that did not come back from server during crawl.
         */
        public List<Integer> missingPosts;
    }
}
