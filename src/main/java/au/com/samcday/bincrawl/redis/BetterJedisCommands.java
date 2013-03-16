package au.com.samcday.bincrawl.redis;

import com.google.common.base.Optional;

public interface BetterJedisCommands {
    public Optional<Long> hgetlong(String key, String field);
    public Long hset(String key, String field, Long value);
    public Optional<Integer> hgetint(String key, String field);
}
