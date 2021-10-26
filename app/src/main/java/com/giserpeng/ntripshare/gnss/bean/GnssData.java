package com.giserpeng.ntripshare.gnss.bean;

import android.annotation.SuppressLint;

import java.util.HashMap;
import java.util.Map;

public class GnssData {

    private long utc;

    private int satellite;

    private Map<String, HashMap<Integer, SatelliteData>> satelliteDataMap = new HashMap<>();

    public GnssData() {
        this(System.currentTimeMillis());
    }

    public GnssData(long utc) {
        this.utc = utc;
    }

    public long getUtc() {
        return utc;
    }

    public void setUtc(long utc) {
        this.utc = utc;
    }

    public int getSatellite() {
        return satellite;
    }

    public void setSatellite(int satellite) {
        this.satellite = satellite;
    }

    @SuppressLint("UseSparseArrays")
    public void addSatelliteData(SatelliteData satelliteData) {
        String system = satelliteData.getSatelliteSystem();
        if (system == null) return;
        if (satelliteDataMap.get(system) == null) {
            satelliteDataMap.put(system, new HashMap<Integer, SatelliteData>());
        }
        satelliteDataMap.get(system).put(satelliteData.getPrn(), satelliteData);
    }

    public SatelliteData getSatelliteData(String system, int prn) {
        HashMap<Integer, SatelliteData> satelliteDatas = getSatelliteDatas(system);
        if (satelliteDatas == null || satelliteDatas.isEmpty()) {
            return null;
        }
        return satelliteDatas.get(prn);
    }

    public HashMap<Integer, SatelliteData> getSatelliteDatas(String system) {
        return satelliteDataMap.get(system);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("GnssData{");
        sb.append("utc=").append(utc);
        sb.append(", satelliteDataMap=").append(satelliteDataMap);
        sb.append('}');
        return sb.toString();
    }
}