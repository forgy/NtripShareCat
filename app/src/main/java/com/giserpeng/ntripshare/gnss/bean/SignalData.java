package com.giserpeng.ntripshare.gnss.bean;

public class SignalData {
	
	private String frequencyBand;
	private String signalCode;
	private double pseudoRange = -1;
	private double phaseRange = -1;
	private double snr = -1;
	private long lockTime = 30;

    public String getFrequencyBand() {
        return frequencyBand;
    }

    public void setFrequencyBand(String frequencyBand) {
        this.frequencyBand = frequencyBand;
    }

    public String getSignalCode() {
        return signalCode;
    }

    public void setSignalCode(String signalCode) {
        this.signalCode = signalCode;
    }

    public double getPseudoRange() {
        return pseudoRange;
    }

    public void setPseudoRange(double pseudoRange) {
        this.pseudoRange = pseudoRange;
    }

    public double getPhaseRange() {
        return phaseRange;
    }

    public void setPhaseRange(double phaseRange) {
        this.phaseRange = phaseRange;
    }

    public double getSnr() {
        return snr;
    }

    public void setSnr(double snr) {
        this.snr = snr;
    }

    public long getLockTime() {
        return lockTime;
    }

    public void setLockTime(long lockTime) {
        this.lockTime = lockTime;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("SignalData{");
        sb.append("frequencyBand='").append(frequencyBand).append('\'');
        sb.append(", signalCode='").append(signalCode).append('\'');
        sb.append(", pseudoRange=").append(pseudoRange);
        sb.append(", phaseRange=").append(phaseRange);
        sb.append(", snr=").append(snr);
        sb.append(", lockTime=").append(lockTime);
        sb.append('}');
        return sb.toString();
    }
}
