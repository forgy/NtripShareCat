package com.giserpeng.ntripshare.gnss.bean;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SatelliteData {

    private String satelliteSystem;
    private int prn;
    private Map<String, SignalData> signalDataMap = new HashMap<>();

    public SatelliteData() {
    }

    public SatelliteData(String satelliteSystem, int prn) {
        this.satelliteSystem = satelliteSystem;
        this.prn = prn;
        this.signalDataMap = new HashMap<>();
    }

    public String getSatelliteSystem() {
        return satelliteSystem;
    }

    public void setSatelliteSystem(String satelliteSystem) {
        this.satelliteSystem = satelliteSystem;
    }

    public int getPrn() {
        return prn;
    }

    public void setPrn(int prn) {
        this.prn = prn;
    }

    public Map<String, SignalData> getSignalDataMap() {
        return signalDataMap;
    }

    public void setSignalDataMap(Map<String, SignalData> signalDataMap) {
        this.signalDataMap = signalDataMap;
    }

    public void addSignalData(SignalData signalData) {
        signalDataMap.put(signalData.getFrequencyBand(), signalData);
    }

    public SignalData getSignalData(String frequencyBand) {
        return signalDataMap.get(frequencyBand);
    }

    public Collection<SignalData> getAllSignalData() {
        return signalDataMap.values();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("SatelliteData{");
        sb.append("satelliteSystem='").append(satelliteSystem).append('\'');
        sb.append(", prn=").append(prn);
        sb.append(", signalDataMap=").append(signalDataMap);
        sb.append('}');
        return sb.toString();
    }
}
