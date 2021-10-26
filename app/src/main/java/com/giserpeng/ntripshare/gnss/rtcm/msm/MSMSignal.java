package com.giserpeng.ntripshare.gnss.rtcm.msm;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Company: Copyright (c) 太昌电子 2018</p>
 *
 * @author liuhanling
 * @date 2019-04-03 09:49
 */
public class MSMSignal {

    private List<Integer> finePseudoRangeList = new ArrayList<>();
    private List<Integer> finePhaseRangeList = new ArrayList<>();
    private List<Integer> phaseRangeLockTimeIndicatorList = new ArrayList<>();
    private List<Integer> halfCycleAmbiguityIndicatorList = new ArrayList<>();
    private List<Integer> snrList = new ArrayList<>();

    public List<Integer> getFinePseudoRangeList() {
        return finePseudoRangeList;
    }

    public void setFinePseudoRangeList(List<Integer> finePseudoRangeList) {
        this.finePseudoRangeList = finePseudoRangeList;
    }

    public List<Integer> getFinePhaseRangeList() {
        return finePhaseRangeList;
    }

    public void setFinePhaseRangeList(List<Integer> finePhaseRangeList) {
        this.finePhaseRangeList = finePhaseRangeList;
    }

    public List<Integer> getPhaseRangeLockTimeIndicatorList() {
        return phaseRangeLockTimeIndicatorList;
    }

    public void setPhaseRangeLockTimeIndicatorList(List<Integer> phaseRangeLockTimeIndicatorList) {
        this.phaseRangeLockTimeIndicatorList = phaseRangeLockTimeIndicatorList;
    }

    public List<Integer> getHalfCycleAmbiguityIndicatorList() {
        return halfCycleAmbiguityIndicatorList;
    }

    public void setHalfCycleAmbiguityIndicatorList(List<Integer> halfCycleAmbiguityIndicatorList) {
        this.halfCycleAmbiguityIndicatorList = halfCycleAmbiguityIndicatorList;
    }

    public List<Integer> getSnrList() {
        return snrList;
    }

    public void setSnrList(List<Integer> snrList) {
        this.snrList = snrList;
    }

    public void addFinePseudorange(int finePseudoRange) {
        this.finePseudoRangeList.add(finePseudoRange);
    }

    public void addFinePhaseRange(int finePhaseRange) {
        this.finePhaseRangeList.add(finePhaseRange);
    }

    public void addPhaseRangeLockTimeIndicator(int phaseRangeLockTimeIndicator) {
        this.phaseRangeLockTimeIndicatorList.add(phaseRangeLockTimeIndicator);
    }

    public void addHalfCycleAmbiguityIndicator(int halfCycleAmbiguityIndicator) {
        this.halfCycleAmbiguityIndicatorList.add(halfCycleAmbiguityIndicator);
    }

    public void addSnr(int snr) {
        this.snrList.add(snr);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RTCM3MSMSignal{");
        sb.append("finePseudoRangeList=").append(finePseudoRangeList);
        sb.append(", finePhaseRangeList=").append(finePhaseRangeList);
        sb.append(", phaseRangeLockTimeIndicatorList=").append(phaseRangeLockTimeIndicatorList);
        sb.append(", halfCycleAmbiguityIndicatorList=").append(halfCycleAmbiguityIndicatorList);
        sb.append(", snrList=").append(snrList);
        sb.append('}');
        return sb.toString();
    }
}
