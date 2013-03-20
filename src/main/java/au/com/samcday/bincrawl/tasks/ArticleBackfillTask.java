package au.com.samcday.bincrawl.tasks;

import au.com.samcday.bincrawl.Crawler;
import au.com.samcday.bincrawl.RedisKeys;
import au.com.samcday.bincrawl.pool.BetterJedisPool;
import au.com.samcday.bincrawl.pool.PooledJedis;
import au.com.samcday.jnntp.GroupInfo;
import com.google.inject.Inject;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Pipeline;

import java.util.concurrent.Callable;

/**
 * Handles crawling a number of posts from specified group. Returns true if there's more articles to be updated.
 */
public class ArticleBackfillTask implements Callable<Boolean> {
    private static final Logger LOG = LoggerFactory.getLogger(ArticleBackfillTask.class);

    private BetterJedisPool redisPool;
    private Crawler crawler;
    private final int numPosts = 20000;         // TODO: make this configurable
    private String group;
    private GroupInfo groupInfo;
//    private MinMaxPriorityQueue<Date> latestDates = new MinMaxPriorityQueue.Builder().maximumSize(10).create();

    @Inject
    public ArticleBackfillTask(BetterJedisPool redisPool, Crawler crawler) {
        this.redisPool = redisPool;
        this.crawler = crawler;
    }

    public void configure(String group, GroupInfo groupInfo) {
        this.group = group;
        this.groupInfo = groupInfo;
    }

    @Override
    public Boolean call() throws Exception {
        try (PooledJedis redisClient = this.redisPool.get()) {
            int maxDays = redisClient.hgetint(RedisKeys.group(this.group), RedisKeys.groupMaxAge).or(-1);
            if(maxDays < 0) {
                return false;
            }
            Long currentDateMillis = redisClient.hgetlong(RedisKeys.group(this.group), RedisKeys.groupStartDate).orNull();
            if(currentDateMillis != null) {
                if(new Duration(new DateTime(currentDateMillis), new DateTime()).getStandardDays() > maxDays) {
                    return false;
                }
            }

            long current = redisClient.hgetlong(RedisKeys.group(this.group), RedisKeys.groupStart).get();

            if(current <= this.groupInfo.low) {
                return false;
            }

            redisClient.publish("groupactivity", this.group + ":b");

            long start = Math.max(current - this.numPosts, this.groupInfo.low);

            Crawler.Result result = this.crawler.crawl(this.group, start, current);

            // Push any missing articles into missing list.
            Pipeline p = redisClient.pipelined();
            for(Long missing : result.missingArticles) {
                p.lpush(RedisKeys.missing(this.group), Long.toString(missing));
            }
            p.sync();

            redisClient.hset(RedisKeys.group(this.group), RedisKeys.groupStartDate, result.dateRange.getStart().getMillis());
            redisClient.hset(RedisKeys.group(this.group), RedisKeys.groupStart, start);
            redisClient.publish("groupupdates", this.group);
            redisClient.publish("groupactivity", this.group + ":!b");

            return start > this.groupInfo.low;
        }
    }
}
