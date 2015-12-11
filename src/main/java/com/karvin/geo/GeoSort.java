package com.karvin.geo;

import java.util.Collection;
import java.util.List;

/**
 * Created by karvin on 15/12/10.
 */
public interface GeoSort {

    List<GeoObject> sort(Location location, Collection<GeoObject> collection, long distance);

}
