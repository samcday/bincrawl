package au.com.samcday.bincrawl.dao;

import au.com.samcday.bincrawl.RedisKeys;
import au.com.samcday.bincrawl.pool.BetterJedisPool;
import au.com.samcday.bincrawl.pool.PooledJedis;
import au.com.samcday.jnntp.Overview;
import au.com.samcday.jnntp.Xref;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Transaction;

import java.util.Date;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class BinaryDaoRedisImplTest {
    private PooledJedis mockJedis;
    private BinaryDaoRedisImpl impl;

    @Before
    public void setUp() throws Exception {
        BetterJedisPool mockJedisPool = mock(BetterJedisPool.class);
        this.mockJedis = mock(PooledJedis.class);
        when(mockJedisPool.get()).thenReturn(this.mockJedis);
        this.impl = new BinaryDaoRedisImpl(mockJedisPool, new ObjectMapper());
    }

    @Test
    public void testCreateNewBinary() {
        Transaction mockTransaction = mock(Transaction.class);
        when(this.mockJedis.multi()).thenReturn(mockTransaction);

        when(mockTransaction.exec()).thenReturn(ImmutableList.<Object>of(1));

        Overview overview = new Overview() {
            @Override
            public Date getDate() {
                return new Date();
            }
        };
        String binaryHash = this.impl.createOrUpdateBinary("alt.test", "Test subject.", 100, overview);

        verify(this.mockJedis).exists(RedisKeys.binary(binaryHash));
        verify(this.mockJedis).watch(RedisKeys.binary(binaryHash));
        verify(mockTransaction).hmset(eq(RedisKeys.binary(binaryHash)), anyMap());
        verify(this.mockJedis).sadd(RedisKeys.binaryGroups(binaryHash), "alt.test");
    }

    @Test
    public void testUpdateBinary() {
        Overview overview = new Overview() {
            @Override
            public Date getDate() {
                return new Date();
            }
        };

        when(this.mockJedis.exists(RedisKeys.binary(anyString()))).thenReturn(true);

        String binaryHash = this.impl.createOrUpdateBinary("alt.test", "Test subject.", 100, overview);
        verify(this.mockJedis, never()).watch(RedisKeys.binary(binaryHash));
    }

    @Test
    public void testUpdateBinaryXref() {
        Overview overview = new Overview() {
            @Override
            public Date getDate() {
                return new Date();
            }

            @Override
            public Xref getXref() {
                return Xref.parse("dummy.server alt.other.group:1234");
            }
        };

        when(this.mockJedis.exists(RedisKeys.binary(anyString()))).thenReturn(true);
        String binaryHash = this.impl.createOrUpdateBinary("alt.test", "Test subject.", 100, overview);
        verify(this.mockJedis).sadd(RedisKeys.binaryGroups(binaryHash), "alt.test");
        verify(this.mockJedis).sadd(RedisKeys.binaryGroups(binaryHash), "alt.other.group");
    }
}
