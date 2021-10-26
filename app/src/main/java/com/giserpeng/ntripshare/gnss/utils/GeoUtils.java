package com.giserpeng.ntripshare.gnss.utils;

import com.giserpeng.ntripshare.gnss.bean.GnssLocation;

/**
 * 坐标系转换类
 *
 * @author liuhanling
 * @date 2018/11/16
 */
public class GeoUtils {

    private static final double x_pi = 3.1415926535897932384626 * 3000.0 / 180.0;
    private static final double pi = 3.1415926535897932384626;
    private static final double a = 6378137.0; //6378245.0;
    private static final double ee = 0.0066943799901377997; //0.00669342162296594323;

    private static final double RANGE_LON_MIN = 72.004;
    private static final double RANGE_LON_MAX = 137.8347;
    private static final double RANGE_LAT_MIN = 0.8293;
    private static final double RANGE_LAT_MAX = 55.8271;

    /**
     * WGS84 (World Geodetic System) ==> GCJ-02 (Mars Geodetic System)
     *
     * @return
     */
    public static GnssLocation gps84_To_Gcj02(GnssLocation location) {
        double lat = location.getLat();
        double lon = location.getLon();
        if (outOfChina(lat, lon)) {
            return null;
        }

        double dLat = dbTransformLat(lon - 105.0, lat - 35.0);
        double dLon = dbTransformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;

        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double mgLat = lat + dLat;
        double mgLon = lon + dLon;

        location.setLatGcj(mgLat);
        location.setLonGcj(mgLon);
        return location;
    }

    /**
     * WGS84 (World Geodetic System) ==> GCJ-02 (Mars Geodetic System)
     *
     * @return
     */
    public static GnssLocation Gps84_To_Gcj02(double lon, double lat) {
        if (outOfChina(lat, lon)) {
            return null;
        }

        double dLat = dbTransformLat(lon - 105.0, lat - 35.0);
        double dLon = dbTransformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;

        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double mgLat = lat + dLat;
        double mgLon = lon + dLon;

        GnssLocation location = new GnssLocation();
        location.setLatGcj(mgLat);
        location.setLonGcj(mgLon);
        return location;
    }

    /**
     * GCJ-02 (Mars Geodetic System) ==> BD09 (BaiDu Geodetic System)
     *
     * @param point
     * @return
     */
    public static GnssLocation gcj02_To_Bd09(GnssLocation location) {
        double lat = location.getLat();
        double lon = location.getLon();
        location = transform(location);
        double longitude = lon * 2 - location.getLon();
        double latitude = lat * 2 - location.getLat();
        return new GnssLocation(longitude, latitude);
    }

    /**
     * WGS84 (World Geodetic System) ==> BD09 (BaiDu Geodetic System)
     *
     * @param point
     * @return
     */
    public static GnssLocation wgs84_To_Bd09(GnssLocation location) {
        double wgs_lon = location.getLon();
        double wgs_lat = location.getLat();
        double bd_lon;
        double bd_lat;

        if (outOfChina(wgs_lat, wgs_lon)) {
            return null;
        }

        // wgs84 ==> gcj02
        double dLat = dbTransformLat(wgs_lon - 105.0, wgs_lat - 35.0);
        double dLon = dbTransformLon(wgs_lon - 105.0, wgs_lat - 35.0);
        double radLat = wgs_lat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        wgs_lon = wgs_lon + dLon;
        wgs_lat = wgs_lat + dLat;

        // gcj02 ==> bd09
        double x = wgs_lon, y = wgs_lat;
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
        bd_lon = z * Math.cos(theta) + 0.0065;
        bd_lat = z * Math.sin(theta) + 0.006;
        location.setLon(bd_lon);
        location.setLat(bd_lat);
        return location;
    }

    /**
     * 判断坐标系是否在中国以外
     *
     * @param lat
     * @param lon
     * @return
     */
    private static boolean outOfChina(double lat, double lon) {
        if (lon < RANGE_LON_MIN || lon > RANGE_LON_MAX)
            return true;
        if (lat < RANGE_LAT_MIN || lat > RANGE_LAT_MAX)
            return true;
        return false;
    }

    public static GnssLocation transform(GnssLocation point) {
        double lat = point.getLat();
        double lon = point.getLon();
        if (outOfChina(lat, lon)) {
            return new GnssLocation(lon, lat);
        }

        double dLat = dbTransformLat(lon - 105.0, lat - 35.0);
        double dLon = dbTransformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double mgLat = lat + dLat;
        double mgLon = lon + dLon;
        return new GnssLocation(mgLon, mgLat);
    }

    public static double dbTransformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    private static double dbTransformLon(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0 * pi)) * 2.0 / 3.0;
        return ret;
    }
}