package com.karvin.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Created by karvin on 15/12/13.
 */
public class RedisTemplateImpl implements RedisTemplate {

    private JedisPool pool;

    public void doCall(Callable callable) {
        Jedis jedis = this.getPool().getResource();
        try{
            callable.call(jedis);
        }finally {
            this.getPool().returnResourceObject(jedis);
        }
    }

    public void before() {

    }

    public void after() {

    }

    public void setPool(JedisPool pool) {
        this.pool = pool;
    }

    public JedisPool getPool(){
        return this.pool;
    }
}
