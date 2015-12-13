package com.karvin.redis;

import redis.clients.jedis.Jedis;

/**
 * Created by karvin on 15/12/13.
 */
public interface Callable<T> {

    T call(Jedis jedis);

}
