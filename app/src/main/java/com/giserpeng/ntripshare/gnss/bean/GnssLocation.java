package com.giserpeng.ntripshare.gnss.bean;


public class GnssLocation implements Cloneable {

    // 时间
    private long utc;

    // 经度
    private double lon;

    // 纬度
    private double lat;

    // 海拔高度
    private float alt;

    // 大地水准面高差
    private float geo;

    // 经度加偏
    private double lonGcj;

    // 纬度加偏
    private double latGcj;

    // 卫星数目
    private int sat;

    // 定位状态
    private int fix;

    // 精度
    private float acc;

    // 速度
    private float speed;

    // 方位
    private float bearing;

    public GnssLocation() {
    }

    public GnssLocation(double lon, double lat) {
        this.lon = lon;
        this.lat = lat;
    }

    public long getUtc() {
        return utc;
    }

    public void setUtc(long utc) {
        this.utc = utc;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public float getAlt() {
        return alt;
    }

    public void setAlt(float alt) {
        this.alt = alt;
    }

    public float getGeo() {
        return geo;
    }

    public void setGeo(float geo) {
        this.geo = geo;
    }

    public double getLonGcj() {
        return lonGcj;
    }

    public void setLonGcj(double lonGcj) {
        this.lonGcj = lonGcj;
    }

    public double getLatGcj() {
        return latGcj;
    }

    public void setLatGcj(double latGcj) {
        this.latGcj = latGcj;
    }

    public int getSat() {
        return sat;
    }

    public void setSat(int sat) {
        this.sat = sat;
    }

    public int getFix() {
        return fix;
    }

    public void setFix(int fix) {
        this.fix = fix;
    }

    public float getAcc() {
        return acc;
    }

    public void setAcc(float acc) {
        this.acc = acc;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public boolean isAvailable(int type) {
        if (type == 0) { // 固定解
            return (fix == 4 && acc < 0.1);
        } else { // 浮点解40cm
            return (fix == 4 || fix == 5) && (acc < 0.4);
        }
    }

    @Override
    public GnssLocation clone() {
        try {
            return (GnssLocation) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
