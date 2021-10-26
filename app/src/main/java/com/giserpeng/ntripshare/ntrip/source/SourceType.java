package com.giserpeng.ntripshare.ntrip.source;

public enum SourceType {
    UNKNOWN, STR, CAS, NET;

    public static SourceType getSourceType(String id) {
        SourceType rc = UNKNOWN;

        SourceType[] vals = SourceType.values();
        for (SourceType v : vals) {
            if (v.name().equals(id)) {
                rc = v;
                break;
            }
        }

        return rc;
    }

}
