package com.giserpeng.ntripshare.ntrip.source;

public enum RtcmType {

    UNKNOWN("UNKNOWN"),
    BINEX("BINEX"),
    CMR("CMR"),
    NMEA("NMEA"),
    RAW("RAW"),
    RTCA("RTCA"),
    RTCM21("RTCM 2.1"),
    RTCM22("RTCM 2.2"),
    RTCM23("RTCM 2.3"),
    RTCM30("RTCM 3.0"),
    RTCM31("RTCM 3.1"),
    RTCM32("RTCM 3.2"),
    RTCM33("RTCM 3.3"),
    RTCM3X("RTCM 3.x"),
    RTCM_SAPOS("RTCM SAPOS");

    private String rtcmId;

    private RtcmType(String rtcmId) {
        this.rtcmId = rtcmId;
    }

    public static RtcmType getRtcmType(String id) {
        RtcmType rc = UNKNOWN;

        RtcmType[] vals = RtcmType.values();
        for (RtcmType v : vals) {
            if (v.getRtcmId().equals(id)) {
                rc = v;
                break;
            }
        }

        return rc;
    }

    public static RtcmType getRtcmTypeByName(String id) {
        RtcmType rc = UNKNOWN;

        RtcmType[] vals = RtcmType.values();
        for (RtcmType v : vals) {
            if (v.name().equals(id)) {
                rc = v;
                break;
            }
        }

        return rc;
    }

    public String getRtcmId() {
        return rtcmId;
    }

}
