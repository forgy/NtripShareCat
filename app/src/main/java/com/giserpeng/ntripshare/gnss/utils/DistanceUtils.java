package com.giserpeng.ntripshare.gnss.utils;

import android.location.Location;

import java.text.DecimalFormat;

public class DistanceUtils {

    private static final double EARTH_RADIUS = 6378137;
    private static final DecimalFormat FORMATTER = new DecimalFormat("#####0.00");

    /**
     * Google算法
     *
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return
     */
    public static double getDistance(double lat1, double lng1, double lat2, double lng2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, results);
        return results[0];
    }

    /**
     * Google算法
     *
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return
     */
    public static String getDistanceString(double lat1, double lng1, double lat2, double lng2) {
        return format(getDistance(lat1, lng1, lat2, lng2));
    }

    /**
     * Google算法
     *
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return
     */
    public static String getDistanceKm(double lat1, double lng1, double lat2, double lng2) {
        return format(getDistance(lat1, lng1, lat2, lng2) / 1000) + "km";
    }

    /**
     * 格式化保留两位
     *
     * @param distance
     * @return
     */
    public static String format(double distance) {
        return FORMATTER.format(distance);
    }

    /**
     * 距离算法
     *
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return
     */
    public static double distance(double lat1, double lng1, double lat2, double lng2) {
        lat1 = degreeToRadian(lat1);
        lng1 = degreeToRadian(lng1);
        lat2 = degreeToRadian(lat2);
        lng2 = degreeToRadian(lng2);

        double lat = Math.abs(lat1 - lat2);
        double lng = Math.abs(lng1 - lng2);

        double h = haverSin(lat) + Math.cos(lat1) * Math.cos(lat2) * haverSin(lng);
        return 2 * EARTH_RADIUS * Math.asin(Math.sqrt(h));
    }

    public static double haverSin(double theta) {
        return Math.pow(Math.sin(theta / 2), 2);
    }

    public static double degreeToRadian(double degree) {
        return degree * Math.PI / 180;
    }
}
