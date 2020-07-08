package com.ferret.antsjob.worker;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RedisCluster {

    private static JedisCluster jedisCluster;
    private static Integer maxTotal = 20;
    private static Integer maxIdle = 10;
    private static Integer minIdle = 2;
    private static Boolean testOnBorrow = true;
    private static Boolean testOnReturn = false;

    private static void initPool() {

        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(30);
        config.setMaxWaitMillis(2000);
        Set<HostAndPort> jedisClusterNode = new HashSet<HostAndPort>();

        jedisClusterNode.add(new HostAndPort("10.0.0.192", 5001));
        jedisClusterNode.add(new HostAndPort("10.0.0.192", 5002));
        jedisClusterNode.add(new HostAndPort("10.0.0.192", 5003));
        jedisClusterNode.add(new HostAndPort("10.0.0.192", 5004));
        jedisClusterNode.add(new HostAndPort("10.0.0.192", 5005));
        jedisClusterNode.add(new HostAndPort("10.0.0.192", 5006));
        jedisCluster = new JedisCluster(jedisClusterNode, config);
    }

    static {
        initPool();
    }

    public static JedisCluster getJedis() {
        return jedisCluster;
    }
}
