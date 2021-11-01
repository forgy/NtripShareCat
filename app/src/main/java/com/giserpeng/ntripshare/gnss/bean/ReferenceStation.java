package com.giserpeng.ntripshare.gnss.bean;


public class ReferenceStation {

    // 1006
    private int messageNumber;

    // 0-4095
    private int referenceStationId;

    // 0-63
    private int itrf;

    // 0 N, 1 Y.
    private int gpsIndicator;

    // 0 N, 1 Y.
    private int gloIndicator;

    // 0 N, 1 Y.
    private int galIndicator;

    // 0 Real, 1 Computed.
    private int referenceStationIndicator;

    private double ecefX;

    // 0 different, 1 same.
    private int receiverOscillatorIndicator;

    private double ecefY;

    // 00, 01, 10, 11
    private int quarterCycleIndicator;

    private double ecefZ;

    // 0-6.5535 m
    private int height;

    private double lon;

    private double lat;

    private double alt;

    public int getMessageNumber() {
        return messageNumber;
    }

    public void setMessageNumber(int messageNumber) {
        this.messageNumber = messageNumber;
    }

    public int getReferenceStationId() {
        return referenceStationId;
    }

    public void setReferenceStationId(int referenceStationId) {
        this.referenceStationId = referenceStationId;
    }

    public int getItrf() {
        return itrf;
    }

    public void setItrf(int itrf) {
        this.itrf = itrf;
    }

    public int getGpsIndicator() {
        return gpsIndicator;
    }

    public void setGpsIndicator(int gpsIndicator) {
        this.gpsIndicator = gpsIndicator;
    }

    public int getGloIndicator() {
        return gloIndicator;
    }

    public void setGloIndicator(int gloIndicator) {
        this.gloIndicator = gloIndicator;
    }

    public int getGalIndicator() {
        return galIndicator;
    }

    public void setGalIndicator(int galIndicator) {
        this.galIndicator = galIndicator;
    }

    public int getReferenceStationIndicator() {
        return referenceStationIndicator;
    }

    public void setReferenceStationIndicator(int referenceStationIndicator) {
        this.referenceStationIndicator = referenceStationIndicator;
    }

    public double getEcefX() {
        return ecefX;
    }

    public void setEcefX(double ecefX) {
        this.ecefX = ecefX;
    }

    public int getReceiverOscillatorIndicator() {
        return receiverOscillatorIndicator;
    }

    public void setReceiverOscillatorIndicator(int receiverOscillatorIndicator) {
        this.receiverOscillatorIndicator = receiverOscillatorIndicator;
    }

    public double getEcefY() {
        return ecefY;
    }

    public void setEcefY(double ecefY) {
        this.ecefY = ecefY;
    }

    public int getQuarterCycleIndicator() {
        return quarterCycleIndicator;
    }

    public void setQuarterCycleIndicator(int quarterCycleIndicator) {
        this.quarterCycleIndicator = quarterCycleIndicator;
    }

    public double getEcefZ() {
        return ecefZ;
    }

    public void setEcefZ(double ecefZ) {
        this.ecefZ = ecefZ;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
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

    public double getAlt() {
        return alt;
    }

    public void setAlt(double alt) {
        this.alt = alt;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ReferenceStation{");
        sb.append("messageNumber=").append(messageNumber);
        sb.append(", referenceStationId=").append(referenceStationId);
        sb.append(", itrf=").append(itrf);
        sb.append(", gpsIndicator=").append(gpsIndicator);
        sb.append(", gloIndicator=").append(gloIndicator);
        sb.append(", galIndicator=").append(galIndicator);
        sb.append(", referenceStationIndicator=").append(referenceStationIndicator);
        sb.append(", ecefX=").append(ecefX);
        sb.append(", receiverOscillatorIndicator=").append(receiverOscillatorIndicator);
        sb.append(", ecefY=").append(ecefY);
        sb.append(", quarterCycleIndicator=").append(quarterCycleIndicator);
        sb.append(", ecefZ=").append(ecefZ);
        sb.append(", height=").append(height);
        sb.append(", lon=").append(lon);
        sb.append(", lat=").append(lat);
        sb.append(", alt=").append(alt);
        sb.append('}');
        return sb.toString();
    }
}
