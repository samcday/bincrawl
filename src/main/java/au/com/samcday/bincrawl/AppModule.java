package au.com.samcday.bincrawl;

import au.com.samcday.bincrawl.configuration.NntpClientConfiguration;
import au.com.samcday.bincrawl.configuration.RedisConfiguration;
import au.com.samcday.bincrawl.pool.BetterJedisPool;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;
import redis.clients.jedis.JedisPoolConfig;

import java.net.MalformedURLException;

public class AppModule implements Module {
    @Override
    public void configure(Binder binder) {
        binder.bind(BinaryPartProcessor.class).to(RedisBinaryPartProcessor.class);
    }

    // TODO: this should obviously be getting loaded from an external source.
    @Provides
    @Singleton
    public RedisConfiguration provideRedisConfiguration() {
        RedisConfiguration config = new RedisConfiguration();
        config.setHost("localhost");
        return config;
    }

    @Provides
    @Singleton
    public NntpClientConfiguration provideNntpClientConfiguration() {
        NntpClientConfiguration config = new NntpClientConfiguration();
        config.setHost(System.getProperty("nntp.host"));
        config.setPort(Integer.parseInt(System.getProperty("nntp.port")));
        config.setUsername(System.getProperty("nntp.user"));
        config.setPassword(System.getProperty("nntp.pass"));
        config.setSsl(true);
        config.setMaxConnections(20);
        return config;
    }

    // TODO: config etc.
    @Provides
    @Singleton
    CouchDbConnector provideCouchDbConnector() throws MalformedURLException {
        HttpClient httpClient = new StdHttpClient.Builder()
            .url("http://localhost:5984")
            .build();

        CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
        CouchDbConnector db = new StdCouchDbConnector("bincrawl", dbInstance);
        db.createDatabaseIfNotExists();

        return db;
    }

    @Provides
    @Singleton
    BetterJedisPool provideJedisPool(RedisConfiguration config) {
        BetterJedisPool pool = new BetterJedisPool(new JedisPoolConfig(), config.getHost());
        return pool;
    }
}
