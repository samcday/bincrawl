package au.com.samcday.bincrawl.pool;

import org.apache.commons.pool.impl.GenericObjectPool;
import redis.clients.jedis.*;

/**
 * Better because it returns a wrapped Redis client that implements AutoCloseable for fun and JDK7 TWR profitz.
 */
public class BetterJedisPool extends JedisPool {
    private static final ThreadLocal<RefCountJedis> JEDIS_THREAD_LOCAL = new ThreadLocal<RefCountJedis>() {
        @Override
        protected RefCountJedis initialValue() {
            return new RefCountJedis();
        }
    };

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

    @Override
    public void returnResource(Jedis resource) {
        if(JEDIS_THREAD_LOCAL.get().unref()) {
            super.returnResource(resource);
        }
    }

    public PooledJedis get() {
        return JEDIS_THREAD_LOCAL.get().ref(this);
    }

    private static class RefCountJedis {
        int refCount = 0;
        PooledJedis inst;
        Jedis decorated;

        public PooledJedis ref(BetterJedisPool pool) {
            if(this.refCount++ == 0) {
                this.decorated = pool.getResource();
                this.inst = new PooledJedis(pool, decorated);
            }

            return inst;
        }

        public boolean unref() {
            if(--this.refCount == 0) {
                this.inst = null;
                this.decorated = null;
                return true;
            }

            return false;
        }
    }
}
