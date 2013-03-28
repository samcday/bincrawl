package au.com.samcday.bincrawl.tasks;

import au.com.samcday.bincrawl.Group;
import au.com.samcday.bincrawl.NntpWorkPool;
import au.com.samcday.jnntp.NntpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupInfoTask implements NntpWorkPool.NntpCallable<Group> {
    private static final Logger LOG = LoggerFactory.getLogger(GroupInfoTask.class);

    private String group;

    public GroupInfoTask configure(String group) {
        this.group = group;
        return this;
    }

    @Override
    public Group call(NntpClient nntpClient) throws Exception {
        LOG.info("Getting group info for {}", this.group);
        return Group.of(this.group, nntpClient.group(this.group));
    }
}
