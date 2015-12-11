package com.karvin.geo;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

/**
 * Created by karvin on 15/12/10.
 */
public class AscGeoSort extends BaseGeoSort {

    @Override
    protected List<GeoObject> sort(SortedMap<Double,GeoObject> sortedMap) {
        return new ArrayList<GeoObject>(sortedMap.values());
    }
}
