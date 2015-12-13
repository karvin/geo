package com.karvin.redis;

import redis.clients.jedis.JedisPool;

/**
 * Created by karvin on 15/12/13.
 */
public interface RedisTemplate {

    void doCall(Callable callable);

    void setPool(JedisPool pool);

    void before();

    void after();

}
