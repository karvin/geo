package com.karvin.geo;

import java.io.Serializable;

/**
 * Created by karvin on 15/12/9.
 */
public class GeoObject<T> implements Serializable{

    private String geoHash;
    private Location location;
    private T object;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }

    public String getGeoHash() {
        return geoHash;
    }

    public void setGeoHash(String geoHash) {
        this.geoHash = geoHash;
    }
}
