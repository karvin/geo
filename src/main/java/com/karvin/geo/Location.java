package com.karvin.geo;

import java.io.Serializable;

/**
 * Created by karvin on 15/12/9.
 */
public class Location implements Serializable {

    private double lat;
    private double lng;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
