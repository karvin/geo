package com.karvin.geo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;

/**
 * Created by karvin on 15/12/10.
 */
public class DescGeoSort extends BaseGeoSort {
    @Override
    protected List<GeoObject> sort(SortedMap<Double, GeoObject> sortedMap) {
        Collection<GeoObject> collection = sortedMap.values();
        List<GeoObject> list = new ArrayList<GeoObject>();
        for(GeoObject geoObject:collection){
            list.add(0,geoObject);
        }
        return list;
    }
}
