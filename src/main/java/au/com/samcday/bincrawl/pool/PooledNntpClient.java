package au.com.samcday.bincrawl.pool;

import au.com.samcday.jnntp.GroupInfo;
import au.com.samcday.jnntp.GroupListItem;
import au.com.samcday.jnntp.NntpClient;
import au.com.samcday.jnntp.OverviewList;
import au.com.samcday.jnntp.bandwidth.BandwidthHandler;
import au.com.samcday.jnntp.bandwidth.HandlerRegistration;
import au.com.samcday.jnntp.exceptions.NntpClientAuthenticationException;
import au.com.samcday.jnntp.exceptions.NntpClientConnectionError;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

public class PooledNntpClient implements NntpClient, AutoCloseable {
    private NntpClient decorated;
    private NntpClientPool nntpClientPool;

    public PooledNntpClient(NntpClientPool nntpClientPool, NntpClient decorated) {
        this.decorated = decorated;
        this.nntpClientPool = nntpClientPool;
    }

    @Override
    public void close() throws Exception {
        nntpClientPool.returnObject(this.decorated);
    }

    @Override
    public void connect() throws NntpClientConnectionError {
        decorated.connect();
    }

    @Override
    public void disconnect() {
        decorated.disconnect();
    }

    @Override
    public void authenticate(String username, String password) throws NntpClientAuthenticationException {
        decorated.authenticate(username, password);
    }

    @Override
    public Date date() {
        return decorated.date();
    }

    @Override
    public List<GroupListItem> list() {
        return decorated.list();
    }

    @Override
    public GroupInfo group(String name) {
        return decorated.group(name);
    }

    @Override
    public OverviewList overview(long start, long end) {
        return decorated.overview(start, end);
    }

    @Override
    public InputStream body(String messageId) {
        return decorated.body(messageId);
    }

    @Override
    public HandlerRegistration registerBandwidthHandler(BandwidthHandler handler) {
        return decorated.registerBandwidthHandler(handler);
    }
}
