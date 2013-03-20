package au.com.samcday.bincrawl.pool;

import au.com.samcday.bincrawl.redis.BetterJedisCommands;
import com.google.common.base.Optional;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import redis.clients.jedis.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PooledJedis implements JedisCommands, BinaryJedisCommands, BetterJedisCommands, AutoCloseable {
    private Jedis decorated;
    private JedisPool jedisPool;

    public PooledJedis(BetterJedisPool jedisPool, Jedis decorated) {
        this.decorated = decorated;
        this.jedisPool = jedisPool;
    }

    @Override
    public void close() {
        jedisPool.returnResource(this.decorated);
    }

    @Override
    public Optional<Long> hgetlong(String key, String field) {
        String val = this.hget(key, field);
        return Optional.fromNullable(val != null ? Longs.tryParse(val) : null);
    }

    @Override
    public Optional<Integer> hgetint(String key, String field) {
        String val = this.hget(key, field);
        return Optional.fromNullable(val != null ? Ints.tryParse(val) : null);
    }

    @Override
    public Long hset(String key, String field, Long value) {
        return this.hset(key, field, Long.toString(value));
    }

    @Override
    public String set(byte[] bytes, byte[] bytes2) {
        return decorated.set(bytes, bytes2);
    }

    @Override
    public byte[] get(byte[] bytes) {
        return decorated.get(bytes);
    }

    @Override
    public Boolean exists(byte[] bytes) {
        return decorated.exists(bytes);
    }

    @Override
    public String type(byte[] bytes) {
        return decorated.type(bytes);
    }

    @Override
    public Long expire(byte[] bytes, int i) {
        return decorated.expire(bytes, i);
    }

    @Override
    public Long expireAt(byte[] bytes, long l) {
        return decorated.expireAt(bytes, l);
    }

    @Override
    public Long ttl(byte[] bytes) {
        return decorated.ttl(bytes);
    }

    @Override
    public byte[] getSet(byte[] bytes, byte[] bytes2) {
        return decorated.getSet(bytes, bytes2);
    }

    @Override
    public Long setnx(byte[] bytes, byte[] bytes2) {
        return decorated.setnx(bytes, bytes2);
    }

    @Override
    public String setex(byte[] bytes, int i, byte[] bytes2) {
        return decorated.setex(bytes, i, bytes2);
    }

    @Override
    public Long decrBy(byte[] bytes, long l) {
        return decorated.decrBy(bytes, l);
    }

    @Override
    public Long decr(byte[] bytes) {
        return decorated.decr(bytes);
    }

    @Override
    public Long incrBy(byte[] bytes, long l) {
        return decorated.incrBy(bytes, l);
    }

    @Override
    public Long incr(byte[] bytes) {
        return decorated.incr(bytes);
    }

    @Override
    public Long append(byte[] bytes, byte[] bytes2) {
        return decorated.append(bytes, bytes2);
    }

    @Override
    public byte[] substr(byte[] bytes, int i, int i2) {
        return decorated.substr(bytes, i, i2);
    }

    @Override
    public Long hset(byte[] bytes, byte[] bytes2, byte[] bytes3) {
        return decorated.hset(bytes, bytes2, bytes3);
    }

    @Override
    public byte[] hget(byte[] bytes, byte[] bytes2) {
        return decorated.hget(bytes, bytes2);
    }

    @Override
    public Long hsetnx(byte[] bytes, byte[] bytes2, byte[] bytes3) {
        return decorated.hsetnx(bytes, bytes2, bytes3);
    }

    @Override
    public String hmset(byte[] bytes, Map<byte[], byte[]> map) {
        return decorated.hmset(bytes, map);
    }

    @Override
    public List<byte[]> hmget(byte[] bytes, byte[]... bytes2) {
        return decorated.hmget(bytes, bytes2);
    }

    @Override
    public Long hincrBy(byte[] bytes, byte[] bytes2, long l) {
        return decorated.hincrBy(bytes, bytes2, l);
    }

    @Override
    public Boolean hexists(byte[] bytes, byte[] bytes2) {
        return decorated.hexists(bytes, bytes2);
    }

    @Override
    public Long hdel(byte[] bytes, byte[]... bytes2) {
        return decorated.hdel(bytes, bytes2);
    }

    @Override
    public Long hlen(byte[] bytes) {
        return decorated.hlen(bytes);
    }

    @Override
    public Set<byte[]> hkeys(byte[] bytes) {
        return decorated.hkeys(bytes);
    }

    @Override
    public Collection<byte[]> hvals(byte[] bytes) {
        return decorated.hvals(bytes);
    }

    @Override
    public Map<byte[], byte[]> hgetAll(byte[] bytes) {
        return decorated.hgetAll(bytes);
    }

    @Override
    public Long rpush(byte[] bytes, byte[]... bytes2) {
        return decorated.rpush(bytes, bytes2);
    }

    @Override
    public Long lpush(byte[] bytes, byte[]... bytes2) {
        return decorated.lpush(bytes, bytes2);
    }

    @Override
    public Long llen(byte[] bytes) {
        return decorated.llen(bytes);
    }

    @Override
    public List<byte[]> lrange(byte[] bytes, int i, int i2) {
        return decorated.lrange(bytes, i, i2);
    }

    @Override
    public String ltrim(byte[] bytes, int i, int i2) {
        return decorated.ltrim(bytes, i, i2);
    }

    @Override
    public byte[] lindex(byte[] bytes, int i) {
        return decorated.lindex(bytes, i);
    }

    @Override
    public String lset(byte[] bytes, int i, byte[] bytes2) {
        return decorated.lset(bytes, i, bytes2);
    }

    @Override
    public Long lrem(byte[] bytes, int i, byte[] bytes2) {
        return decorated.lrem(bytes, i, bytes2);
    }

    @Override
    public byte[] lpop(byte[] bytes) {
        return decorated.lpop(bytes);
    }

    @Override
    public byte[] rpop(byte[] bytes) {
        return decorated.rpop(bytes);
    }

    @Override
    public Long sadd(byte[] bytes, byte[]... bytes2) {
        return decorated.sadd(bytes, bytes2);
    }

    @Override
    public Set<byte[]> smembers(byte[] bytes) {
        return decorated.smembers(bytes);
    }

    @Override
    public Long srem(byte[] bytes, byte[]... bytes2) {
        return decorated.srem(bytes, bytes2);
    }

    @Override
    public byte[] spop(byte[] bytes) {
        return decorated.spop(bytes);
    }

    @Override
    public Long scard(byte[] bytes) {
        return decorated.scard(bytes);
    }

    @Override
    public Boolean sismember(byte[] bytes, byte[] bytes2) {
        return decorated.sismember(bytes, bytes2);
    }

    @Override
    public byte[] srandmember(byte[] bytes) {
        return decorated.srandmember(bytes);
    }

    @Override
    public Long zadd(byte[] bytes, double v, byte[] bytes2) {
        return decorated.zadd(bytes, v, bytes2);
    }

    @Override
    public Long zadd(byte[] bytes, Map<Double, byte[]> doubleMap) {
        return decorated.zadd(bytes, doubleMap);
    }

    @Override
    public Set<byte[]> zrange(byte[] bytes, int i, int i2) {
        return decorated.zrange(bytes, i, i2);
    }

    @Override
    public Long zrem(byte[] bytes, byte[]... bytes2) {
        return decorated.zrem(bytes, bytes2);
    }

    @Override
    public Double zincrby(byte[] bytes, double v, byte[] bytes2) {
        return decorated.zincrby(bytes, v, bytes2);
    }

    @Override
    public Long zrank(byte[] bytes, byte[] bytes2) {
        return decorated.zrank(bytes, bytes2);
    }

    @Override
    public Long zrevrank(byte[] bytes, byte[] bytes2) {
        return decorated.zrevrank(bytes, bytes2);
    }

    @Override
    public Set<byte[]> zrevrange(byte[] bytes, int i, int i2) {
        return decorated.zrevrange(bytes, i, i2);
    }

    @Override
    public Set<Tuple> zrangeWithScores(byte[] bytes, int i, int i2) {
        return decorated.zrangeWithScores(bytes, i, i2);
    }

    @Override
    public Set<Tuple> zrevrangeWithScores(byte[] bytes, int i, int i2) {
        return decorated.zrevrangeWithScores(bytes, i, i2);
    }

    @Override
    public Long zcard(byte[] bytes) {
        return decorated.zcard(bytes);
    }

    @Override
    public Double zscore(byte[] bytes, byte[] bytes2) {
        return decorated.zscore(bytes, bytes2);
    }

    @Override
    public List<byte[]> sort(byte[] bytes) {
        return decorated.sort(bytes);
    }

    @Override
    public List<byte[]> sort(byte[] bytes, SortingParams sortingParams) {
        return decorated.sort(bytes, sortingParams);
    }

    @Override
    public Long zcount(byte[] bytes, double v, double v2) {
        return decorated.zcount(bytes, v, v2);
    }

    @Override
    public Long zcount(byte[] bytes, byte[] bytes2, byte[] bytes3) {
        return decorated.zcount(bytes, bytes2, bytes3);
    }

    @Override
    public Set<byte[]> zrangeByScore(byte[] bytes, double v, double v2) {
        return decorated.zrangeByScore(bytes, v, v2);
    }

    @Override
    public Set<byte[]> zrangeByScore(byte[] bytes, double v, double v2, int i, int i2) {
        return decorated.zrangeByScore(bytes, v, v2, i, i2);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] bytes, double v, double v2) {
        return decorated.zrangeByScoreWithScores(bytes, v, v2);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] bytes, double v, double v2, int i, int i2) {
        return decorated.zrangeByScoreWithScores(bytes, v, v2, i, i2);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] bytes, byte[] bytes2, byte[] bytes3) {
        return decorated.zrangeByScoreWithScores(bytes, bytes2, bytes3);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] bytes, byte[] bytes2, byte[] bytes3, int i, int i2) {
        return decorated.zrangeByScoreWithScores(bytes, bytes2, bytes3, i, i2);
    }

    @Override
    public Set<byte[]> zrevrangeByScore(byte[] bytes, double v, double v2) {
        return decorated.zrevrangeByScore(bytes, v, v2);
    }

    @Override
    public Set<byte[]> zrevrangeByScore(byte[] bytes, double v, double v2, int i, int i2) {
        return decorated.zrevrangeByScore(bytes, v, v2, i, i2);
    }

    @Override
    public Set<byte[]> zrevrangeByScore(byte[] bytes, byte[] bytes2, byte[] bytes3) {
        return decorated.zrevrangeByScore(bytes, bytes2, bytes3);
    }

    @Override
    public Set<byte[]> zrevrangeByScore(byte[] bytes, byte[] bytes2, byte[] bytes3, int i, int i2) {
        return decorated.zrevrangeByScore(bytes, bytes2, bytes3, i, i2);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(byte[] bytes, double v, double v2) {
        return decorated.zrevrangeByScoreWithScores(bytes, v, v2);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(byte[] bytes, double v, double v2, int i, int i2) {
        return decorated.zrevrangeByScoreWithScores(bytes, v, v2, i, i2);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(byte[] bytes, byte[] bytes2, byte[] bytes3) {
        return decorated.zrevrangeByScoreWithScores(bytes, bytes2, bytes3);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(byte[] bytes, byte[] bytes2, byte[] bytes3, int i, int i2) {
        return decorated.zrevrangeByScoreWithScores(bytes, bytes2, bytes3, i, i2);
    }

    @Override
    public Long zremrangeByRank(byte[] bytes, int i, int i2) {
        return decorated.zremrangeByRank(bytes, i, i2);
    }

    @Override
    public Long zremrangeByScore(byte[] bytes, double v, double v2) {
        return decorated.zremrangeByScore(bytes, v, v2);
    }

    @Override
    public Long zremrangeByScore(byte[] bytes, byte[] bytes2, byte[] bytes3) {
        return decorated.zremrangeByScore(bytes, bytes2, bytes3);
    }

    @Override
    public Long linsert(byte[] bytes, BinaryClient.LIST_POSITION list_position, byte[] bytes2, byte[] bytes3) {
        return decorated.linsert(bytes, list_position, bytes2, bytes3);
    }

    @Override
    public Long objectRefcount(byte[] bytes) {
        return decorated.objectRefcount(bytes);
    }

    @Override
    public Long objectIdletime(byte[] bytes) {
        return decorated.objectIdletime(bytes);
    }

    @Override
    public byte[] objectEncoding(byte[] bytes) {
        return decorated.objectEncoding(bytes);
    }

    @Override
    public Long lpushx(byte[] bytes, byte[] bytes2) {
        return decorated.lpushx(bytes, bytes2);
    }

    @Override
    public Long rpushx(byte[] bytes, byte[] bytes2) {
        return decorated.rpushx(bytes, bytes2);
    }

    @Override
    public String set(String s, String s2) {
        return decorated.set(s, s2);
    }

    @Override
    public String get(String s) {
        return decorated.get(s);
    }

    @Override
    public Boolean exists(String s) {
        return decorated.exists(s);
    }

    @Override
    public String type(String s) {
        return decorated.type(s);
    }

    @Override
    public Long expire(String s, int i) {
        return decorated.expire(s, i);
    }

    @Override
    public Long expireAt(String s, long l) {
        return decorated.expireAt(s, l);
    }

    @Override
    public Long ttl(String s) {
        return decorated.ttl(s);
    }

    @Override
    public Boolean setbit(String s, long l, boolean b) {
        return decorated.setbit(s, l, b);
    }

    @Override
    public Boolean getbit(String s, long l) {
        return decorated.getbit(s, l);
    }

    @Override
    public Long setrange(String s, long l, String s2) {
        return decorated.setrange(s, l, s2);
    }

    @Override
    public String getrange(String s, long l, long l2) {
        return decorated.getrange(s, l, l2);
    }

    @Override
    public String getSet(String s, String s2) {
        return decorated.getSet(s, s2);
    }

    @Override
    public Long setnx(String s, String s2) {
        return decorated.setnx(s, s2);
    }

    @Override
    public String setex(String s, int i, String s2) {
        return decorated.setex(s, i, s2);
    }

    @Override
    public Long decrBy(String s, long l) {
        return decorated.decrBy(s, l);
    }

    @Override
    public Long decr(String s) {
        return decorated.decr(s);
    }

    @Override
    public Long incrBy(String s, long l) {
        return decorated.incrBy(s, l);
    }

    @Override
    public Long incr(String s) {
        return decorated.incr(s);
    }

    @Override
    public Long append(String s, String s2) {
        return decorated.append(s, s2);
    }

    @Override
    public String substr(String s, int i, int i2) {
        return decorated.substr(s, i, i2);
    }

    @Override
    public Long hset(String s, String s2, String s3) {
        return decorated.hset(s, s2, s3);
    }

    @Override
    public String hget(String s, String s2) {
        return decorated.hget(s, s2);
    }

    @Override
    public Long hsetnx(String s, String s2, String s3) {
        return decorated.hsetnx(s, s2, s3);
    }

    @Override
    public String hmset(String s, Map<String, String> stringStringMap) {
        return decorated.hmset(s, stringStringMap);
    }

    @Override
    public List<String> hmget(String s, String... strings) {
        return decorated.hmget(s, strings);
    }

    @Override
    public Long hincrBy(String s, String s2, long l) {
        return decorated.hincrBy(s, s2, l);
    }

    @Override
    public Boolean hexists(String s, String s2) {
        return decorated.hexists(s, s2);
    }

    @Override
    public Long hdel(String s, String... strings) {
        return decorated.hdel(s, strings);
    }

    @Override
    public Long hlen(String s) {
        return decorated.hlen(s);
    }

    @Override
    public Set<String> hkeys(String s) {
        return decorated.hkeys(s);
    }

    @Override
    public List<String> hvals(String s) {
        return decorated.hvals(s);
    }

    @Override
    public Map<String, String> hgetAll(String s) {
        return decorated.hgetAll(s);
    }

    @Override
    public Long rpush(String s, String... strings) {
        return decorated.rpush(s, strings);
    }

    @Override
    public Long lpush(String s, String... strings) {
        return decorated.lpush(s, strings);
    }

    @Override
    public Long llen(String s) {
        return decorated.llen(s);
    }

    @Override
    public List<String> lrange(String s, long l, long l2) {
        return decorated.lrange(s, l, l2);
    }

    @Override
    public String ltrim(String s, long l, long l2) {
        return decorated.ltrim(s, l, l2);
    }

    @Override
    public String lindex(String s, long l) {
        return decorated.lindex(s, l);
    }

    @Override
    public String lset(String s, long l, String s2) {
        return decorated.lset(s, l, s2);
    }

    @Override
    public Long lrem(String s, long l, String s2) {
        return decorated.lrem(s, l, s2);
    }

    @Override
    public String lpop(String s) {
        return decorated.lpop(s);
    }

    @Override
    public String rpop(String s) {
        return decorated.rpop(s);
    }

    @Override
    public Long sadd(String s, String... strings) {
        return decorated.sadd(s, strings);
    }

    @Override
    public Set<String> smembers(String s) {
        return decorated.smembers(s);
    }

    @Override
    public Long srem(String s, String... strings) {
        return decorated.srem(s, strings);
    }

    @Override
    public String spop(String s) {
        return decorated.spop(s);
    }

    @Override
    public Long scard(String s) {
        return decorated.scard(s);
    }

    @Override
    public Boolean sismember(String s, String s2) {
        return decorated.sismember(s, s2);
    }

    @Override
    public String srandmember(String s) {
        return decorated.srandmember(s);
    }

    @Override
    public Long zadd(String s, double v, String s2) {
        return decorated.zadd(s, v, s2);
    }

    @Override
    public Long zadd(String s, Map<Double, String> doubleStringMap) {
        return decorated.zadd(s, doubleStringMap);
    }

    @Override
    public Set<String> zrange(String s, long l, long l2) {
        return decorated.zrange(s, l, l2);
    }

    @Override
    public Long zrem(String s, String... strings) {
        return decorated.zrem(s, strings);
    }

    @Override
    public Double zincrby(String s, double v, String s2) {
        return decorated.zincrby(s, v, s2);
    }

    @Override
    public Long zrank(String s, String s2) {
        return decorated.zrank(s, s2);
    }

    @Override
    public Long zrevrank(String s, String s2) {
        return decorated.zrevrank(s, s2);
    }

    @Override
    public Set<String> zrevrange(String s, long l, long l2) {
        return decorated.zrevrange(s, l, l2);
    }

    @Override
    public Set<Tuple> zrangeWithScores(String s, long l, long l2) {
        return decorated.zrangeWithScores(s, l, l2);
    }

    @Override
    public Set<Tuple> zrevrangeWithScores(String s, long l, long l2) {
        return decorated.zrevrangeWithScores(s, l, l2);
    }

    @Override
    public Long zcard(String s) {
        return decorated.zcard(s);
    }

    @Override
    public Double zscore(String s, String s2) {
        return decorated.zscore(s, s2);
    }

    @Override
    public List<String> sort(String s) {
        return decorated.sort(s);
    }

    @Override
    public List<String> sort(String s, SortingParams sortingParams) {
        return decorated.sort(s, sortingParams);
    }

    @Override
    public Long zcount(String s, double v, double v2) {
        return decorated.zcount(s, v, v2);
    }

    @Override
    public Long zcount(String s, String s2, String s3) {
        return decorated.zcount(s, s2, s3);
    }

    @Override
    public Set<String> zrangeByScore(String s, double v, double v2) {
        return decorated.zrangeByScore(s, v, v2);
    }

    @Override
    public Set<String> zrangeByScore(String s, String s2, String s3) {
        return decorated.zrangeByScore(s, s2, s3);
    }

    @Override
    public Set<String> zrevrangeByScore(String s, double v, double v2) {
        return decorated.zrevrangeByScore(s, v, v2);
    }

    @Override
    public Set<String> zrangeByScore(String s, double v, double v2, int i, int i2) {
        return decorated.zrangeByScore(s, v, v2, i, i2);
    }

    @Override
    public Set<String> zrevrangeByScore(String s, String s2, String s3) {
        return decorated.zrevrangeByScore(s, s2, s3);
    }

    @Override
    public Set<String> zrangeByScore(String s, String s2, String s3, int i, int i2) {
        return decorated.zrangeByScore(s, s2, s3, i, i2);
    }

    @Override
    public Set<String> zrevrangeByScore(String s, double v, double v2, int i, int i2) {
        return decorated.zrevrangeByScore(s, v, v2, i, i2);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String s, double v, double v2) {
        return decorated.zrangeByScoreWithScores(s, v, v2);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String s, double v, double v2) {
        return decorated.zrevrangeByScoreWithScores(s, v, v2);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String s, double v, double v2, int i, int i2) {
        return decorated.zrangeByScoreWithScores(s, v, v2, i, i2);
    }

    @Override
    public Set<String> zrevrangeByScore(String s, String s2, String s3, int i, int i2) {
        return decorated.zrevrangeByScore(s, s2, s3, i, i2);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String s, String s2, String s3) {
        return decorated.zrangeByScoreWithScores(s, s2, s3);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String s, String s2, String s3) {
        return decorated.zrevrangeByScoreWithScores(s, s2, s3);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String s, String s2, String s3, int i, int i2) {
        return decorated.zrangeByScoreWithScores(s, s2, s3, i, i2);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String s, double v, double v2, int i, int i2) {
        return decorated.zrevrangeByScoreWithScores(s, v, v2, i, i2);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String s, String s2, String s3, int i, int i2) {
        return decorated.zrevrangeByScoreWithScores(s, s2, s3, i, i2);
    }

    @Override
    public Long zremrangeByRank(String s, long l, long l2) {
        return decorated.zremrangeByRank(s, l, l2);
    }

    @Override
    public Long zremrangeByScore(String s, double v, double v2) {
        return decorated.zremrangeByScore(s, v, v2);
    }

    @Override
    public Long zremrangeByScore(String s, String s2, String s3) {
        return decorated.zremrangeByScore(s, s2, s3);
    }

    @Override
    public Long linsert(String s, BinaryClient.LIST_POSITION list_position, String s2, String s3) {
        return decorated.linsert(s, list_position, s2, s3);
    }

    @Override
    public Long lpushx(String s, String s2) {
        return decorated.lpushx(s, s2);
    }

    @Override
    public Long rpushx(String s, String s2) {
        return decorated.rpushx(s, s2);
    }

    public Pipeline pipelined() {
        return decorated.pipelined();
    }

    public List<Object> pipelined(PipelineBlock jedisPipeline) {
        return decorated.pipelined(jedisPipeline);
    }

    public List<byte[]> blpop(int timeout, byte[]... keys) {
        return decorated.blpop(timeout, keys);
    }

    public List<byte[]> brpop(int timeout, byte[]... keys) {
        return decorated.brpop(timeout, keys);
    }

    public List<String> blpop(int timeout, String... keys) {
        return decorated.blpop(timeout, keys);
    }

    public List<String> brpop(int timeout, String... keys) {
        return decorated.brpop(timeout, keys);
    }

    public String brpopsingle(int timeout, String key) {
        List<String> result = this.brpop(timeout, key);
        if(result != null) {
            return result.get(1);
        }
        return null;
    }

    public Long del(String... keys) {
        return decorated.del(keys);
    }

    public Long publish(String channel, String message) {
        return decorated.publish(channel, message);
    }

    public String watch(final String... keys) {
        return decorated.watch(keys);
    }

    public Transaction multi() {
        return decorated.multi();
    }

    public Set<String> keys(String pattern) {
        return decorated.keys(pattern);
    }
}
