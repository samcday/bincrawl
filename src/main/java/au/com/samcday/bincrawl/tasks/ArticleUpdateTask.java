package au.com.samcday.bincrawl.tasks;

import au.com.samcday.bincrawl.pool.BetterJedisPool;
import au.com.samcday.bincrawl.pool.NntpClientPool;
import au.com.samcday.bincrawl.pool.PooledJedis;
import au.com.samcday.bincrawl.pool.PooledNntpClient;
import au.com.samcday.jnntp.GroupInfo;
import com.google.inject.Inject;

import java.util.concurrent.Callable;

/**
 * Handles crawling a number of posts from specified group. Returns true if there's more articles to be updated.
 */
public class ArticleUpdateTask implements Callable<Boolean> {
    private BetterJedisPool redisPool;
    private NntpClientPool nntpClientPool;
    private final int numPosts = 20000;         // TODO: make this configurable
    private String group;
    private GroupInfo groupInfo;

    @Inject
    public ArticleUpdateTask(BetterJedisPool redisPool, NntpClientPool nntpClientPool) {
        this.redisPool = redisPool;
        this.nntpClientPool = nntpClientPool;
    }

    public void configure(String group, GroupInfo groupInfo, boolean backfill) {
        this.group = group;
        this.groupInfo = groupInfo;
    }

    @Override
    public Boolean call() throws Exception {
        try (PooledJedis redisClient = this.redisPool.get(); PooledNntpClient nntpClient = this.nntpClientPool.borrow()) {

        }

        return true;
    }
}
