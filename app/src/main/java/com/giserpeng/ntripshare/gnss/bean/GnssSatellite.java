package com.giserpeng.ntripshare.gnss.bean;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Company: Copyright (c) 太昌电子 2018</p>
 *
 * @author liuhanling
 * @date 2019-03-13 17:40
 */
public class GnssSatellite {

    // 类型
    private int type;

    // 噪声码
    private int prn;

    // 仰角
    private float elevation;

    // 方位角
    private float azimuth;

    // 信噪比
    private float snr;

    // 用于解算
    private boolean usedInFix;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getPrn() {
        return prn;
    }

    public void setPrn(int prn) {
        this.prn = prn;
    }

    public float getElevation() {
        return elevation;
    }

    public void setElevation(float elevation) {
        this.elevation = elevation;
    }

    public float getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(float azimuth) {
        this.azimuth = azimuth;
    }

    public float getSnr() {
        return snr;
    }

    public void setSnr(float snr) {
        this.snr = snr;
    }

    public boolean isUsedInFix() {
        return usedInFix;
    }

    public void setUsedInFix(boolean usedInFix) {
        this.usedInFix = usedInFix;
    }
}
