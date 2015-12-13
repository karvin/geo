package com.karvin.geo;

import com.google.gson.Gson;
import com.karvin.redis.Callable;
import com.karvin.redis.RedisTemplate;
import com.karvin.redis.RedisTemplateImpl;
import org.apache.commons.collections.CollectionUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;

/**
 * Created by karvin on 15/12/11.
 */
public class RedisGeoStore implements GeoStore {

    private RedisTemplate template;

    private String redisGeoKey;

    private String redisDataKey;

    private Gson gson = new Gson();

    public String getRedisGeoKey() {
        return redisGeoKey;
    }

    public void setRedisGeoKey(String redisGeoKey) {
        this.redisGeoKey = redisGeoKey;
    }

    public List<GeoObject> getByLocationAndDistance(Location location, long distance, SortEnum sortEnum) {
        String geoHash = GeoHashUtils.encode(location.getLat(), location.getLng());
        final int precision = GeoHashUtils.getPrecision(distance);
        final List<String> neighbours = GeoHashUtils.getNeighbours(geoHash, distance);
        final List<String> list = new ArrayList<String>();
        Callable callable = new Callable() {
            public Object call(Jedis jedis) {
                for(String neighbour:neighbours) {
                    String minHash = GeoHashUtils.buildMinGeoHash(neighbour, precision);
                    String maxHash = GeoHashUtils.buildMaxGeoHash(neighbour, precision);
                    long minScore = GeoHashUtils.base10(minHash);
                    long maxScore = GeoHashUtils.base10(maxHash);
                    Set<String> keys = jedis.zrangeByScore(RedisGeoStore.this.getRedisGeoKey(), minScore, maxScore);
                    if(CollectionUtils.isEmpty(keys)){
                        continue;
                    }
                    String[] keyArray = new String[keys.size()];
                    int i = 0;
                    for (String key : keys) {
                        keyArray[i++] = key;
                    }
                    list.addAll(jedis.hmget(RedisGeoStore.this.getRedisDataKey(),keyArray));
                }
                return list;
            }
        };
        template.doCall(callable);
        List<GeoObject> geoObjects = new ArrayList<GeoObject>();
        for (String geoString : list) {
            geoObjects.add(gson.fromJson(geoString, GeoObject.class));
        }
        GeoSort sort = new DescGeoSort();
        if(SortEnum.ASC.equals(sortEnum)){
            sort = new AscGeoSort();
        }
        return sort.sort(location,geoObjects,distance);
    }

    public List<GeoObject> getByLocationAndDistance(Location location, long distance) {
        return this.getByLocationAndDistance(location, distance, SortEnum.DESC);
    }

    public List<GeoObject> getAll() {
        final Map<String,String> map = new HashMap<String, String>();
        Callable callable = new Callable() {
            public Object call(Jedis jedis) {
                Map<String,String> result = jedis.hgetAll(RedisGeoStore.this.getRedisDataKey());
                map.putAll(result);
                return map;
            }
        };
        template.doCall(callable);
        Collection<String> values = map.values();
        List<GeoObject> geoObjects = new ArrayList<GeoObject>();
        for(String geoString:values){
            geoObjects.add(gson.fromJson(geoString,GeoObject.class));
        }
        return geoObjects;
    }

    public void addGeoObject(Location location, final GeoObject geoObject) {
        final String geoHash = GeoHashUtils.encode(location.getLat(), location.getLng());
        final long score = GeoHashUtils.base10(geoHash);
        Callable callable = new Callable() {
            public Object call(Jedis jedis) {
                jedis.zadd(RedisGeoStore.this.getRedisGeoKey(), score, geoHash);
                jedis.hset(RedisGeoStore.this.getRedisDataKey(), geoHash, gson.toJson(geoObject));
                return null;
            }
        };
        template.doCall(callable);
    }

    public void addGeoObject(GeoObject geoObject) {
        this.addGeoObject(geoObject.getLocation(), geoObject);
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

    public static void main(String[] args){
        JedisPool jedisPool = new JedisPool("127.0.0.1",6379);
        RedisTemplate template = new RedisTemplateImpl();
        template.setPool(jedisPool);
        String redisDataKey = "geo_data:";
        String geoKey = "geo_key:";
        RedisGeoStore store = new RedisGeoStore();
        store.setTemplate(template);
        store.setRedisDataKey(redisDataKey);
        store.setRedisGeoKey(geoKey);
        /*Random random = new Random();
        long time = System.nanoTime();
        for(int i=0;i<500000;i++){
            double lat = 39 + random.nextInt(500000000)/10000000.0;
            double lng = 115 + random.nextInt(500000000)/10000000.0;
            Location location = new Location();
            location.setLat(lat);
            location.setLng(lng);
            GeoObject geoObject = new GeoObject();
            String geoHash = GeoHashUtils.encode(lat,lng);
            geoObject.setLocation(location);
            geoObject.setGeoHash(geoHash);
            geoObject.setObject(i);
            store.addGeoObject(location,geoObject);
        }

        System.out.println("add 500000 geo cost:"+(System.nanoTime()-time)+" nano");*/
        Location location = new Location();
        location.setLat(60.20);
        location.setLng(116.60);
        long start = System.nanoTime();
        List<GeoObject> objects = store.getByLocationAndDistance(location,6000,SortEnum.ASC);
        System.out.println("time span:"+(System.nanoTime()-start)+" matches:"+objects.size());
        for(GeoObject geoObject:objects){
            Location loc = geoObject.getLocation();
            double distance = GeoHashUtils.getDistance(60.20, 116.60, loc.getLat(), loc.getLng());
            System.out.println("location:{lat="+loc.getLat()+" lng="+loc.getLng()+"} distance="+distance+" poi id:"+geoObject.getObject()+" geoHash:"+geoObject.getGeoHash());
        }
    }

    public void setTemplate(RedisTemplate template) {
        this.template = template;
    }
}
