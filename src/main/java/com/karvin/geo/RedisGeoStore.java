package com.karvin.geo;

import com.google.gson.Gson;
import org.apache.commons.collections.CollectionUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;

/**
 * Created by karvin on 15/12/11.
 */
public class RedisGeoStore implements GeoStore {

    private JedisPool jedisPool;

    private String redisGeoKey;

    private String redisDataKey;

    private Gson gson = new Gson();

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
        String maxHash = GeoHashUtils.buildMaxGeoHash(geoHash, GeoHashUtils.getPrecision(distance));
        Jedis jedis = this.getJedisPool().getResource();
        long minScore = GeoHashUtils.base10(minHash);
        long maxScore = GeoHashUtils.base10(maxHash);
        Set<String> keys = jedis.zrangeByScore(this.getRedisGeoKey(), minScore, maxScore);
        String[] keyArray = new String[keys.size()];
        int i=0;
        for(String key : keys){
            keyArray[i++] = key;
        }
        List<String> list = jedis.hmget(this.getRedisDataKey(),keyArray);
        List<GeoObject> geoObjects = new ArrayList<GeoObject>();
        for(String geoString:list){
            geoObjects.add(gson.fromJson(geoString,GeoObject.class));
        }
        GeoSort sort = new DescGeoSort();
        if(SortEnum.ASC.equals(sortEnum)){
            sort = new AscGeoSort();
        }
        return sort.sort(location,geoObjects,distance);
    }

    public List<GeoObject> getByLocationAndDistance(Location location, long distance) {
        return this.getByLocationAndDistance(location,distance,SortEnum.DESC);
    }

    public List<GeoObject> getAll() {
        Jedis jedis = this.getJedisPool().getResource();
        Map<String,String> map = jedis.hgetAll(this.getRedisDataKey());
        Collection<String> values = map.values();
        List<GeoObject> geoObjects = new ArrayList<GeoObject>();
        for(String geoString:values){
            geoObjects.add(gson.fromJson(geoString,GeoObject.class));
        }
        return geoObjects;
    }

    public void addGeoObject(Location location, GeoObject geoObject) {
        String geoHash = GeoHashUtils.encode(location.getLat(), location.getLng());
        long score = GeoHashUtils.base10(geoHash);
        Jedis jedis = this.getJedisPool().getResource();
        jedis.zadd(this.getRedisGeoKey(),score,geoHash);
        jedis.hset(this.getRedisDataKey(),geoHash,gson.toJson(geoObject));
    }

    public void addGeoObject(GeoObject geoObject) {
        this.addGeoObject(geoObject.getLocation(),geoObject);
    }

    public void addAllGeoObject(GeoStore store) {
        this.addAllGeoObject(store.getAll());
    }

    public void addAllGeoObject(List<GeoObject> list) {
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        for(GeoObject geoObject:list){
            this.addGeoObject(geoObject);
        }
    }

    public String getRedisDataKey() {
        return redisDataKey;
    }

    public void setRedisDataKey(String redisDataKey) {
        this.redisDataKey = redisDataKey;
    }
}
