package com.karvin.geo;

import java.util.*;

/**
 * Created by karvin on 15/12/10.
 */
public abstract class BaseGeoSort implements GeoSort {

    @Override
    public List<GeoObject> sort(Location location, Collection<GeoObject> collection,long distance) {
        if(collection == null || collection.isEmpty()){
            return Collections.EMPTY_LIST;
        }
        SortedMap<Double,GeoObject> map = new TreeMap<Double,GeoObject>();
        for(GeoObject geoObject:collection){
            Location geoLocation = geoObject.getLocation();
            double real = GeoHashUtils.getDistance(location.getLat(),location.getLng(),
                    geoLocation.getLat(),geoLocation.getLng());
            if(real <= distance){
                map.put(real, geoObject);
            }
        }
        return sort(map);
    }

    protected abstract List<GeoObject> sort(SortedMap<Double,GeoObject> sortedMap);

}
