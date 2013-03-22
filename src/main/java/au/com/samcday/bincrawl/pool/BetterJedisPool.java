package au.com.samcday.bincrawl.pool;

import com.google.common.base.Throwables;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import redis.clients.jedis.*;

/**
 * Better because it returns a wrapped Redis client that implements AutoCloseable for fun and JDK7 TWR profitz.
 */
public class BetterJedisPool {
    private GenericObjectPool<Jedis> pool;

    private static final ThreadLocal<RefCountJedis> JEDIS_THREAD_LOCAL = new ThreadLocal<RefCountJedis>() {
        @Override
        protected RefCountJedis initialValue() {
            return new RefCountJedis();
        }
    };

    public BetterJedisPool(GenericObjectPool.Config poolConfig, String host) {
        this.pool = new GenericObjectPool<Jedis>(new JedisFactory(host, Protocol.DEFAULT_PORT, Protocol.DEFAULT_TIMEOUT, null, Protocol.DEFAULT_DATABASE), poolConfig);
    }

    public void returnResource(Jedis resource) {
        if(JEDIS_THREAD_LOCAL.get().unref()) {
            try {
                pool.returnObject(resource);
            }
            catch(Exception e) {
                Throwables.propagate(e);
            }
        }
    }

    public PooledJedis get() {
        return JEDIS_THREAD_LOCAL.get().ref(this);
    }

    public int getNumActive() {
        return this.pool.getNumActive();
    }

    public int getNumIdle() {
        return this.pool.getNumIdle();
    }

    public void setMaxActive(int maxActive) {
        this.pool.setMaxActive(maxActive);
    }

    private static class RefCountJedis {
        int refCount = 0;
        PooledJedis inst;
        Jedis decorated;

        public PooledJedis ref(BetterJedisPool pool) {
            if(this.refCount++ == 0) {
                try {
                    this.decorated = pool.pool.borrowObject();
                }
                catch(Exception e) {
                    Throwables.propagate(e);
                }
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

    private static class JedisFactory extends BasePoolableObjectFactory {
        private final String host;
        private final int port;
        private final int timeout;
        private final String password;
        private final int database;

        public JedisFactory(final String host, final int port,
                            final int timeout, final String password, final int database) {
            super();
            this.host = host;
            this.port = port;
            this.timeout = timeout;
            this.password = password;
            this.database = database;
        }

        public Object makeObject() throws Exception {
            final Jedis jedis = new Jedis(this.host, this.port, this.timeout);

            jedis.connect();
            if (null != this.password) {
                jedis.auth(this.password);
            }
            if( database != 0 ) {
                jedis.select(database);
            }

            return jedis;
        }

        public void destroyObject(final Object obj) throws Exception {
            if (obj instanceof Jedis) {
                final Jedis jedis = (Jedis) obj;
                if (jedis.isConnected()) {
                    try {
                        try {
                            jedis.quit();
                        } catch (Exception e) {
                        }
                        jedis.disconnect();
                    } catch (Exception e) {

                    }
                }
            }
        }

        public boolean validateObject(final Object obj) {
            if (obj instanceof Jedis) {
                final Jedis jedis = (Jedis) obj;
                try {
                    return jedis.isConnected() && jedis.ping().equals("PONG");
                } catch (final Exception e) {
                    return false;
                }
            } else {
                return false;
            }
        }
    }
}
