package au.com.samcday.bincrawl.pool;

import org.apache.commons.pool.impl.GenericObjectPool;
import redis.clients.jedis.*;

/**
 * Better because it returns a wrapped Redis client that implements AutoCloseable for fun and JDK7 TWR profitz.
 */
public class BetterJedisPool extends JedisPool {
    public BetterJedisPool(GenericObjectPool.Config poolConfig, String host) {
        super(poolConfig, host);
    }

    public BetterJedisPool(String host, int port) {
        super(host, port);
    }

    public BetterJedisPool(GenericObjectPool.Config poolConfig, String host, int port, int timeout, String password) {
        super(poolConfig, host, port, timeout, password);
    }

    public BetterJedisPool(GenericObjectPool.Config poolConfig, String host, int port) {
        super(poolConfig, host, port);
    }

    public BetterJedisPool(GenericObjectPool.Config poolConfig, String host, int port, int timeout) {
        super(poolConfig, host, port, timeout);
    }

    public PooledJedis get() {
        return new PooledJedis(this, super.getResource());
    }

}
