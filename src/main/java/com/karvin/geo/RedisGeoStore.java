package com.karvin.geo;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;

/**
 * Created by karvin on 15/12/11.
 */
public class RedisGeoStore implements GeoStore {

    private JedisPool jedisPool;

    private String redisGeoKey;

    public String getRedisGeoKey() {
        return redisGeoKey;
    }

    public void setRedisGeoKey(String redisGeoKey) {
        this.redisGeoKey = redisGeoKey;
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public List<GeoObject> getByLocationAndDistance(Location location, long distance, SortEnum sortEnum) {
        String geoHash = GeoHashUtils.encode(location.getLat(),location.getLng());
        String minHash = GeoHashUtils.buildMinGeoHash(geoHash, GeoHashUtils.getPrecision(distance));
        String maxHash = GeoHashUtils.buildMaxGeoHash(geoHash,GeoHashUtils.getPrecision(distance));
        Jedis jedis = this.getJedisPool().getResource();
        long minIndex = jedis.zrank(this.getRedisGeoKey(),minHash);
        long maxIndex = jedis.zrank(this.getRedisGeoKey(),maxHash);
        return null;
    }

    public List<GeoObject> getByLocationAndDistance(Location location, long distance) {
        return null;
    }

    public List<GeoObject> getAll() {
        return null;
    }

    public void addGeoObject(Location location, GeoObject geoObject) {

    }

    public void addGeoObject(GeoObject geoObject) {

    }

    public void addAllGeoObject(GeoStore store) {

    }

    public void addAllGeoObject(List<GeoObject> list) {

    }
}
