package com.giserpeng.ntripshare.gnss.rtcm.msm;

import java.util.ArrayList;
import java.util.List;


public class MSMSatellite {

    private List<Integer> millisecondsRoughRangeList = new ArrayList<>();
    private List<Integer> dotMillisecondsRoungRangeList = new ArrayList<>();

    public List<Integer> getMillisecondsRoughRangeList() {
        return millisecondsRoughRangeList;
    }

    public void setMillisecondsRoughRangeList(List<Integer> millisecondsRoughRangeList) {
        this.millisecondsRoughRangeList = millisecondsRoughRangeList;
    }

    public List<Integer> getDotMillisecondsRoungRangeList() {
        return dotMillisecondsRoungRangeList;
    }

    public void setDotMillisecondsRoungRangeList(List<Integer> dotMillisecondsRoungRangeList) {
        this.dotMillisecondsRoungRangeList = dotMillisecondsRoungRangeList;
    }

    public void addMillisecondsRoughRange(int millisecondsRoughRange) {
        millisecondsRoughRangeList.add(millisecondsRoughRange);
    }

    public void addDotMillisecondsRoughRange(int dotMillisecondsRoughRange) {
        dotMillisecondsRoungRangeList.add(dotMillisecondsRoughRange);
    }

    public int getSatelliteLength() {
        return millisecondsRoughRangeList.size() * (8 + 10);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("MSMSatellite{");
        sb.append("millisecondsRoughRangeList=").append(millisecondsRoughRangeList);
        sb.append(", dotMillisecondsRoungRangeList=").append(dotMillisecondsRoungRangeList);
        sb.append('}');
        return sb.toString();
    }
}