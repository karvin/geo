package com.karvin.geo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by karvin on 15/12/9.
 */
public class GeoHashUtils {

    private static final double MAX_LAT = 90;
    private static final double MIN_LAT = -90;
    private static final double MAX_LNG = 180;
    private static final double MIN_LNG = -180;
    private static final int PRECISION = 26;
    private static final double EARTH_RADIUS = 6378.137;

    private static final Map<Character,Integer> CHAR_MAP = new HashMap<Character,Integer>();

    private static final Map<Long,Integer> LENGTH_MAP = new HashMap<Long,Integer>();

    private static final List<Long> DISTANCE_CONSTRAINT = new ArrayList<Long>();

    private static final char[] CHARS = {'0','1','2','3','4','5','6','7','8','9',
                                    'b','c','d','e','f','g','h','j','k','m','n',
                                    'p','q','r','s','t','u','v','w','x','y','z'};
    static {
        for(int i=0;i<CHARS.length;i++){
            CHAR_MAP.put(CHARS[i],i);
        }
        LENGTH_MAP.put(20l,8);
        LENGTH_MAP.put(76l,7);
        LENGTH_MAP.put(610l,6);
        LENGTH_MAP.put(2400l,5);
        LENGTH_MAP.put(20000l,4);
        LENGTH_MAP.put(78000l,3);
        LENGTH_MAP.put(630000l,2);
        LENGTH_MAP.put(2500000l,1);
        DISTANCE_CONSTRAINT.add(20l);
        DISTANCE_CONSTRAINT.add(76l);
        DISTANCE_CONSTRAINT.add(610l);
        DISTANCE_CONSTRAINT.add(2400l);
        DISTANCE_CONSTRAINT.add(20000l);
        DISTANCE_CONSTRAINT.add(78000l);
        DISTANCE_CONSTRAINT.add(630000l);
        DISTANCE_CONSTRAINT.add(2500000l);
    }

    public static String encode(double lat,double lng){
        int latHash = 0;
        int lngHash = 0;
        double maxLat = MAX_LAT;
        double minLat = MIN_LAT;
        double middleLat = (maxLat + minLat)/2;
        for(int i=0;i<PRECISION;i++){
            if(lat>=middleLat){
                minLat = middleLat;
                latHash = latHash << 1;
                latHash = latHash | 1;
            }else{
                maxLat = middleLat;
                latHash = latHash << 1;
                latHash = latHash | 0;
            }
            middleLat = (maxLat + minLat)/2;
        }

        double maxLng = MAX_LNG;
        double minLng = MIN_LNG;
        double middleLng = (maxLng + minLng)/2;
        for(int i=0;i<PRECISION;i++){
            if(lng>=middleLng){
                minLng = middleLng;
                lngHash = lngHash << 1;
                lngHash = lngHash | 1;
            }else{
                maxLng = middleLng;
                lngHash = lngHash << 1;
                lngHash = lngHash | 0;
            }
            middleLng = (maxLng + minLng)/2;
        }
        return base32(latHash, lngHash);
    }

    private static String base32(int latHash,int lngHash){
        long code = 0;
        for(int i=PRECISION-1;i>=0;i--){
            int latBit = (latHash >>i)&1;
            int lngBit = (lngHash>>i)&1;
            int hash = (latBit << 1) | lngBit;
            code = code << 2;
            code = code | hash;
        }
        StringBuilder sb = new StringBuilder();
        long result = code;
        while(result > 0){
            int mod = (int)(result  % 32);
            sb.append(CHARS[mod]);
            result = result /32;
        }
        return sb.reverse().toString();
    }

    public static double[] decode(String geoHash){
        long hashCode = 0;
        char[] chars = geoHash.toCharArray();
        for(int i= 0;i<chars.length;i++){
            int index = CHAR_MAP.get(chars[i]);
            hashCode = ((hashCode)<<5) + index;
        }
        double maxLat = MAX_LAT;
        double minLat = MIN_LAT;
        double maxLng = MAX_LNG;
        double minLng = MIN_LNG;
        double lat = (maxLat+minLat)/2;
        double lng = (maxLng+minLng)/2;
        long lngHash = 0;
        for(int i=PRECISION-1;i>=0;i--){
            int result = (int)(hashCode >> (i*2));
            int latResult = result & 2;
            int lngResult = result & 1;
            if(latResult > 0){
                minLat = lat;
            }else{
                maxLat = lat;
            }
            if(lngResult > 0){
                lngHash = (lngHash << 1) | 1;
                minLng = lng;
            }else{
                lngHash = (lngHash<<1)&0;
                maxLng = lng;
            }
            lat = (maxLat+minLat)/2;
            lng = (maxLng+minLng)/2;
        }
        System.out.println("lng hash:"+lngHash);
        return new double[]{lat,lng};
    }

    public static double getDistance(double latStart,double lngStart,double latEnd,double lngEnd){
        double radLatStart = rad(latStart);
        double radLatEnd = rad(latEnd);
        double latDelta = radLatEnd - radLatStart;
        double lngDelta = rad(lngStart) - rad(lngEnd);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(latDelta / 2), 2) +
                Math.cos(radLatStart) * Math.cos(radLatEnd) * Math.pow(Math.sin(lngDelta / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10;
        return s;
    }

    private static double rad(double d)
    {
        return d * Math.PI / 180.0;
    }

    public static int getPrecision(long distance){
        for(int i=0;i<DISTANCE_CONSTRAINT.size();i++){
            if(distance<=DISTANCE_CONSTRAINT.get(i)){
                long length = DISTANCE_CONSTRAINT.get(i);
                return LENGTH_MAP.get(length);
            }
        }
        long length = DISTANCE_CONSTRAINT.get(DISTANCE_CONSTRAINT.size()-1);
        return LENGTH_MAP.get(length);
    }

    public static String buildMinGeoHash(String geoHash,int precision){
        if(precision>=geoHash.length())
            return geoHash;
        StringBuilder sb = new StringBuilder();
        sb.append(geoHash.substring(0,precision));
        for(int i=0;i<11-precision;i++){
            sb.append("0");
        }
        return sb.toString();
    }

    public static String buildMaxGeoHash(String geoHash,int precision){
        if(precision>=geoHash.length())
            return geoHash;
        StringBuilder sb = new StringBuilder();
        sb.append(geoHash.substring(0,precision));
        for(int i=0;i<11-precision;i++){
            sb.append("z");
        }
        return sb.toString();
    }

    public static void main(String[] args){
        String hash = GeoHashUtils.encode(30.63578, 104.031601);
        System.out.println(hash);
        double[] geo = GeoHashUtils.decode(hash);
        for(int i=0;i<geo.length;i++){
            System.out.println(geo[i]);
        }
        System.out.println(GeoHashUtils.getDistance(30.63578, 104.031601, 30.43, 116.27));
    }

}
