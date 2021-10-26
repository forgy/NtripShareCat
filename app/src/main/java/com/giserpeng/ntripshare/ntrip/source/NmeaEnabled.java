package com.giserpeng.ntripshare.ntrip.source;

public enum NmeaEnabled {
    UNKNOWN(-1), NMEA_DISABLED(0), NMEA_ENABLED(1);

    private int nmeaId;

    NmeaEnabled(int carrierType) {
        this.nmeaId = carrierType;
    }

    public static NmeaEnabled getNmeaEnabled(int id) {
        NmeaEnabled rc;

        switch (id) {
            case 0:
                rc = NMEA_DISABLED;
                break;
            case 1:
                rc = NMEA_ENABLED;
                break;
            default:
                rc = UNKNOWN;
                break;
        }

        return rc;
    }

    public static NmeaEnabled getNmeaEnabled(String id) {
        NmeaEnabled rc = UNKNOWN;
        int nId = Integer.parseInt(id);
        return getNmeaEnabled(nId);
    }

    public int getNmeaId() {
        return nmeaId;
    }

}
