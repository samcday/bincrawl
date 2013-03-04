package au.com.samcday.bincrawl.tasks;

import au.com.samcday.bincrawl.Crawler;
import au.com.samcday.bincrawl.RedisBinaryPartProcessor;
import au.com.samcday.bincrawl.RedisKeys;
import au.com.samcday.bincrawl.pool.BetterJedisPool;
import au.com.samcday.bincrawl.pool.NntpClientPool;
import au.com.samcday.bincrawl.pool.PooledJedis;
import au.com.samcday.bincrawl.pool.PooledNntpClient;
import au.com.samcday.jnntp.GroupInfo;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Pipeline;

import java.util.concurrent.Callable;

/**
 * Handles crawling a number of posts from specified group. Returns true if there's more articles to be updated.
 */
public class ArticleUpdateTask implements Callable<Boolean> {
    private static final Logger LOG = LoggerFactory.getLogger(ArticleUpdateTask.class);

    private BetterJedisPool redisPool;
    private NntpClientPool nntpClientPool;
    private Crawler crawler;
    private RedisBinaryPartProcessor partProcessor;
    private final int numPosts = 20000;         // TODO: make this configurable
    private String group;
    private GroupInfo groupInfo;

    @Inject
    public ArticleUpdateTask(BetterJedisPool redisPool, NntpClientPool nntpClientPool, Crawler crawler,
            RedisBinaryPartProcessor partProcessor) {
        this.redisPool = redisPool;
        this.nntpClientPool = nntpClientPool;
        this.crawler = crawler;
        this.partProcessor = partProcessor;
    }

    public void configure(String group, GroupInfo groupInfo) {
        this.group = group;
        this.groupInfo = groupInfo;
    }

    @Override
    public Boolean call() throws Exception {
        try (PooledJedis redisClient = this.redisPool.get(); PooledNntpClient nntpClient = this.nntpClientPool.borrow()) {
            long current = redisClient.hgetlong(RedisKeys.group(this.group), RedisKeys.groupEnd).or(this.groupInfo.high);
            LOG.info("Apparently I'm up to article {} for group {}", current, this.group);
            long end;
            if(current < this.groupInfo.high) {
                long start = current;
                end = Math.min(start + this.numPosts, this.groupInfo.high);

                Crawler.Result result = this.crawler.crawl(this.partProcessor, this.group, current, end);

                // Push any missing articles into missing list.
                Pipeline p = redisClient.pipelined();
                for(Long missing : result.missingArticles) {
                    p.lpush(RedisKeys.missing(this.group), Long.toString(missing));
                }
                p.sync();
            }
            else {
                end = current;
            }

            redisClient.hset(RedisKeys.group(this.group), RedisKeys.groupEnd, end);

            return current < end;
        }
    }
}
