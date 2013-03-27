package au.com.samcday.bincrawl.dao;

import au.com.samcday.bincrawl.pool.BetterJedisPool;
import au.com.samcday.bincrawl.pool.PooledJedis;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    /*@Test
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
    }*/
}
