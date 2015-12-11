package com.karvin.geo;

import java.util.*;

/**
 * Created by karvin on 15/12/9.
 */
public class MemoryGeoStore implements GeoStore{

    private SortedMap<String,GeoObject> collection = new TreeMap<String,GeoObject>();

    public List<GeoObject> getByLocationAndDistance(Location location,long distance,SortEnum sortEnum){
        String geoHash = GeoHashUtils.encode(location.getLat(), location.getLng());
        int precision = GeoHashUtils.getPrecision(distance);

        String minGeoHash = GeoHashUtils.buildMinGeoHash(geoHash, precision);
        String maxGeoHash = GeoHashUtils.buildMaxGeoHash(geoHash, precision);
        Collection<GeoObject> result = collection.subMap(minGeoHash,maxGeoHash).values();
        GeoSort sort = null;
        if(SortEnum.DESC.equals(sortEnum)){
            sort = new DescGeoSort();
        }else{
            sort = new AscGeoSort();
        }
        return sort.sort(location,result,distance);
    }

    public List<GeoObject> getByLocationAndDistance(Location location,long distance){
        return this.getByLocationAndDistance(location,distance,SortEnum.ASC);
    }

    public List<GeoObject> getAll() {
        return new ArrayList<GeoObject>(collection.values());
    }

    public void addGeoObject(Location location,GeoObject geoObject){
        String geoHash = GeoHashUtils.encode(location.getLat(),location.getLng());
        collection.put(geoHash, geoObject);
    }

    public void addGeoObject(GeoObject geoObject){
        if(geoObject != null) {
            this.addGeoObject(geoObject.getLocation(), geoObject);
        }
    }

    public void addAllGeoObject(GeoStore store){
        this.addAllGeoObject(store.getAll());
    }

    public void addAllGeoObject(List<GeoObject> list){
        for(GeoObject object:list){
            if(object == null)
                continue;
            this.addGeoObject(object.getLocation(),object);
        }
    }

    public static void main(String[] args){
        GeoStore store = new MemoryGeoStore();
        Random random = new Random();
        long time = System.nanoTime();
        for(int i=0;i<2000000;i++){
            double lat = 39 + random.nextInt(300000000)/10000000.0;
            double lng = 115 + random.nextInt(300000000)/10000000.0;
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
        System.out.println("add 2000000 geo cost:"+(System.nanoTime()-time)+" nano");
        Location location = new Location();
        location.setLat(40.20);
        location.setLng(116.60);
        long start = System.nanoTime();
        List<GeoObject> objects = store.getByLocationAndDistance(location,10000,SortEnum.ASC);
        System.out.println("time span:"+(System.nanoTime()-start)+" matches:"+objects.size());
        for(GeoObject geoObject:objects){
            Location loc = geoObject.getLocation();
            double distance = GeoHashUtils.getDistance(40.20, 116.60, loc.getLat(), loc.getLng());
            System.out.println("location:{lat="+loc.getLat()+" lng="+loc.getLng()+"} distance="+distance+" poi id:"+geoObject.getObject()+" geoHash:"+geoObject.getGeoHash());
        }
    }

}
