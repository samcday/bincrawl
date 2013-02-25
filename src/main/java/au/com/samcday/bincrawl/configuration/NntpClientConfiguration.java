package au.com.samcday.bincrawl.configuration;

import com.google.common.base.Strings;

public class NntpClientConfiguration {
    private String host;
    private int port;
    private String username;
    private String password;
    private boolean ssl;
    private int maxConnections;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public boolean hasAuth() {
        return !Strings.isNullOrEmpty(this.username) && !Strings.isNullOrEmpty(this.password);
    }
}
