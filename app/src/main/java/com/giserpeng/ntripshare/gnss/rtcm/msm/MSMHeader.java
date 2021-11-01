package com.giserpeng.ntripshare.gnss.rtcm.msm;

import android.util.Log;

import java.util.List;


public class MSMHeader {

    private int messageNumber;
    private int referenceStationId;
    private int epochTime;
    private int multipleMessageFlag;
    private int iods;
    private int clockSteeringIndicator;
    private int externalClockIndicator;
    private int smoothIndicator;
    private int smoothInterval;
    private long satelliteMask;
    private long signalMask;
    private long cellMask;

    private int cellCount;
    private int validCellCount;
    private int satelliteCount;
    private int signalCount;
    private int headerLength;

    private List<Integer> satelliteList;
    private List<Integer> signalList;

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

    public int getEpochTime() {
        return epochTime;
    }

    public void setEpochTime(int epochTime) {
        this.epochTime = epochTime;
    }

    public int getMultipleMessageFlag() {
        return multipleMessageFlag;
    }

    public void setMultipleMessageFlag(int multipleMessageFlag) {
        this.multipleMessageFlag = multipleMessageFlag;
    }

    public int getIods() {
        return iods;
    }

    public void setIods(int iods) {
        this.iods = iods;
    }

    public int getClockSteeringIndicator() {
        return clockSteeringIndicator;
    }

    public void setClockSteeringIndicator(int clockSteeringIndicator) {
        this.clockSteeringIndicator = clockSteeringIndicator;
    }

    public int getExternalClockIndicator() {
        return externalClockIndicator;
    }

    public void setExternalClockIndicator(int externalClockIndicator) {
        this.externalClockIndicator = externalClockIndicator;
    }

    public int getSmoothIndicator() {
        return smoothIndicator;
    }

    public void setSmoothIndicator(int smoothIndicator) {
        this.smoothIndicator = smoothIndicator;
    }

    public int getSmoothInterval() {
        return smoothInterval;
    }

    public void setSmoothInterval(int smoothInterval) {
        this.smoothInterval = smoothInterval;
    }

    public long getSatelliteMask() {
        return satelliteMask;
    }

    public void setSatelliteMask(long satelliteMask) {
        this.satelliteMask = satelliteMask;
    }

    public long getSignalMask() {
        return signalMask;
    }

    public void setSignalMask(long signalMask) {
        this.signalMask = signalMask;
    }

    public long getCellMask() {
        return cellMask;
    }

    public void setCellMask(long cellMask) {
        this.cellMask = cellMask;
    }

    public int getCellCount() {
        return cellCount;
    }

    public void setCellCount(int cellCount) {
        this.cellCount = cellCount;
    }

    public int getValidCellCount() {
        return validCellCount;
    }

    public void setValidCellCount(int validCellCount) {
        this.validCellCount = validCellCount;
    }

    public int getSatelliteCount() {
        return satelliteCount;
    }

    public void setSatelliteCount(int satelliteCount) {
        this.satelliteCount = satelliteCount;
    }

    public int getSignalCount() {
        return signalCount;
    }

    public void setSignalCount(int signalCount) {
        this.signalCount = signalCount;
    }

    public int getHeaderLength() {
        return headerLength;
    }

    public void setHeaderLength(int headerLength) {
        this.headerLength = headerLength;
    }

    public List<Integer> getSatelliteList() {
        return satelliteList;
    }

    public void setSatelliteList(List<Integer> satelliteList) {
        this.satelliteList = satelliteList;
    }

    public List<Integer> getSignalList() {
        return signalList;
    }

    public void setSignalList(List<Integer> signalList) {
        this.signalList = signalList;
    }

    public String[] getFrequencyBand() {
        int signalCount = getSignalCount();
        if(signalCount == 2){
            return new String[] {"L1","L2"};
        }else if(signalCount == 3) {
            return new String[] {"L1","L2","L3"};
        }else if(signalCount == 4) {
            return new String[] {"L1","L2","L3","L5"};
        }else {
            Log.e("RTCM3MSMHeader", "The count of available signal mark: " + signalCount + " is not correct");
            return null;
        }
    }

    public boolean isValidCell(int index) {
        int cellCount = getCellCount();
        return ((this.cellMask >> (cellCount - 1 - index)) & 1) == 1;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("MSMHeader{");
        sb.append("messageNumber=").append(messageNumber);
        sb.append(", referenceStationId=").append(referenceStationId);
        sb.append(", epochTime=").append(epochTime);
        sb.append(", multipleMessageFlag=").append(multipleMessageFlag);
        sb.append(", iods=").append(iods);
        sb.append(", clockSteeringIndicator=").append(clockSteeringIndicator);
        sb.append(", externalClockIndicator=").append(externalClockIndicator);
        sb.append(", smoothIndicator=").append(smoothIndicator);
        sb.append(", smoothInterval=").append(smoothInterval);
        sb.append(", satelliteMask=").append(satelliteMask);
        sb.append(", signalMask=").append(signalMask);
        sb.append(", cellMask=").append(cellMask);
        sb.append(", cellCount=").append(cellCount);
        sb.append(", validCellCount=").append(validCellCount);
        sb.append(", satelliteCount=").append(satelliteCount);
        sb.append(", signalCount=").append(signalCount);
        sb.append(", headerLength=").append(headerLength);
        sb.append('}');
        return sb.toString();
    }
}
