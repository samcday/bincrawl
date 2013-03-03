package au.com.samcday.bincrawl.pool;

import redis.clients.jedis.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PooledJedis implements JedisCommands, BinaryJedisCommands, AutoCloseable {
    private Jedis decorated;
    private JedisPool jedisPool;

    public PooledJedis(BetterJedisPool jedisPool, Jedis decorated) {
        this.decorated = decorated;
        this.jedisPool = jedisPool;
    }

    @Override
    public void close() throws Exception {
        jedisPool.returnResource(this.decorated);
    }

    @Override
    public String set(String key, String value) {
        return decorated.set(key, value);
    }

    @Override
    public String get(String key) {
        return decorated.get(key);
    }

    @Override
    public Boolean exists(String key) {
        return decorated.exists(key);
    }

    @Override
    public String type(String key) {
        return decorated.type(key);
    }

    @Override
    public Long expire(String key, int seconds) {
        return decorated.expire(key, seconds);
    }

    @Override
    public Long expireAt(String key, long unixTime) {
        return decorated.expireAt(key, unixTime);
    }

    @Override
    public Long ttl(String key) {
        return decorated.ttl(key);
    }

    @Override
    public boolean setbit(String key, long offset, boolean value) {
        return decorated.setbit(key, offset, value);
    }

    @Override
    public boolean getbit(String key, long offset) {
        return decorated.getbit(key, offset);
    }

    @Override
    public long setrange(String key, long offset, String value) {
        return decorated.setrange(key, offset, value);
    }

    @Override
    public String getrange(String key, long startOffset, long endOffset) {
        return decorated.getrange(key, startOffset, endOffset);
    }

    @Override
    public String getSet(String key, String value) {
        return decorated.getSet(key, value);
    }

    @Override
    public Long setnx(String key, String value) {
        return decorated.setnx(key, value);
    }

    @Override
    public String setex(String key, int seconds, String value) {
        return decorated.setex(key, seconds, value);
    }

    @Override
    public Long decrBy(String key, long integer) {
        return decorated.decrBy(key, integer);
    }

    @Override
    public Long decr(String key) {
        return decorated.decr(key);
    }

    @Override
    public Long incrBy(String key, long integer) {
        return decorated.incrBy(key, integer);
    }

    @Override
    public Long incr(String key) {
        return decorated.incr(key);
    }

    @Override
    public Long append(String key, String value) {
        return decorated.append(key, value);
    }

    @Override
    public String substr(String key, int start, int end) {
        return decorated.substr(key, start, end);
    }

    @Override
    public Long hset(String key, String field, String value) {
        return decorated.hset(key, field, value);
    }

    @Override
    public String hget(String key, String field) {
        return decorated.hget(key, field);
    }

    @Override
    public Long hsetnx(String key, String field, String value) {
        return decorated.hsetnx(key, field, value);
    }

    @Override
    public String hmset(String key, Map<String, String> hash) {
        return decorated.hmset(key, hash);
    }

    @Override
    public List<String> hmget(String key, String... fields) {
        return decorated.hmget(key, fields);
    }

    @Override
    public Long hincrBy(String key, String field, long value) {
        return decorated.hincrBy(key, field, value);
    }

    @Override
    public Boolean hexists(String key, String field) {
        return decorated.hexists(key, field);
    }

    @Override
    public Long hdel(String key, String field) {
        return decorated.hdel(key, field);
    }

    @Override
    public Long hlen(String key) {
        return decorated.hlen(key);
    }

    @Override
    public Set<String> hkeys(String key) {
        return decorated.hkeys(key);
    }

    @Override
    public List<String> hvals(String key) {
        return decorated.hvals(key);
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        return decorated.hgetAll(key);
    }

    @Override
    public Long rpush(String key, String string) {
        return decorated.rpush(key, string);
    }

    @Override
    public Long lpush(String key, String string) {
        return decorated.lpush(key, string);
    }

    @Override
    public Long llen(String key) {
        return decorated.llen(key);
    }

    @Override
    public List<String> lrange(String key, long start, long end) {
        return decorated.lrange(key, start, end);
    }

    @Override
    public String ltrim(String key, long start, long end) {
        return decorated.ltrim(key, start, end);
    }

    @Override
    public String lindex(String key, long index) {
        return decorated.lindex(key, index);
    }

    @Override
    public String lset(String key, long index, String value) {
        return decorated.lset(key, index, value);
    }

    @Override
    public Long lrem(String key, long count, String value) {
        return decorated.lrem(key, count, value);
    }

    @Override
    public String lpop(String key) {
        return decorated.lpop(key);
    }

    @Override
    public String rpop(String key) {
        return decorated.rpop(key);
    }

    @Override
    public Long sadd(String key, String member) {
        return decorated.sadd(key, member);
    }

    @Override
    public Set<String> smembers(String key) {
        return decorated.smembers(key);
    }

    @Override
    public Long srem(String key, String member) {
        return decorated.srem(key, member);
    }

    @Override
    public String spop(String key) {
        return decorated.spop(key);
    }

    @Override
    public Long scard(String key) {
        return decorated.scard(key);
    }

    @Override
    public Boolean sismember(String key, String member) {
        return decorated.sismember(key, member);
    }

    @Override
    public String srandmember(String key) {
        return decorated.srandmember(key);
    }

    @Override
    public Long zadd(String key, double score, String member) {
        return decorated.zadd(key, score, member);
    }

    @Override
    public Set<String> zrange(String key, int start, int end) {
        return decorated.zrange(key, start, end);
    }

    @Override
    public Long zrem(String key, String member) {
        return decorated.zrem(key, member);
    }

    @Override
    public Double zincrby(String key, double score, String member) {
        return decorated.zincrby(key, score, member);
    }

    @Override
    public Long zrank(String key, String member) {
        return decorated.zrank(key, member);
    }

    @Override
    public Long zrevrank(String key, String member) {
        return decorated.zrevrank(key, member);
    }

    @Override
    public Set<String> zrevrange(String key, int start, int end) {
        return decorated.zrevrange(key, start, end);
    }

    @Override
    public Set<Tuple> zrangeWithScores(String key, int start, int end) {
        return decorated.zrangeWithScores(key, start, end);
    }

    @Override
    public Set<Tuple> zrevrangeWithScores(String key, int start, int end) {
        return decorated.zrevrangeWithScores(key, start, end);
    }

    @Override
    public Long zcard(String key) {
        return decorated.zcard(key);
    }

    @Override
    public Double zscore(String key, String member) {
        return decorated.zscore(key, member);
    }

    @Override
    public List<String> sort(String key) {
        return decorated.sort(key);
    }

    @Override
    public List<String> sort(String key, SortingParams sortingParameters) {
        return decorated.sort(key, sortingParameters);
    }

    @Override
    public Long zcount(String key, double min, double max) {
        return decorated.zcount(key, min, max);
    }

    @Override
    public Set<String> zrangeByScore(String key, double min, double max) {
        return decorated.zrangeByScore(key, min, max);
    }

    @Override
    public Set<String> zrevrangeByScore(String key, double max, double min) {
        return decorated.zrevrangeByScore(key, max, min);
    }

    @Override
    public Set<String> zrangeByScore(String key, double min, double max, int offset, int count) {
        return decorated.zrangeByScore(key, min, max, offset, count);
    }

    @Override
    public Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count) {
        return decorated.zrevrangeByScore(key, max, min, offset, count);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
        return decorated.zrangeByScoreWithScores(key, min, max);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
        return decorated.zrevrangeByScoreWithScores(key, max, min);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
        return decorated.zrangeByScoreWithScores(key, min, max, offset, count);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) {
        return decorated.zrevrangeByScoreWithScores(key, max, min, offset, count);
    }

    @Override
    public Long zremrangeByRank(String key, int start, int end) {
        return decorated.zremrangeByRank(key, start, end);
    }

    @Override
    public Long zremrangeByScore(String key, double start, double end) {
        return decorated.zremrangeByScore(key, start, end);
    }

    @Override
    public Long linsert(String key, BinaryClient.LIST_POSITION where, String pivot, String value) {
        return decorated.linsert(key, where, pivot, value);
    }

    @Override
    public String set(byte[] key, byte[] value) {
        return decorated.set(key, value);
    }

    @Override
    public byte[] get(byte[] key) {
        return decorated.get(key);
    }

    @Override
    public Boolean exists(byte[] key) {
        return decorated.exists(key);
    }

    @Override
    public String type(byte[] key) {
        return decorated.type(key);
    }

    @Override
    public Long expire(byte[] key, int seconds) {
        return decorated.expire(key, seconds);
    }

    @Override
    public Long expireAt(byte[] key, long unixTime) {
        return decorated.expireAt(key, unixTime);
    }

    @Override
    public Long ttl(byte[] key) {
        return decorated.ttl(key);
    }

    @Override
    public byte[] getSet(byte[] key, byte[] value) {
        return decorated.getSet(key, value);
    }

    @Override
    public Long setnx(byte[] key, byte[] value) {
        return decorated.setnx(key, value);
    }

    @Override
    public String setex(byte[] key, int seconds, byte[] value) {
        return decorated.setex(key, seconds, value);
    }

    @Override
    public Long decrBy(byte[] key, long integer) {
        return decorated.decrBy(key, integer);
    }

    @Override
    public Long decr(byte[] key) {
        return decorated.decr(key);
    }

    @Override
    public Long incrBy(byte[] key, long integer) {
        return decorated.incrBy(key, integer);
    }

    @Override
    public Long incr(byte[] key) {
        return decorated.incr(key);
    }

    @Override
    public Long append(byte[] key, byte[] value) {
        return decorated.append(key, value);
    }

    @Override
    public byte[] substr(byte[] key, int start, int end) {
        return decorated.substr(key, start, end);
    }

    @Override
    public Long hset(byte[] key, byte[] field, byte[] value) {
        return decorated.hset(key, field, value);
    }

    @Override
    public byte[] hget(byte[] key, byte[] field) {
        return decorated.hget(key, field);
    }

    @Override
    public Long hsetnx(byte[] key, byte[] field, byte[] value) {
        return decorated.hsetnx(key, field, value);
    }

    @Override
    public String hmset(byte[] key, Map<byte[], byte[]> hash) {
        return decorated.hmset(key, hash);
    }

    @Override
    public List<byte[]> hmget(byte[] key, byte[]... fields) {
        return decorated.hmget(key, fields);
    }

    @Override
    public Long hincrBy(byte[] key, byte[] field, long value) {
        return decorated.hincrBy(key, field, value);
    }

    @Override
    public Boolean hexists(byte[] key, byte[] field) {
        return decorated.hexists(key, field);
    }

    @Override
    public Long hdel(byte[] key, byte[] field) {
        return decorated.hdel(key, field);
    }

    @Override
    public Long hlen(byte[] key) {
        return decorated.hlen(key);
    }

    @Override
    public Set<byte[]> hkeys(byte[] key) {
        return decorated.hkeys(key);
    }

    @Override
    public Collection<byte[]> hvals(byte[] key) {
        return decorated.hvals(key);
    }

    @Override
    public Map<byte[], byte[]> hgetAll(byte[] key) {
        return decorated.hgetAll(key);
    }

    @Override
    public Long rpush(byte[] key, byte[] string) {
        return decorated.rpush(key, string);
    }

    @Override
    public Long lpush(byte[] key, byte[] string) {
        return decorated.lpush(key, string);
    }

    @Override
    public Long llen(byte[] key) {
        return decorated.llen(key);
    }

    @Override
    public List<byte[]> lrange(byte[] key, int start, int end) {
        return decorated.lrange(key, start, end);
    }

    @Override
    public String ltrim(byte[] key, int start, int end) {
        return decorated.ltrim(key, start, end);
    }

    @Override
    public byte[] lindex(byte[] key, int index) {
        return decorated.lindex(key, index);
    }

    @Override
    public String lset(byte[] key, int index, byte[] value) {
        return decorated.lset(key, index, value);
    }

    @Override
    public Long lrem(byte[] key, int count, byte[] value) {
        return decorated.lrem(key, count, value);
    }

    @Override
    public byte[] lpop(byte[] key) {
        return decorated.lpop(key);
    }

    @Override
    public byte[] rpop(byte[] key) {
        return decorated.rpop(key);
    }

    @Override
    public Long sadd(byte[] key, byte[] member) {
        return decorated.sadd(key, member);
    }

    @Override
    public Set<byte[]> smembers(byte[] key) {
        return decorated.smembers(key);
    }

    @Override
    public Long srem(byte[] key, byte[] member) {
        return decorated.srem(key, member);
    }

    @Override
    public byte[] spop(byte[] key) {
        return decorated.spop(key);
    }

    @Override
    public Long scard(byte[] key) {
        return decorated.scard(key);
    }

    @Override
    public Boolean sismember(byte[] key, byte[] member) {
        return decorated.sismember(key, member);
    }

    @Override
    public byte[] srandmember(byte[] key) {
        return decorated.srandmember(key);
    }

    @Override
    public Long zadd(byte[] key, double score, byte[] member) {
        return decorated.zadd(key, score, member);
    }

    @Override
    public Set<byte[]> zrange(byte[] key, int start, int end) {
        return decorated.zrange(key, start, end);
    }

    @Override
    public Long zrem(byte[] key, byte[] member) {
        return decorated.zrem(key, member);
    }

    @Override
    public Double zincrby(byte[] key, double score, byte[] member) {
        return decorated.zincrby(key, score, member);
    }

    @Override
    public Long zrank(byte[] key, byte[] member) {
        return decorated.zrank(key, member);
    }

    @Override
    public Long zrevrank(byte[] key, byte[] member) {
        return decorated.zrevrank(key, member);
    }

    @Override
    public Set<byte[]> zrevrange(byte[] key, int start, int end) {
        return decorated.zrevrange(key, start, end);
    }

    @Override
    public Set<Tuple> zrangeWithScores(byte[] key, int start, int end) {
        return decorated.zrangeWithScores(key, start, end);
    }

    @Override
    public Set<Tuple> zrevrangeWithScores(byte[] key, int start, int end) {
        return decorated.zrevrangeWithScores(key, start, end);
    }

    @Override
    public Long zcard(byte[] key) {
        return decorated.zcard(key);
    }

    @Override
    public Double zscore(byte[] key, byte[] member) {
        return decorated.zscore(key, member);
    }

    @Override
    public List<byte[]> sort(byte[] key) {
        return decorated.sort(key);
    }

    @Override
    public List<byte[]> sort(byte[] key, SortingParams sortingParameters) {
        return decorated.sort(key, sortingParameters);
    }

    @Override
    public Long zcount(byte[] key, double min, double max) {
        return decorated.zcount(key, min, max);
    }

    @Override
    public Set<byte[]> zrangeByScore(byte[] key, double min, double max) {
        return decorated.zrangeByScore(key, min, max);
    }

    @Override
    public Set<byte[]> zrangeByScore(byte[] key, double min, double max, int offset, int count) {
        return decorated.zrangeByScore(key, min, max, offset, count);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max) {
        return decorated.zrangeByScoreWithScores(key, min, max);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max, int offset, int count) {
        return decorated.zrangeByScoreWithScores(key, min, max, offset, count);
    }

    @Override
    public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min) {
        return decorated.zrevrangeByScore(key, max, min);
    }

    @Override
    public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min, int offset, int count) {
        return decorated.zrevrangeByScore(key, max, min, offset, count);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min) {
        return decorated.zrevrangeByScoreWithScores(key, max, min);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min, int offset, int count) {
        return decorated.zrevrangeByScoreWithScores(key, max, min, offset, count);
    }

    @Override
    public Long zremrangeByRank(byte[] key, int start, int end) {
        return decorated.zremrangeByRank(key, start, end);
    }

    @Override
    public Long zremrangeByScore(byte[] key, double start, double end) {
        return decorated.zremrangeByScore(key, start, end);
    }

    @Override
    public Long linsert(byte[] key, BinaryClient.LIST_POSITION where, byte[] pivot, byte[] value) {
        return decorated.linsert(key, where, pivot, value);
    }
}
