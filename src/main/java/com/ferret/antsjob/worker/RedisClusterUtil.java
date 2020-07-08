package com.ferret.antsjob.worker;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.HashSet;
import java.util.Set;

public class RedisClusterUtil {

    public static String set(String key, String value) {
        return RedisCluster.getJedis().set(key, value);
    }

    public static String setEx(String key, String value, int exTime) {
        return RedisCluster.getJedis().setex(key, exTime, value);
    }

    public static String get(String key) {
        return RedisCluster.getJedis().get(key);
    }

    public static Long expire(String key, int exTime) {
        return RedisCluster.getJedis().expire(key, exTime);
    }

    public static Long del(String key) {
        return RedisCluster.getJedis().del(key);
    }

    public static void main(String[] args) {
        ScanParams scanParams = new ScanParams().count(1000);
        Set<String> allKeys = new HashSet<>();
        for (JedisPool pool : RedisCluster.getJedis().getClusterNodes().values()) {
            String cur = ScanParams.SCAN_POINTER_START;
            do {
                try (Jedis jedis = pool.getResource()) {
                    ScanResult<String> scanResult = jedis.scan(cur, scanParams);
                    allKeys.addAll(scanResult.getResult());
                    cur = scanResult.getStringCursor();
                }
                if (allKeys.size() >= 1000) break;
            } while (!cur.equals(ScanParams.SCAN_POINTER_START));
            if (allKeys.size() >= 1000) break;
        }
        allKeys.stream().forEach(k -> {
            System.out.println(k + " : " + RedisClusterUtil.get(k));
        });
    }
}
