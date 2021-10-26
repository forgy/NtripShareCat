package com.giserpeng.ntripshare.ntrip.source;

public enum CarrierType {
    UNKNOWN(-1), DGPS(0), RTK(1)/* L1 */, RTK2(2) /* L1 & L2 */;

    private int carrierId;

    CarrierType(int carrierId) {
        this.carrierId = carrierId;
    }

    public static CarrierType getCarrierType(int id) {
        CarrierType rc;

        switch (id) {
            case 0:
                rc = DGPS;
                break;
            case 1:
                rc = RTK;
                break;
            case 2:
                rc = RTK2;
                break;
            default:
                rc = UNKNOWN;
                break;
        }

        return rc;
    }

    public static CarrierType getCarrierType(String id) {
        CarrierType rc = UNKNOWN;
        int nId = Integer.parseInt(id);
        return getCarrierType(nId);
    }

    public int getCarrierId() {
        return carrierId;
    }

}
