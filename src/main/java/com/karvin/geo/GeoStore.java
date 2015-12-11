package com.karvin.geo;

import java.util.List;

/**
 * Created by karvin on 15/12/11.
 */
public interface GeoStore {

    List<GeoObject> getByLocationAndDistance(Location location,long distance,SortEnum sortEnum);

    List<GeoObject> getByLocationAndDistance(Location location,long distance);

    List<GeoObject> getAll();

    void addGeoObject(Location location,GeoObject geoObject);

    void addGeoObject(GeoObject geoObject);

    void addAllGeoObject(GeoStore store);

    void addAllGeoObject(List<GeoObject> list);

}
