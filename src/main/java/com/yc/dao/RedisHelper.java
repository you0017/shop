package com.yc.dao;

import redis.clients.jedis.Jedis;

public class RedisHelper {
    public static Jedis getRedisInstance(){
        Jedis jedis = new Jedis( DbProperties.getInstance().getProperty("redis.host"),
                Integer.parseInt(DbProperties.getInstance().getProperty("redis.port")));
        jedis.auth(DbProperties.getInstance().getProperty("redis.password"));
        return jedis;
    }
}
