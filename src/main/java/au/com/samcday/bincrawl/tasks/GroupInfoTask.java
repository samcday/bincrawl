package au.com.samcday.bincrawl.tasks;

import au.com.samcday.bincrawl.Group;
import au.com.samcday.bincrawl.pool.NntpClientPool;
import au.com.samcday.bincrawl.pool.PooledNntpClient;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class GroupInfoTask implements Callable<Group> {
    private static final Logger LOG = LoggerFactory.getLogger(GroupInfoTask.class);

    private NntpClientPool nntpClientPool;
    private String group;

    @Inject
    public GroupInfoTask(NntpClientPool nntpClientPool) {
        this.nntpClientPool = nntpClientPool;
    }

    public GroupInfoTask configure(String group) {
        this.group = group;
        return this;
    }

    @Override
    public Group call() throws Exception {
        try(PooledNntpClient nntpClient = this.nntpClientPool.borrow()) {
            LOG.info("Getting group info for {}", this.group);
            return Group.of(this.group, nntpClient.group(this.group));
        }
    }
}
