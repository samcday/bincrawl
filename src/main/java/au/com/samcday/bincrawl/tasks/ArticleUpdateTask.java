package au.com.samcday.bincrawl.tasks;

import au.com.samcday.bincrawl.Crawler;
import au.com.samcday.bincrawl.NntpWorkPool;
import au.com.samcday.bincrawl.RedisKeys;
import au.com.samcday.bincrawl.pool.BetterJedisPool;
import au.com.samcday.bincrawl.pool.PooledJedis;
import au.com.samcday.jnntp.GroupInfo;
import au.com.samcday.jnntp.NntpClient;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Pipeline;

/**
 * Handles crawling a number of posts from specified group. Returns true if there's more articles to be updated.
 */
public class ArticleUpdateTask implements NntpWorkPool.NntpCallable<Boolean> {
    private static final Logger LOG = LoggerFactory.getLogger(ArticleUpdateTask.class);

    private BetterJedisPool redisPool;
    private Crawler crawler;
    private final int numPosts = 20000;         // TODO: make this configurable
    private String group;
    private GroupInfo groupInfo;

    @Inject
    public ArticleUpdateTask(BetterJedisPool redisPool, Crawler crawler) {
        this.redisPool = redisPool;
        this.crawler = crawler;
    }

    public void configure(String group, GroupInfo groupInfo) {
        this.group = group;
        this.groupInfo = groupInfo;
    }

    @Override
    public Boolean call(NntpClient nntpClient) throws Exception {
        try (PooledJedis redisClient = this.redisPool.get()) {
            long current = redisClient.hgetlong(RedisKeys.group(this.group), RedisKeys.groupEnd).or(this.groupInfo.high);
            LOG.info("Apparently I'm up to article {} for group {}", current, this.group);
            long end;
            if(current + 1 < this.groupInfo.high) {
                redisClient.publish("groupactivity", this.group + ":u");
                long start = current + 1;
                end = Math.min(start + this.numPosts, this.groupInfo.high);

                Crawler.Result result = this.crawler.crawl(nntpClient, this.group, current, end);

                // Push any missing articles into missing list.
                Pipeline p = redisClient.pipelined();
                for(Long missing : result.missingArticles) {
                    p.lpush(RedisKeys.missing(this.group), Long.toString(missing));
                }
                p.sync();

                redisClient.hset(RedisKeys.group(this.group), RedisKeys.groupEndDate, result.dateRange.getEnd().getMillis());
                redisClient.publish("groupactivity", this.group + ":!u");
            }
            else {
                end = current;
                LOG.info("Nothing to crawl for group {}", this.group);
            }

            redisClient.hset(RedisKeys.group(this.group), RedisKeys.groupEnd, end);
            redisClient.publish("groupupdates", this.group);

            return current < end;
        }
    }
}
