package com.giserpeng.ntripshare.ntrip.source;

public enum AuthType {
    UNKNOWN, N, B, D;

    public static AuthType getAuthType(String id) {
        AuthType rc = UNKNOWN;

        AuthType[] vals = AuthType.values();
        for (AuthType v : vals) {
            if (v.name().equals(id)) {
                rc = v;
                break;
            }
        }

        return rc;
    }

}
