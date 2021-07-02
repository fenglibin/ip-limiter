/*
* Copyright All Rights Reserved.
* @Owner:fenglibin
* @Date:  2018-01-22 22:37
*/
package com.eeeffff.limiter.dashboard.redis;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "spring.redis")
public class RedisProperties {

    private int database = 0;
    private String url;
    private String host = "localhost";
    private String hosts = "localhost:6379";
    private String password;
    private int port = 6379;
    private boolean ssl;
    private Duration timeout;
    private RedisProperties.Cluster cluster;
    private int  connectionTimeout;
    private int  soTimeout;
    private int  maxAttempts;
    private final Jedis jedis = new Jedis();
    public String getHosts() {
        return hosts;
    }

    public Jedis getJedis() {
        return jedis;
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }


    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    /**
     * Pool properties.
     */
    public static class Pool {

        /**
         * Maximum number of "idle" connections in the pool. Use a negative value to
         * indicate an unlimited number of idle connections.
         */
        private int maxIdle = 8;

        /**
         * Target for the minimum number of idle connections to maintain in the pool. This
         * setting only has an effect if it is positive.
         */
        private int minIdle = 0;

        /**
         * Maximum number of connections that can be allocated by the pool at a given
         * time. Use a negative value for no limit.
         */
        private int maxActive = 8;

        /**
         * Maximum amount of time a connection allocation should block before throwing an
         * exception when the pool is exhausted. Use a negative value to block
         * indefinitely.
         */
        private Duration maxWait = Duration.ofMillis(-1);

        public int getMaxIdle() {
            return this.maxIdle;
        }

        public void setMaxIdle(int maxIdle) {
            this.maxIdle = maxIdle;
        }

        public int getMinIdle() {
            return this.minIdle;
        }

        public void setMinIdle(int minIdle) {
            this.minIdle = minIdle;
        }

        public int getMaxActive() {
            return this.maxActive;
        }

        public void setMaxActive(int maxActive) {
            this.maxActive = maxActive;
        }

        public Duration getMaxWait() {
            return this.maxWait;
        }

        public void setMaxWait(Duration maxWait) {
            this.maxWait = maxWait;
        }

    }

    public static class Cluster {
        private List<String> nodes;
        private Integer maxRedirects;

        public Cluster() {
        }

        public List<String> getNodes() {
            return this.nodes;
        }

        public void setNodes(List<String> nodes) {
            this.nodes = nodes;
        }

        public Integer getMaxRedirects() {
            return this.maxRedirects;
        }

        public void setMaxRedirects(Integer maxRedirects) {
            this.maxRedirects = maxRedirects;
        }
    }

    public static class Jedis {

        /**
         * Jedis pool configuration.
         */
        private Pool pool;

        public Pool getPool() {
            return this.pool;
        }

        public void setPool(Pool pool) {
            this.pool = pool;
        }

    }


}

