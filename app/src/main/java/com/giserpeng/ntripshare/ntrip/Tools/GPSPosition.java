package com.giserpeng.ntripshare.ntrip.Tools;


public class GPSPosition {
    public float time = 0.0f;
    public float lat = 0.0f;
    public float lon = 0.0f;
    public boolean fixed = false;
    public int quality = 0;
    public float dir = 0.0f;
    public float altitude = 0.0f;
    public float velocity = 0.0f;
    public long dataTime = System.currentTimeMillis();

    public GPSPosition(){

    }

    public void updatefix() {
        fixed = quality > 0;
    }

    public String getStatus( boolean isEn){
        String str;
        switch (this.quality) {
            case 0:
                if(isEn){
                    str = "Invalid";
                }else{
                    str = "无效解";
                }

                break;
            case 1:
                if(isEn){
                    str = "Single";
                }else{
                    str = "单点解";
                }

                break;
            case 2:
                if(isEn){
                    str = "DGPS";
                }else{
                    str = "差分解";
                }
                break;
            case 3:
                str = "PPS";
                break;
            case 4:
                if(isEn){
                    str = "Fix";
                }else{
                    str = "固定解";
                }
                break;
            case 5:
                if(isEn){
                    str = "Float";
                }else{
                    str = "浮点解";
                }
                break;
            case 6:
                str = "Estimated";
                break;
            case 7:
                str = "Manual";
                break;
            case 8:
                str = "Simulation";
                break;
            case 9:
                str = "WAAS";
                break;
            case 10:
                str = "No Data";
                break;
            default:
                str = "Unknown";
                break;
        }
        return  str;
    }

    public String toString() {
        return String.format("POSITION: lat: %f, lon: %f, time: %f, Q: %d, dir: %f, alt: %f, vel: %f", lat, lon, time, quality, dir, altitude, velocity);
    }

    public boolean isSet() {
        return lat != 0.0f && lon != 0.0f;
    }
}
