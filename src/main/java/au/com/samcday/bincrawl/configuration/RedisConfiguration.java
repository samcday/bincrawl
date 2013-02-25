package au.com.samcday.bincrawl.configuration;

public class RedisConfiguration {
    private String host;
    private int db;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getDb() {
        return db;
    }

    public void setDb(int db) {
        this.db = db;
    }
}
